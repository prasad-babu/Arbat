package com.metricstream.omg.eventchannel;

import com.metricstream.omg.event.BooleanHolder;
import com.metricstream.omg.event.Disconnected;
import com.metricstream.omg.event.PullConsumer;
import com.metricstream.omg.event.PullSupplier;
import com.metricstream.omg.event.PushConsumer;
import com.metricstream.omg.event.PushSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the EventChannel interface.
 * This is the non-CORBA equivalent of the CORBA EventChannel implementation.
 */
public class EventChannelImpl implements EventChannel {
    
    private final ConsumerAdminImpl consumerAdmin;
    private final SupplierAdminImpl supplierAdmin;
    private final ExecutorService threadPool;
    private final LinkedBlockingQueue<Object> eventQueue;
    private volatile boolean destroyed = false;
    
    /**
     * Creates a new event channel implementation.
     */
    public EventChannelImpl() {
        this.consumerAdmin = new ConsumerAdminImpl(this);
        this.supplierAdmin = new SupplierAdminImpl(this);
        this.threadPool = Executors.newCachedThreadPool();
        this.eventQueue = new LinkedBlockingQueue<>();
        
        // Start a thread to process events from the queue
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                processEventQueue();
            }
        });
    }
    
    @Override
    public ConsumerAdmin for_consumers() {
        return consumerAdmin;
    }
    
    @Override
    public SupplierAdmin for_suppliers() {
        return supplierAdmin;
    }
    
    @Override
    public void destroy() {
        destroyed = true;
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        consumerAdmin.destroy();
        supplierAdmin.destroy();
    }
    
    /**
     * Pushes an event to all connected push consumers.
     * 
     * @param data The event data
     */
    void push(final Object data) {
        if (destroyed) {
            return;
        }
        
        // Add the event to the queue for processing
        eventQueue.offer(data);
    }
    
    /**
     * Processes events from the queue and distributes them to consumers.
     */
    private void processEventQueue() {
        while (!destroyed) {
            try {
                final Object data = eventQueue.poll(1, TimeUnit.SECONDS);
                if (data != null) {
                    threadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            consumerAdmin.deliverEvent(data);
                        }
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Implementation of the ConsumerAdmin interface.
     */
    private class ConsumerAdminImpl implements ConsumerAdmin {
        
        private final EventChannelImpl channel;
        private final List<ProxyPushSupplierImpl> pushSuppliers = new ArrayList<>();
        private final List<ProxyPullSupplierImpl> pullSuppliers = new ArrayList<>();
        
        ConsumerAdminImpl(EventChannelImpl channel) {
            this.channel = channel;
        }
        
        @Override
        public ProxyPushSupplier obtain_push_supplier() {
            ProxyPushSupplierImpl supplier = new ProxyPushSupplierImpl(this);
            synchronized (pushSuppliers) {
                pushSuppliers.add(supplier);
            }
            return supplier;
        }
        
        @Override
        public ProxyPullSupplier obtain_pull_supplier() {
            ProxyPullSupplierImpl supplier = new ProxyPullSupplierImpl(this);
            synchronized (pullSuppliers) {
                pullSuppliers.add(supplier);
            }
            return supplier;
        }
        
        /**
         * Delivers an event to all connected consumers.
         * 
         * @param data The event data
         */
        void deliverEvent(final Object data) {
            // Deliver to push consumers
            List<ProxyPushSupplierImpl> clonePushSuppliers;
            synchronized (pushSuppliers) {
                clonePushSuppliers = new ArrayList<>(pushSuppliers);
            }
            
            for (final ProxyPushSupplierImpl supplier : clonePushSuppliers) {
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        supplier.deliverEvent(data);
                    }
                });
            }
            
            // Store for pull consumers
            List<ProxyPullSupplierImpl> clonePullSuppliers;
            synchronized (pullSuppliers) {
                clonePullSuppliers = new ArrayList<>(pullSuppliers);
            }
            
            for (ProxyPullSupplierImpl supplier : clonePullSuppliers) {
                supplier.storeEvent(data);
            }
        }
        
        /**
         * Removes a proxy push supplier from the list.
         * 
         * @param supplier The supplier to remove
         */
        void removeProxyPushSupplier(ProxyPushSupplierImpl supplier) {
            synchronized (pushSuppliers) {
                pushSuppliers.remove(supplier);
            }
        }
        
        /**
         * Removes a proxy pull supplier from the list.
         * 
         * @param supplier The supplier to remove
         */
        void removeProxyPullSupplier(ProxyPullSupplierImpl supplier) {
            synchronized (pullSuppliers) {
                pullSuppliers.remove(supplier);
            }
        }
        
        /**
         * Destroys this admin and all its proxies.
         */
        void destroy() {
            List<ProxyPushSupplierImpl> clonePushSuppliers;
            synchronized (pushSuppliers) {
                clonePushSuppliers = new ArrayList<>(pushSuppliers);
                pushSuppliers.clear();
            }
            
            for (ProxyPushSupplierImpl supplier : clonePushSuppliers) {
                supplier.destroy();
            }
            
            List<ProxyPullSupplierImpl> clonePullSuppliers;
            synchronized (pullSuppliers) {
                clonePullSuppliers = new ArrayList<>(pullSuppliers);
                pullSuppliers.clear();
            }
            
            for (ProxyPullSupplierImpl supplier : clonePullSuppliers) {
                supplier.destroy();
            }
        }
    }
    
    /**
     * Implementation of the SupplierAdmin interface.
     */
    private class SupplierAdminImpl implements SupplierAdmin {
        
        private final EventChannelImpl channel;
        private final List<ProxyPushConsumerImpl> pushConsumers = new ArrayList<>();
        private final List<ProxyPullConsumerImpl> pullConsumers = new ArrayList<>();
        
        SupplierAdminImpl(EventChannelImpl channel) {
            this.channel = channel;
        }
        
        @Override
        public ProxyPushConsumer obtain_push_consumer() {
            ProxyPushConsumerImpl consumer = new ProxyPushConsumerImpl(this);
            synchronized (pushConsumers) {
                pushConsumers.add(consumer);
            }
            return consumer;
        }
        
        @Override
        public ProxyPullConsumer obtain_pull_consumer() {
            ProxyPullConsumerImpl consumer = new ProxyPullConsumerImpl(this);
            synchronized (pullConsumers) {
                pullConsumers.add(consumer);
            }
            return consumer;
        }
        
        /**
         * Removes a proxy push consumer from the list.
         * 
         * @param consumer The consumer to remove
         */
        void removeProxyPushConsumer(ProxyPushConsumerImpl consumer) {
            synchronized (pushConsumers) {
                pushConsumers.remove(consumer);
            }
        }
        
        /**
         * Removes a proxy pull consumer from the list.
         * 
         * @param consumer The consumer to remove
         */
        void removeProxyPullConsumer(ProxyPullConsumerImpl consumer) {
            synchronized (pullConsumers) {
                pullConsumers.remove(consumer);
            }
        }
        
        /**
         * Destroys this admin and all its proxies.
         */
        void destroy() {
            List<ProxyPushConsumerImpl> clonePushConsumers;
            synchronized (pushConsumers) {
                clonePushConsumers = new ArrayList<>(pushConsumers);
                pushConsumers.clear();
            }
            
            for (ProxyPushConsumerImpl consumer : clonePushConsumers) {
                consumer.destroy();
            }
            
            List<ProxyPullConsumerImpl> clonePullConsumers;
            synchronized (pullConsumers) {
                clonePullConsumers = new ArrayList<>(pullConsumers);
                pullConsumers.clear();
            }
            
            for (ProxyPullConsumerImpl consumer : clonePullConsumers) {
                consumer.destroy();
            }
        }
    }
    
    /**
     * Implementation of the ProxyPushSupplier interface.
     */
    private class ProxyPushSupplierImpl implements ProxyPushSupplier {
        
        private final ConsumerAdminImpl admin;
        private PushConsumer consumer;
        private boolean connected = false;
        private boolean destroyed = false;
        
        ProxyPushSupplierImpl(ConsumerAdminImpl admin) {
            this.admin = admin;
        }
        
        @Override
        public synchronized void connect_push_consumer(PushConsumer push_consumer) throws AlreadyConnected {
            if (destroyed) {
                throw new IllegalStateException("Proxy has been destroyed");
            }
            
            if (connected) {
                throw new AlreadyConnected("Push consumer already connected");
            }
            
            this.consumer = push_consumer;
            this.connected = true;
        }
        
        @Override
        public synchronized void disconnect_push_supplier() {
            if (connected && consumer != null) {
                consumer.disconnect_push_consumer();
                consumer = null;
                connected = false;
            }
            
            admin.removeProxyPushSupplier(this);
        }
        
        /**
         * Delivers an event to the connected consumer.
         * 
         * @param data The event data
         */
        synchronized void deliverEvent(Object data) {
            if (connected && consumer != null) {
                try {
                    consumer.push(data);
                } catch (Disconnected e) {
                    disconnect_push_supplier();
                } catch (Exception e) {
                    // Log the exception but don't disconnect
                    System.err.println("Error delivering event to push consumer: " + e.getMessage());
                }
            }
        }
        
        /**
         * Destroys this proxy.
         */
        synchronized void destroy() {
            if (!destroyed) {
                destroyed = true;
                disconnect_push_supplier();
            }
        }
    }
    
    /**
     * Implementation of the ProxyPullSupplier interface.
     */
    private class ProxyPullSupplierImpl implements ProxyPullSupplier {
        
        private final ConsumerAdminImpl admin;
        private PullConsumer consumer;
        private boolean connected = false;
        private boolean destroyed = false;
        private final LinkedBlockingQueue<Object> eventQueue = new LinkedBlockingQueue<>();
        
        ProxyPullSupplierImpl(ConsumerAdminImpl admin) {
            this.admin = admin;
        }
        
        @Override
        public synchronized void connect_pull_consumer(PullConsumer pull_consumer) throws AlreadyConnected {
            if (destroyed) {
                throw new IllegalStateException("Proxy has been destroyed");
            }
            
            if (connected) {
                throw new AlreadyConnected("Pull consumer already connected");
            }
            
            this.consumer = pull_consumer;
            this.connected = true;
        }
        
        @Override
        public synchronized Object try_pull(BooleanHolder has_event) throws Disconnected {
            if (!connected) {
                throw new Disconnected("Proxy is not connected");
            }
            
            Object event = eventQueue.poll();
            has_event.value = (event != null);
            return event;
        }
        
        @Override
        public synchronized Object pull() throws Disconnected {
            if (!connected) {
                throw new Disconnected("Proxy is not connected");
            }
            
            try {
                return eventQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new Disconnected("Pull operation interrupted");
            }
        }
        
        @Override
        public synchronized void disconnect_pull_supplier() {
            if (connected && consumer != null) {
                consumer.disconnect_pull_consumer();
                consumer = null;
                connected = false;
            }
            
            eventQueue.clear();
            admin.removeProxyPullSupplier(this);
        }
        
        /**
         * Stores an event for later retrieval by pull consumers.
         * 
         * @param data The event data
         */
        synchronized void storeEvent(Object data) {
            if (connected) {
                eventQueue.offer(data);
            }
        }
        
        /**
         * Destroys this proxy.
         */
        synchronized void destroy() {
            if (!destroyed) {
                destroyed = true;
                disconnect_pull_supplier();
            }
        }
    }
    
    /**
     * Implementation of the ProxyPushConsumer interface.
     */
    private class ProxyPushConsumerImpl implements ProxyPushConsumer {
        
        private final SupplierAdminImpl admin;
        private PushSupplier supplier;
        private boolean connected = false;
        private boolean destroyed = false;
        
        ProxyPushConsumerImpl(SupplierAdminImpl admin) {
            this.admin = admin;
        }
        
        @Override
        public synchronized void connect_push_supplier(PushSupplier push_supplier) throws AlreadyConnected {
            if (destroyed) {
                throw new IllegalStateException("Proxy has been destroyed");
            }
            
            if (connected) {
                throw new AlreadyConnected("Push supplier already connected");
            }
            
            this.supplier = push_supplier;
            this.connected = true;
        }
        
        @Override
        public synchronized void push(Object data) throws Disconnected {
            if (!connected) {
                throw new Disconnected("Proxy is not connected");
            }
            
            // Forward the event to the event channel
            channel.push(data);
        }
        
        @Override
        public synchronized void disconnect_push_consumer() {
            if (connected && supplier != null) {
                supplier.disconnect_push_supplier();
                supplier = null;
                connected = false;
            }
            
            admin.removeProxyPushConsumer(this);
        }
        
        /**
         * Destroys this proxy.
         */
        synchronized void destroy() {
            if (!destroyed) {
                destroyed = true;
                disconnect_push_consumer();
            }
        }
    }
    
    /**
     * Implementation of the ProxyPullConsumer interface.
     */
    private class ProxyPullConsumerImpl implements ProxyPullConsumer {
        
        private final SupplierAdminImpl admin;
        private PullSupplier supplier;
        private boolean connected = false;
        private boolean destroyed = false;
        private final Thread pullThread;
        private volatile boolean running = true;
        
        ProxyPullConsumerImpl(SupplierAdminImpl admin) {
            this.admin = admin;
            
            // Create a thread to pull events from the supplier
            this.pullThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    pullEvents();
                }
            });
            this.pullThread.setDaemon(true);
            this.pullThread.start();
        }
        
        @Override
        public synchronized void connect_pull_supplier(PullSupplier pull_supplier) throws AlreadyConnected {
            if (destroyed) {
                throw new IllegalStateException("Proxy has been destroyed");
            }
            
            if (connected) {
                throw new AlreadyConnected("Pull supplier already connected");
            }
            
            this.supplier = pull_supplier;
            this.connected = true;
        }
        
        @Override
        public synchronized void disconnect_pull_consumer() {
            running = false;
            
            if (connected && supplier != null) {
                supplier.disconnect_pull_supplier();
                supplier = null;
                connected = false;
            }
            
            admin.removeProxyPullConsumer(this);
            
            // Interrupt the pull thread
            pullThread.interrupt();
        }
        
        /**
         * Pulls events from the supplier and forwards them to the event channel.
         */
        private void pullEvents() {
            while (running) {
                try {
                    PullSupplier currentSupplier;
                    synchronized (this) {
                        if (!connected || supplier == null) {
                            Thread.sleep(1000);
                            continue;
                        }
                        currentSupplier = supplier;
                    }
                    
                    // Try to pull an event
                    BooleanHolder hasEvent = new BooleanHolder();
                    Object data = currentSupplier.try_pull(hasEvent);
                    
                    if (hasEvent.value && data != null) {
                        // Forward the event to the event channel
                        channel.push(data);
                    } else {
                        // Wait a bit before trying again
                        Thread.sleep(100);
                    }
                } catch (Disconnected e) {
                    disconnect_pull_consumer();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // Log the exception but don't disconnect
                    System.err.println("Error pulling event from supplier: " + e.getMessage());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        /**
         * Destroys this proxy.
         */
        synchronized void destroy() {
            if (!destroyed) {
                destroyed = true;
                disconnect_pull_consumer();
            }
        }
    }
}
