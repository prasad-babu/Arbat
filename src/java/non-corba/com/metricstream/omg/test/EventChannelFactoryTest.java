package com.metricstream.omg.test;

import com.metricstream.omg.event.*;
import com.metricstream.omg.eventchannel.*;
import com.metricstream.omg.naming.*;
import com.metricstream.omg.util.MigrationHelper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test class for verifying the functionality of the EventChannelFactory.
 */
public class EventChannelFactoryTest {

    private static final int WAIT_TIMEOUT_SECONDS = 5;

    public static void main(String[] args) {
        System.out.println("Starting EventChannelFactoryTest...");
        
        try {
            testEventChannelCreation();
            testEventChannelRegistration();
            testEventChannelLookup();
            testMultipleEventChannels();
            testEventChannelDestruction();
            
            System.out.println("\nAll EventChannelFactoryTest tests passed successfully!");
        } catch (Exception e) {
            System.err.println("\nEventChannelFactoryTest failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tests basic event channel creation and verifies that consumer and supplier admins are available.
     */
    private static void testEventChannelCreation() throws Exception {
        System.out.println("\n=== Testing Event Channel Creation ===");
        
        // Get the event channel factory
        EventChannelFactory factory = EventChannelFactory.getInstance();
        
        // Create an event channel
        EventChannel channel = factory.createEventChannel();
        System.out.println("Created event channel");
        
        // Verify that consumer and supplier admins are available
        ConsumerAdmin consumerAdmin = channel.for_consumers();
        SupplierAdmin supplierAdmin = channel.for_suppliers();
        
        if (consumerAdmin == null) {
            throw new AssertionError("Consumer admin is null");
        }
        
        if (supplierAdmin == null) {
            throw new AssertionError("Supplier admin is null");
        }
        
        System.out.println("Verified consumer and supplier admins are available");
        
        // Verify that proxy objects can be obtained
        ProxyPushSupplier proxyPushSupplier = consumerAdmin.obtain_push_supplier();
        ProxyPullSupplier proxyPullSupplier = consumerAdmin.obtain_pull_supplier();
        ProxyPushConsumer proxyPushConsumer = supplierAdmin.obtain_push_consumer();
        ProxyPullConsumer proxyPullConsumer = supplierAdmin.obtain_pull_consumer();
        
        if (proxyPushSupplier == null || proxyPullSupplier == null || 
            proxyPushConsumer == null || proxyPullConsumer == null) {
            throw new AssertionError("One or more proxy objects are null");
        }
        
        System.out.println("Verified proxy objects can be obtained");
        System.out.println("Event channel creation test passed");
    }
    
    /**
     * Tests registering an event channel with the naming service and resolving it.
     */
    private static void testEventChannelRegistration() throws Exception {
        System.out.println("\n=== Testing Event Channel Registration ===");
        
        // Get the factories
        EventChannelFactory channelFactory = EventChannelFactory.getInstance();
        NamingServiceFactory namingFactory = NamingServiceFactory.getInstance();
        NamingContext rootContext = namingFactory.getRootContext();
        
        // Create an event channel
        EventChannel channel = channelFactory.createEventChannel();
        
        // Register the event channel with the naming service
        String channelName = "TestChannel";
        String channelPath = "EventChannel/Test";
        channelFactory.registerEventChannel(channelName, channel, channelPath);
        System.out.println("Registered event channel with name '" + channelName + "' at path '" + channelPath + "'");
        
        // Resolve the event channel from the naming service
        Name name = MigrationHelper.createNameFromPath(channelPath);
        Object resolvedObj = rootContext.resolve(name);
        
        if (!(resolvedObj instanceof EventChannel)) {
            throw new AssertionError("Resolved object is not an EventChannel");
        }
        
        EventChannel resolvedChannel = (EventChannel) resolvedObj;
        System.out.println("Successfully resolved event channel from naming service");
        
        // Verify that the resolved channel is functional
        ConsumerAdmin consumerAdmin = resolvedChannel.for_consumers();
        SupplierAdmin supplierAdmin = resolvedChannel.for_suppliers();
        
        if (consumerAdmin == null || supplierAdmin == null) {
            throw new AssertionError("Resolved channel is not functional");
        }
        
        System.out.println("Verified resolved channel is functional");
        
        // Clean up
        rootContext.unbind(name);
        System.out.println("Unbound event channel from naming service");
        System.out.println("Event channel registration test passed");
    }
    
    /**
     * Tests looking up an event channel by name and path.
     */
    private static void testEventChannelLookup() throws Exception {
        System.out.println("\n=== Testing Event Channel Lookup ===");
        
        // Get the factories
        EventChannelFactory channelFactory = EventChannelFactory.getInstance();
        NamingServiceFactory namingFactory = NamingServiceFactory.getInstance();
        NamingContext rootContext = namingFactory.getRootContext();
        
        // Create an event channel
        EventChannel channel = channelFactory.createEventChannel();
        
        // Register the event channel with the naming service
        String channelName = "LookupChannel";
        String channelPath = "EventChannel/Lookup";
        channelFactory.registerEventChannel(channelName, channel, channelPath);
        System.out.println("Registered event channel with name '" + channelName + "' at path '" + channelPath + "'");
        
        // Look up the event channel by name
        EventChannel lookedUpByName = channelFactory.lookupEventChannel(channelName);
        
        if (lookedUpByName == null) {
            throw new AssertionError("Failed to look up event channel by name");
        }
        
        System.out.println("Successfully looked up event channel by name");
        
        // Look up the event channel by path
        EventChannel lookedUpByPath = channelFactory.lookupEventChannelByPath(channelPath);
        
        if (lookedUpByPath == null) {
            throw new AssertionError("Failed to look up event channel by path");
        }
        
        System.out.println("Successfully looked up event channel by path");
        
        // Verify that the looked up channels are functional
        ConsumerAdmin consumerAdmin1 = lookedUpByName.for_consumers();
        ConsumerAdmin consumerAdmin2 = lookedUpByPath.for_consumers();
        
        if (consumerAdmin1 == null || consumerAdmin2 == null) {
            throw new AssertionError("Looked up channels are not functional");
        }
        
        System.out.println("Verified looked up channels are functional");
        
        // Clean up
        Name name = MigrationHelper.createNameFromPath(channelPath);
        rootContext.unbind(name);
        System.out.println("Unbound event channel from naming service");
        System.out.println("Event channel lookup test passed");
    }
    
    /**
     * Tests creating and using multiple event channels concurrently.
     */
    private static void testMultipleEventChannels() throws Exception {
        System.out.println("\n=== Testing Multiple Event Channels ===");
        
        // Get the factories
        EventChannelFactory channelFactory = EventChannelFactory.getInstance();
        NamingServiceFactory namingFactory = NamingServiceFactory.getInstance();
        NamingContext rootContext = namingFactory.getRootContext();
        
        // Create multiple event channels
        EventChannel channel1 = channelFactory.createEventChannel();
        EventChannel channel2 = channelFactory.createEventChannel();
        EventChannel channel3 = channelFactory.createEventChannel();
        
        // Register the event channels with the naming service
        channelFactory.registerEventChannel("Channel1", channel1, "EventChannel/Multi/1");
        channelFactory.registerEventChannel("Channel2", channel2, "EventChannel/Multi/2");
        channelFactory.registerEventChannel("Channel3", channel3, "EventChannel/Multi/3");
        
        System.out.println("Created and registered 3 event channels");
        
        // Create test consumers and suppliers
        TestPushConsumer consumer1 = new TestPushConsumer("Consumer1");
        TestPushConsumer consumer2 = new TestPushConsumer("Consumer2");
        TestPushConsumer consumer3 = new TestPushConsumer("Consumer3");
        
        TestPushSupplier supplier1 = new TestPushSupplier("Supplier1");
        TestPushSupplier supplier2 = new TestPushSupplier("Supplier2");
        TestPushSupplier supplier3 = new TestPushSupplier("Supplier3");
        
        // Connect consumers and suppliers to their respective channels
        connectConsumerAndSupplier(channel1, consumer1, supplier1);
        connectConsumerAndSupplier(channel2, consumer2, supplier2);
        connectConsumerAndSupplier(channel3, consumer3, supplier3);
        
        System.out.println("Connected consumers and suppliers to their respective channels");
        
        // Push events through each channel
        supplier1.sendEvent("Event from Supplier1");
        supplier2.sendEvent("Event from Supplier2");
        supplier3.sendEvent("Event from Supplier3");
        
        // Wait for all events to be received
        if (!consumer1.waitForEvent(WAIT_TIMEOUT_SECONDS) ||
            !consumer2.waitForEvent(WAIT_TIMEOUT_SECONDS) ||
            !consumer3.waitForEvent(WAIT_TIMEOUT_SECONDS)) {
            throw new AssertionError("Not all events were received within the timeout period");
        }
        
        System.out.println("All events were successfully received");
        
        // Verify that events were received by the correct consumers
        if (!consumer1.getLastEvent().equals("Event from Supplier1") ||
            !consumer2.getLastEvent().equals("Event from Supplier2") ||
            !consumer3.getLastEvent().equals("Event from Supplier3")) {
            throw new AssertionError("Events were not received by the correct consumers");
        }
        
        System.out.println("Verified events were received by the correct consumers");
        
        // Clean up
        rootContext.unbind(MigrationHelper.createNameFromPath("EventChannel/Multi/1"));
        rootContext.unbind(MigrationHelper.createNameFromPath("EventChannel/Multi/2"));
        rootContext.unbind(MigrationHelper.createNameFromPath("EventChannel/Multi/3"));
        
        System.out.println("Unbound all event channels from naming service");
        System.out.println("Multiple event channels test passed");
    }
    
    /**
     * Tests destroying an event channel and verifying that it is unregistered.
     */
    private static void testEventChannelDestruction() throws Exception {
        System.out.println("\n=== Testing Event Channel Destruction ===");
        
        // Get the factories
        EventChannelFactory channelFactory = EventChannelFactory.getInstance();
        NamingServiceFactory namingFactory = NamingServiceFactory.getInstance();
        NamingContext rootContext = namingFactory.getRootContext();
        
        // Create an event channel
        EventChannel channel = channelFactory.createEventChannel();
        
        // Register the event channel with the naming service
        String channelName = "DestructChannel";
        String channelPath = "EventChannel/Destruct";
        channelFactory.registerEventChannel(channelName, channel, channelPath);
        System.out.println("Registered event channel with name '" + channelName + "' at path '" + channelPath + "'");
        
        // Verify that the channel is registered
        EventChannel lookedUp = channelFactory.lookupEventChannel(channelName);
        
        if (lookedUp == null) {
            throw new AssertionError("Channel is not registered");
        }
        
        System.out.println("Verified channel is registered");
        
        // Destroy the event channel
        channelFactory.destroyEventChannel(channelName);
        System.out.println("Destroyed event channel");
        
        // Verify that the channel is no longer registered
        try {
            lookedUp = channelFactory.lookupEventChannel(channelName);
            if (lookedUp != null) {
                throw new AssertionError("Channel is still registered after destruction");
            }
        } catch (Exception e) {
            // Expected exception
            System.out.println("Verified channel is no longer registered");
        }
        
        // Verify that the channel is no longer bound in the naming service
        try {
            Name name = MigrationHelper.createNameFromPath(channelPath);
            Object obj = rootContext.resolve(name);
            if (obj != null) {
                throw new AssertionError("Channel is still bound in naming service after destruction");
            }
        } catch (NotFound e) {
            // Expected exception
            System.out.println("Verified channel is no longer bound in naming service");
        }
        
        System.out.println("Event channel destruction test passed");
    }
    
    /**
     * Helper method to connect a consumer and supplier to an event channel.
     */
    private static void connectConsumerAndSupplier(EventChannel channel, 
                                                  TestPushConsumer consumer, 
                                                  TestPushSupplier supplier) throws Exception {
        // Connect consumer
        ConsumerAdmin consumerAdmin = channel.for_consumers();
        ProxyPushSupplier proxySupplier = consumerAdmin.obtain_push_supplier();
        proxySupplier.connect_push_consumer(consumer);
        consumer.setProxySupplier(proxySupplier);
        
        // Connect supplier
        SupplierAdmin supplierAdmin = channel.for_suppliers();
        ProxyPushConsumer proxyConsumer = supplierAdmin.obtain_push_consumer();
        proxyConsumer.connect_push_supplier(supplier);
        supplier.setProxyConsumer(proxyConsumer);
    }
    
    /**
     * Test implementation of PushConsumer for testing purposes.
     */
    static class TestPushConsumer implements PushConsumer {
        private final String name;
        private final CountDownLatch eventLatch = new CountDownLatch(1);
        private ProxyPushSupplier proxySupplier;
        private String lastEvent;
        
        public TestPushConsumer(String name) {
            this.name = name;
        }
        
        public void setProxySupplier(ProxyPushSupplier proxySupplier) {
            this.proxySupplier = proxySupplier;
        }
        
        @Override
        public void push(Object data) throws Disconnected {
            System.out.println(name + ": Received event: " + data);
            lastEvent = (String) data;
            eventLatch.countDown();
        }
        
        @Override
        public void disconnect_push_consumer() {
            System.out.println(name + ": Disconnected");
            if (proxySupplier != null) {
                try {
                    proxySupplier.disconnect_push_supplier();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        public boolean waitForEvent(int timeoutSeconds) throws InterruptedException {
            return eventLatch.await(timeoutSeconds, TimeUnit.SECONDS);
        }
        
        public String getLastEvent() {
            return lastEvent;
        }
    }
    
    /**
     * Test implementation of PushSupplier for testing purposes.
     */
    static class TestPushSupplier implements PushSupplier {
        private final String name;
        private ProxyPushConsumer proxyConsumer;
        
        public TestPushSupplier(String name) {
            this.name = name;
        }
        
        public void setProxyConsumer(ProxyPushConsumer proxyConsumer) {
            this.proxyConsumer = proxyConsumer;
        }
        
        public void sendEvent(String eventData) throws Disconnected {
            System.out.println(name + ": Sending event: " + eventData);
            proxyConsumer.push(eventData);
        }
        
        @Override
        public void disconnect_push_supplier() {
            System.out.println(name + ": Disconnected");
            if (proxyConsumer != null) {
                try {
                    proxyConsumer.disconnect_push_consumer();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
}
