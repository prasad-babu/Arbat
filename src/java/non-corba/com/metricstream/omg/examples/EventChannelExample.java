package com.metricstream.omg.examples;

import com.metricstream.omg.event.*;
import com.metricstream.omg.eventchannel.*;
import com.metricstream.omg.naming.*;
import com.metricstream.omg.util.MigrationHelper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Example application demonstrating the usage of the non-CORBA event channel with push model.
 * This example shows how to:
 * - Create an event channel
 * - Connect push suppliers and consumers
 * - Send and receive events
 * - Register the event channel with the naming service
 * 
 * It also includes a migration snippet showing the comparison between CORBA and non-CORBA code.
 */
public class EventChannelExample {

    private static final int WAIT_TIMEOUT_SECONDS = 5;

    public static void main(String[] args) {
        System.out.println("Starting EventChannelExample...");
        
        try {
            // Example 1: Basic event channel usage
            basicEventChannelExample();
            
            // Example 2: Multiple consumers
            multipleConsumersExample();
            
            // Example 3: Event channel with naming service
            eventChannelWithNamingServiceExample();
            
            // Example 4: Migration comparison
            showMigrationComparison();
            
            System.out.println("\nEventChannelExample completed successfully!");
        } catch (Exception e) {
            System.err.println("\nEventChannelExample failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Example demonstrating basic event channel usage.
     */
    private static void basicEventChannelExample() throws Exception {
        System.out.println("\n=== Basic Event Channel Example ===");
        
        // Get the event channel factory
        EventChannelFactory factory = EventChannelFactory.getInstance();
        
        // Create an event channel
        EventChannel channel = factory.createEventChannel();
        System.out.println("Created event channel");
        
        // Create a consumer
        ExamplePushConsumer consumer = new ExamplePushConsumer("Consumer-1");
        
        // Connect the consumer to the channel
        ConsumerAdmin consumerAdmin = channel.for_consumers();
        ProxyPushSupplier proxySupplier = consumerAdmin.obtain_push_supplier();
        proxySupplier.connect_push_consumer(consumer);
        System.out.println("Connected push consumer to event channel");
        
        // Create a supplier
        ExamplePushSupplier supplier = new ExamplePushSupplier("Supplier-1");
        
        // Connect the supplier to the channel
        SupplierAdmin supplierAdmin = channel.for_suppliers();
        ProxyPushConsumer proxyConsumer = supplierAdmin.obtain_push_consumer();
        proxyConsumer.connect_push_supplier(supplier);
        System.out.println("Connected push supplier to event channel");
        
        // Send an event
        String eventData = "Hello, Event Channel!";
        supplier.sendEvent(proxyConsumer, eventData);
        System.out.println("Supplier sent event: " + eventData);
        
        // Wait for the event to be received
        if (consumer.waitForEvent(WAIT_TIMEOUT_SECONDS)) {
            System.out.println("Consumer received event: " + consumer.getLastEvent());
        } else {
            System.out.println("Timed out waiting for event");
        }
        
        // Disconnect
        proxySupplier.disconnect_push_supplier();
        proxyConsumer.disconnect_push_consumer();
        System.out.println("Disconnected consumer and supplier");
    }
    
    /**
     * Example demonstrating multiple consumers receiving events.
     */
    private static void multipleConsumersExample() throws Exception {
        System.out.println("\n=== Multiple Consumers Example ===");
        
        // Get the event channel factory
        EventChannelFactory factory = EventChannelFactory.getInstance();
        
        // Create an event channel
        EventChannel channel = factory.createEventChannel();
        System.out.println("Created event channel");
        
        // Create multiple consumers
        ExamplePushConsumer consumer1 = new ExamplePushConsumer("Consumer-1");
        ExamplePushConsumer consumer2 = new ExamplePushConsumer("Consumer-2");
        ExamplePushConsumer consumer3 = new ExamplePushConsumer("Consumer-3");
        
        // Connect the consumers to the channel
        ConsumerAdmin consumerAdmin = channel.for_consumers();
        
        ProxyPushSupplier proxySupplier1 = consumerAdmin.obtain_push_supplier();
        proxySupplier1.connect_push_consumer(consumer1);
        
        ProxyPushSupplier proxySupplier2 = consumerAdmin.obtain_push_supplier();
        proxySupplier2.connect_push_consumer(consumer2);
        
        ProxyPushSupplier proxySupplier3 = consumerAdmin.obtain_push_supplier();
        proxySupplier3.connect_push_consumer(consumer3);
        
        System.out.println("Connected three push consumers to event channel");
        
        // Create a supplier
        ExamplePushSupplier supplier = new ExamplePushSupplier("Supplier-1");
        
        // Connect the supplier to the channel
        SupplierAdmin supplierAdmin = channel.for_suppliers();
        ProxyPushConsumer proxyConsumer = supplierAdmin.obtain_push_consumer();
        proxyConsumer.connect_push_supplier(supplier);
        System.out.println("Connected push supplier to event channel");
        
        // Send an event
        String eventData = "Broadcast Event";
        supplier.sendEvent(proxyConsumer, eventData);
        System.out.println("Supplier sent broadcast event: " + eventData);
        
        // Wait for all consumers to receive the event
        boolean consumer1Received = consumer1.waitForEvent(WAIT_TIMEOUT_SECONDS);
        boolean consumer2Received = consumer2.waitForEvent(WAIT_TIMEOUT_SECONDS);
        boolean consumer3Received = consumer3.waitForEvent(WAIT_TIMEOUT_SECONDS);
        
        System.out.println("Consumer-1 received: " + (consumer1Received ? consumer1.getLastEvent() : "timed out"));
        System.out.println("Consumer-2 received: " + (consumer2Received ? consumer2.getLastEvent() : "timed out"));
        System.out.println("Consumer-3 received: " + (consumer3Received ? consumer3.getLastEvent() : "timed out"));
        
        // Send another event
        eventData = "Second Broadcast Event";
        supplier.sendEvent(proxyConsumer, eventData);
        System.out.println("Supplier sent second broadcast event: " + eventData);
        
        // Reset consumers for the second event
        consumer1.reset();
        consumer2.reset();
        consumer3.reset();
        
        // Wait for all consumers to receive the second event
        consumer1Received = consumer1.waitForEvent(WAIT_TIMEOUT_SECONDS);
        consumer2Received = consumer2.waitForEvent(WAIT_TIMEOUT_SECONDS);
        consumer3Received = consumer3.waitForEvent(WAIT_TIMEOUT_SECONDS);
        
        System.out.println("Consumer-1 received: " + (consumer1Received ? consumer1.getLastEvent() : "timed out"));
        System.out.println("Consumer-2 received: " + (consumer2Received ? consumer2.getLastEvent() : "timed out"));
        System.out.println("Consumer-3 received: " + (consumer3Received ? consumer3.getLastEvent() : "timed out"));
        
        // Disconnect one consumer
        proxySupplier2.disconnect_push_supplier();
        System.out.println("Disconnected Consumer-2");
        
        // Send a third event
        eventData = "Third Broadcast Event";
        supplier.sendEvent(proxyConsumer, eventData);
        System.out.println("Supplier sent third broadcast event: " + eventData);
        
        // Reset remaining consumers for the third event
        consumer1.reset();
        consumer3.reset();
        
        // Wait for remaining consumers to receive the third event
        consumer1Received = consumer1.waitForEvent(WAIT_TIMEOUT_SECONDS);
        consumer3Received = consumer3.waitForEvent(WAIT_TIMEOUT_SECONDS);
        
        System.out.println("Consumer-1 received: " + (consumer1Received ? consumer1.getLastEvent() : "timed out"));
        System.out.println("Consumer-3 received: " + (consumer3Received ? consumer3.getLastEvent() : "timed out"));
        
        // Disconnect remaining components
        proxySupplier1.disconnect_push_supplier();
        proxySupplier3.disconnect_push_supplier();
        proxyConsumer.disconnect_push_consumer();
        System.out.println("Disconnected all remaining consumers and supplier");
    }
    
    /**
     * Example demonstrating event channel usage with naming service.
     */
    private static void eventChannelWithNamingServiceExample() throws Exception {
        System.out.println("\n=== Event Channel with Naming Service Example ===");
        
        // Get the factories
        EventChannelFactory channelFactory = EventChannelFactory.getInstance();
        NamingServiceFactory namingFactory = NamingServiceFactory.getInstance();
        
        // Get the root naming context
        NamingContext rootContext = namingFactory.getRootContext();
        
        // Create an event channel
        EventChannel channel = channelFactory.createEventChannel();
        System.out.println("Created event channel");
        
        // Register the event channel with the naming service
        Name channelName = MigrationHelper.createNameFromPath("EventServices/MainChannel");
        rootContext.bind(channelName, channel);
        System.out.println("Registered event channel with naming service as 'EventServices/MainChannel'");
        
        // Look up the event channel from the naming service
        Object resolvedObj = rootContext.resolve(channelName);
        
        if (!(resolvedObj instanceof EventChannel)) {
            throw new AssertionError("Resolved object is not an EventChannel");
        }
        
        EventChannel resolvedChannel = (EventChannel) resolvedObj;
        System.out.println("Successfully resolved event channel from naming service");
        
        // Create a consumer and supplier
        ExamplePushConsumer consumer = new ExamplePushConsumer("Named-Consumer");
        ExamplePushSupplier supplier = new ExamplePushSupplier("Named-Supplier");
        
        // Connect the consumer and supplier to the resolved channel
        ConsumerAdmin consumerAdmin = resolvedChannel.for_consumers();
        ProxyPushSupplier proxySupplier = consumerAdmin.obtain_push_supplier();
        proxySupplier.connect_push_consumer(consumer);
        System.out.println("Connected push consumer to resolved event channel");
        
        SupplierAdmin supplierAdmin = resolvedChannel.for_suppliers();
        ProxyPushConsumer proxyConsumer = supplierAdmin.obtain_push_consumer();
        proxyConsumer.connect_push_supplier(supplier);
        System.out.println("Connected push supplier to resolved event channel");
        
        // Send an event
        String eventData = "Event via Naming Service";
        supplier.sendEvent(proxyConsumer, eventData);
        System.out.println("Supplier sent event: " + eventData);
        
        // Wait for the event to be received
        if (consumer.waitForEvent(WAIT_TIMEOUT_SECONDS)) {
            System.out.println("Consumer received event: " + consumer.getLastEvent());
        } else {
            System.out.println("Timed out waiting for event");
        }
        
        // Disconnect
        proxySupplier.disconnect_push_supplier();
        proxyConsumer.disconnect_push_consumer();
        
        // Unbind the event channel
        rootContext.unbind(channelName);
        System.out.println("Unregistered event channel from naming service");
    }
    
    /**
     * Shows a comparison between CORBA and non-CORBA event channel usage.
     */
    private static void showMigrationComparison() {
        System.out.println("\n=== Migration Comparison ===");
        
        System.out.println("CORBA Event Channel Example:");
        System.out.println("```java");
        System.out.println("// Initialize the ORB");
        System.out.println("ORB orb = ORB.init(args, null);");
        System.out.println("");
        System.out.println("// Get the root POA");
        System.out.println("POA rootPOA = POAHelper.narrow(orb.resolve_initial_references(\"RootPOA\"));");
        System.out.println("rootPOA.the_POAManager().activate();");
        System.out.println("");
        System.out.println("// Get the event channel");
        System.out.println("org.omg.CORBA.Object objRef = orb.resolve_initial_references(\"EventService\");");
        System.out.println("EventChannel channel = EventChannelHelper.narrow(objRef);");
        System.out.println("");
        System.out.println("// Create and connect a consumer");
        System.out.println("PushConsumerImpl consumer = new PushConsumerImpl();");
        System.out.println("ConsumerAdmin consumerAdmin = channel.for_consumers();");
        System.out.println("ProxyPushSupplier proxySupplier = consumerAdmin.obtain_push_supplier();");
        System.out.println("proxySupplier.connect_push_consumer(consumer._this(orb));");
        System.out.println("");
        System.out.println("// Create and connect a supplier");
        System.out.println("PushSupplierImpl supplier = new PushSupplierImpl();");
        System.out.println("SupplierAdmin supplierAdmin = channel.for_suppliers();");
        System.out.println("ProxyPushConsumer proxyConsumer = supplierAdmin.obtain_push_consumer();");
        System.out.println("proxyConsumer.connect_push_supplier(supplier._this(orb));");
        System.out.println("");
        System.out.println("// Send an event");
        System.out.println("Any any = orb.create_any();");
        System.out.println("any.insert_string(\"Hello, Event Channel!\");");
        System.out.println("proxyConsumer.push(any);");
        System.out.println("```");
        
        System.out.println("\nNon-CORBA Event Channel Example:");
        System.out.println("```java");
        System.out.println("// Get the event channel factory");
        System.out.println("EventChannelFactory factory = EventChannelFactory.getInstance();");
        System.out.println("EventChannel channel = factory.createEventChannel();");
        System.out.println("");
        System.out.println("// Create and connect a consumer");
        System.out.println("PushConsumer consumer = new PushConsumerImpl();");
        System.out.println("ConsumerAdmin consumerAdmin = channel.for_consumers();");
        System.out.println("ProxyPushSupplier proxySupplier = consumerAdmin.obtain_push_supplier();");
        System.out.println("proxySupplier.connect_push_consumer(consumer);");
        System.out.println("");
        System.out.println("// Create and connect a supplier");
        System.out.println("PushSupplier supplier = new PushSupplierImpl();");
        System.out.println("SupplierAdmin supplierAdmin = channel.for_suppliers();");
        System.out.println("ProxyPushConsumer proxyConsumer = supplierAdmin.obtain_push_consumer();");
        System.out.println("proxyConsumer.connect_push_supplier(supplier);");
        System.out.println("");
        System.out.println("// Send an event");
        System.out.println("proxyConsumer.push(\"Hello, Event Channel!\");");
        System.out.println("```");
        
        System.out.println("\nKey Differences:");
        System.out.println("1. No need for ORB initialization");
        System.out.println("2. No need for POA activation");
        System.out.println("3. Factory pattern for obtaining event channels");
        System.out.println("4. Direct Java object references instead of CORBA object references");
        System.out.println("5. No need for Any type - use regular Java objects");
        System.out.println("6. No need for Helper classes to narrow objects");
    }
    
    /**
     * Example implementation of PushConsumer for demonstration purposes.
     */
    static class ExamplePushConsumer implements PushConsumer {
        private final String name;
        private CountDownLatch eventLatch = new CountDownLatch(1);
        private String lastEvent;
        
        public ExamplePushConsumer(String name) {
            this.name = name;
        }
        
        @Override
        public void push(Object data) throws Disconnected {
            System.out.println(name + ": Received event: " + data);
            lastEvent = data.toString();
            eventLatch.countDown();
        }
        
        @Override
        public void disconnect_push_consumer() {
            System.out.println(name + ": Disconnected");
        }
        
        public boolean waitForEvent(int timeoutSeconds) throws InterruptedException {
            return eventLatch.await(timeoutSeconds, TimeUnit.SECONDS);
        }
        
        public String getLastEvent() {
            return lastEvent;
        }
        
        public void reset() {
            eventLatch = new CountDownLatch(1);
            lastEvent = null;
        }
    }
    
    /**
     * Example implementation of PushSupplier for demonstration purposes.
     */
    static class ExamplePushSupplier implements PushSupplier {
        private final String name;
        
        public ExamplePushSupplier(String name) {
            this.name = name;
        }
        
        public void sendEvent(ProxyPushConsumer proxyConsumer, Object eventData) throws Disconnected {
            System.out.println(name + ": Sending event: " + eventData);
            proxyConsumer.push(eventData);
        }
        
        @Override
        public void disconnect_push_supplier() {
            System.out.println(name + ": Disconnected");
        }
    }
}
