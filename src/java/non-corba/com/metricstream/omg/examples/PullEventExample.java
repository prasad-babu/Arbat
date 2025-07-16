package com.metricstream.omg.examples;

import com.metricstream.omg.event.*;
import com.metricstream.omg.eventchannel.*;
import com.metricstream.omg.naming.*;
import com.metricstream.omg.util.MigrationHelper;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Example application demonstrating the pull model for event communication
 * using the non-CORBA implementation.
 */
public class PullEventExample {
    
    private static final int PULL_INTERVAL_MS = 500;
    private static final int EXAMPLE_DURATION_MS = 10000;
    
    public static void main(String[] args) {
        System.out.println("Starting PullEventExample...");
        
        try {
            // Get the event channel factory
            EventChannelFactory channelFactory = EventChannelFactory.getInstance();
            
            // Create an event channel
            EventChannel channel = channelFactory.createEventChannel();
            System.out.println("Created event channel");
            
            // Register the event channel with the naming service
            NamingServiceFactory namingFactory = NamingServiceFactory.getInstance();
            NamingContext rootContext = namingFactory.getRootContext();
            
            Name channelName = MigrationHelper.createNameFromPath("EventChannel/PullExample");
            rootContext.bind(channelName, channel);
            System.out.println("Registered event channel with naming service as 'EventChannel/PullExample'");
            
            // Create and connect a pull supplier
            ExamplePullSupplier supplier = new ExamplePullSupplier();
            SupplierAdmin supplierAdmin = channel.for_suppliers();
            ProxyPullConsumer proxyConsumer = supplierAdmin.obtain_pull_consumer();
            proxyConsumer.connect_pull_supplier(supplier);
            System.out.println("Connected pull supplier to event channel");
            
            // Create and connect two pull consumers with different strategies
            ConsumerAdmin consumerAdmin = channel.for_consumers();
            
            // Consumer 1: Uses blocking pull() method
            ProxyPullSupplier proxySupplier1 = consumerAdmin.obtain_pull_supplier();
            BlockingPullConsumer consumer1 = new BlockingPullConsumer("Consumer1");
            proxySupplier1.connect_pull_consumer(consumer1);
            
            // Consumer 2: Uses non-blocking try_pull() method
            ProxyPullSupplier proxySupplier2 = consumerAdmin.obtain_pull_supplier();
            NonBlockingPullConsumer consumer2 = new NonBlockingPullConsumer("Consumer2");
            proxySupplier2.connect_pull_consumer(consumer2);
            
            System.out.println("Connected pull consumers to event channel");
            
            // Start the consumers in separate threads
            ExecutorService executor = Executors.newFixedThreadPool(2);
            
            // Flag to signal when to stop
            AtomicBoolean running = new AtomicBoolean(true);
            
            // Start the blocking consumer
            executor.submit(() -> {
                try {
                    while (running.get()) {
                        try {
                            Object data = proxySupplier1.pull();
                            consumer1.processEvent(data);
                            Thread.sleep(PULL_INTERVAL_MS);
                        } catch (Disconnected e) {
                            System.out.println("Consumer1: Supplier disconnected");
                            break;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                } finally {
                    System.out.println("Consumer1: Exiting pull loop");
                }
            });
            
            // Start the non-blocking consumer
            executor.submit(() -> {
                try {
                    while (running.get()) {
                        try {
                            BooleanHolder hasEvent = new BooleanHolder();
                            Object data = proxySupplier2.try_pull(hasEvent);
                            
                            if (hasEvent.value) {
                                consumer2.processEvent(data);
                            } else {
                                System.out.println("Consumer2: No event available");
                            }
                            
                            Thread.sleep(PULL_INTERVAL_MS);
                        } catch (Disconnected e) {
                            System.out.println("Consumer2: Supplier disconnected");
                            break;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                } finally {
                    System.out.println("Consumer2: Exiting pull loop");
                }
            });
            
            // Let the example run for a while
            System.out.println("Example will run for " + (EXAMPLE_DURATION_MS / 1000) + " seconds...");
            Thread.sleep(EXAMPLE_DURATION_MS);
            
            // Signal threads to stop and shutdown executor
            running.set(false);
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);
            
            // Disconnect consumers and suppliers
            try {
                proxySupplier1.disconnect_pull_supplier();
                proxySupplier2.disconnect_pull_supplier();
                proxyConsumer.disconnect_pull_consumer();
                System.out.println("Disconnected all consumers and suppliers");
            } catch (Exception e) {
                System.out.println("Error during disconnect: " + e.getMessage());
            }
            
            // Unregister the event channel
            rootContext.unbind(channelName);
            System.out.println("Unregistered event channel from naming service");
            
            System.out.println("PullEventExample completed successfully");
            
            // Show migration comparison
            showMigrationComparison();
            
        } catch (Exception e) {
            System.err.println("Error in PullEventExample: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows a comparison between CORBA and non-CORBA pull model code
     */
    private static void showMigrationComparison() {
        System.out.println("\n=== Migration Comparison: CORBA vs Non-CORBA Pull Model ===");
        
        System.out.println("\nCORBA Pull Model:");
        System.out.println("```java");
        System.out.println("// Initialize ORB and get naming service");
        System.out.println("ORB orb = ORB.init(args, null);");
        System.out.println("org.omg.CORBA.Object objRef = orb.resolve_initial_references(\"NameService\");");
        System.out.println("NamingContext namingContext = NamingContextHelper.narrow(objRef);");
        System.out.println("");
        System.out.println("// Create name components");
        System.out.println("NameComponent[] name = new NameComponent[2];");
        System.out.println("name[0] = new NameComponent(\"EventChannel\", \"\");");
        System.out.println("name[1] = new NameComponent(\"PullExample\", \"\");");
        System.out.println("");
        System.out.println("// Resolve event channel");
        System.out.println("org.omg.CORBA.Object channelObj = namingContext.resolve(name);");
        System.out.println("EventChannel channel = EventChannelHelper.narrow(channelObj);");
        System.out.println("");
        System.out.println("// Connect pull supplier");
        System.out.println("SupplierAdmin supplierAdmin = channel.for_suppliers();");
        System.out.println("ProxyPullConsumer proxyConsumer = supplierAdmin.obtain_pull_consumer();");
        System.out.println("proxyConsumer.connect_pull_supplier(pullSupplier);");
        System.out.println("");
        System.out.println("// Connect pull consumer");
        System.out.println("ConsumerAdmin consumerAdmin = channel.for_consumers();");
        System.out.println("ProxyPullSupplier proxySupplier = consumerAdmin.obtain_pull_supplier();");
        System.out.println("proxySupplier.connect_pull_consumer(pullConsumer);");
        System.out.println("");
        System.out.println("// Pull events");
        System.out.println("Any event = proxySupplier.pull();");
        System.out.println("String data = event.extract_string();");
        System.out.println("");
        System.out.println("// Try pull events");
        System.out.println("BooleanHolder hasEvent = new BooleanHolder();");
        System.out.println("Any event = proxySupplier.try_pull(hasEvent);");
        System.out.println("if (hasEvent.value) {");
        System.out.println("    String data = event.extract_string();");
        System.out.println("}");
        System.out.println("```");
        
        System.out.println("\nNon-CORBA Pull Model:");
        System.out.println("```java");
        System.out.println("// Get factories");
        System.out.println("EventChannelFactory channelFactory = EventChannelFactory.getInstance();");
        System.out.println("NamingServiceFactory namingFactory = NamingServiceFactory.getInstance();");
        System.out.println("NamingContext rootContext = namingFactory.getRootContext();");
        System.out.println("");
        System.out.println("// Create name");
        System.out.println("Name channelName = MigrationHelper.createNameFromPath(\"EventChannel/PullExample\");");
        System.out.println("");
        System.out.println("// Resolve event channel");
        System.out.println("EventChannel channel = (EventChannel) rootContext.resolve(channelName);");
        System.out.println("");
        System.out.println("// Connect pull supplier");
        System.out.println("SupplierAdmin supplierAdmin = channel.for_suppliers();");
        System.out.println("ProxyPullConsumer proxyConsumer = supplierAdmin.obtain_pull_consumer();");
        System.out.println("proxyConsumer.connect_pull_supplier(pullSupplier);");
        System.out.println("");
        System.out.println("// Connect pull consumer");
        System.out.println("ConsumerAdmin consumerAdmin = channel.for_consumers();");
        System.out.println("ProxyPullSupplier proxySupplier = consumerAdmin.obtain_pull_supplier();");
        System.out.println("proxySupplier.connect_pull_consumer(pullConsumer);");
        System.out.println("");
        System.out.println("// Pull events");
        System.out.println("Object event = proxySupplier.pull();");
        System.out.println("String data = (String) event;");
        System.out.println("");
        System.out.println("// Try pull events");
        System.out.println("BooleanHolder hasEvent = new BooleanHolder();");
        System.out.println("Object event = proxySupplier.try_pull(hasEvent);");
        System.out.println("if (hasEvent.value) {");
        System.out.println("    String data = (String) event;");
        System.out.println("}");
        System.out.println("```");
    }
    
    /**
     * Example implementation of a PullSupplier that provides events when pulled
     */
    static class ExamplePullSupplier implements PullSupplier {
        private int counter = 0;
        private boolean connected = true;
        
        @Override
        public Object pull() throws Disconnected {
            if (!connected) {
                throw new Disconnected();
            }
            
            counter++;
            String eventData = "Event #" + counter + " at " + System.currentTimeMillis();
            System.out.println("Supplier: Providing event: " + eventData);
            return eventData;
        }
        
        @Override
        public Object try_pull(BooleanHolder hasEvent) throws Disconnected {
            if (!connected) {
                throw new Disconnected();
            }
            
            // Simulate sometimes having no event available (every 3rd call)
            if (counter % 3 == 0) {
                hasEvent.value = false;
                System.out.println("Supplier: No event available");
                return null;
            } else {
                counter++;
                String eventData = "Event #" + counter + " at " + System.currentTimeMillis();
                System.out.println("Supplier: Providing event: " + eventData);
                hasEvent.value = true;
                return eventData;
            }
        }
        
        @Override
        public void disconnect_pull_supplier() {
            System.out.println("Supplier: Disconnected");
            connected = false;
        }
    }
    
    /**
     * Example implementation of a PullConsumer that uses blocking pull() method
     */
    static class BlockingPullConsumer implements PullConsumer {
        private final String name;
        
        public BlockingPullConsumer(String name) {
            this.name = name;
        }
        
        @Override
        public void disconnect_pull_consumer() {
            System.out.println(name + ": Disconnected");
        }
        
        public void processEvent(Object data) {
            System.out.println(name + ": Received event: " + data);
        }
    }
    
    /**
     * Example implementation of a PullConsumer that uses non-blocking try_pull() method
     */
    static class NonBlockingPullConsumer implements PullConsumer {
        private final String name;
        
        public NonBlockingPullConsumer(String name) {
            this.name = name;
        }
        
        @Override
        public void disconnect_pull_consumer() {
            System.out.println(name + ": Disconnected");
        }
        
        public void processEvent(Object data) {
            System.out.println(name + ": Received event: " + data);
        }
    }
}
