package com.metricstream.omg.test;

import com.metricstream.omg.event.*;
import com.metricstream.omg.eventchannel.*;
import com.metricstream.omg.naming.*;
import com.metricstream.omg.util.MigrationHelper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive test class for verifying the functionality of the non-CORBA implementation.
 * Tests both naming service and event channel functionality.
 */
public class MigrationTest {

    private static final int WAIT_TIMEOUT_SECONDS = 5;

    public static void main(String[] args) {
        System.out.println("Starting MigrationTest...");
        
        try {
            // Test naming service functionality
            testNamingServiceBasic();
            testNamingServiceSubContexts();
            testNamingServiceListBindings();
            
            // Test event channel functionality
            testEventChannelPushModel();
            testEventChannelPullModel();
            
            // Test combined functionality
            testEventChannelWithNamingService();
            
            System.out.println("\nAll MigrationTest tests passed successfully!");
        } catch (Exception e) {
            System.err.println("\nMigrationTest failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tests basic naming service functionality: binding and resolving objects.
     */
    private static void testNamingServiceBasic() throws Exception {
        System.out.println("\n=== Testing Basic Naming Service Functionality ===");
        
        // Get the naming service factory
        NamingServiceFactory factory = NamingServiceFactory.getInstance();
        NamingContext rootContext = factory.getRootContext();
        
        // Create a test object to bind
        String testObject = "Test Object";
        
        // Create a name
        Name name = new Name();
        name.addComponent(new NameComponent("test", "object"));
        
        // Bind the object
        rootContext.bind(name, testObject);
        System.out.println("Bound test object to name 'test.object'");
        
        // Resolve the object
        Object resolvedObj = rootContext.resolve(name);
        
        if (!(resolvedObj instanceof String)) {
            throw new AssertionError("Resolved object is not a String");
        }
        
        String resolvedString = (String) resolvedObj;
        
        if (!resolvedString.equals(testObject)) {
            throw new AssertionError("Resolved object does not match the bound object");
        }
        
        System.out.println("Successfully resolved object: " + resolvedString);
        
        // Rebind the object
        String newTestObject = "New Test Object";
        rootContext.rebind(name, newTestObject);
        System.out.println("Rebound test object to name 'test.object'");
        
        // Resolve the object again
        resolvedObj = rootContext.resolve(name);
        
        if (!(resolvedObj instanceof String)) {
            throw new AssertionError("Resolved object is not a String");
        }
        
        resolvedString = (String) resolvedObj;
        
        if (!resolvedString.equals(newTestObject)) {
            throw new AssertionError("Resolved object does not match the rebound object");
        }
        
        System.out.println("Successfully resolved rebound object: " + resolvedString);
        
        // Unbind the object
        rootContext.unbind(name);
        System.out.println("Unbound test object");
        
        // Verify that the object is no longer bound
        try {
            rootContext.resolve(name);
            throw new AssertionError("Object is still bound after unbinding");
        } catch (NotFound e) {
            // Expected exception
            System.out.println("Verified object is no longer bound");
        }
        
        System.out.println("Basic naming service test passed");
    }
    
    /**
     * Tests naming service sub-contexts: creating, binding, and resolving in sub-contexts.
     */
    private static void testNamingServiceSubContexts() throws Exception {
        System.out.println("\n=== Testing Naming Service Sub-Contexts ===");
        
        // Get the naming service factory
        NamingServiceFactory factory = NamingServiceFactory.getInstance();
        NamingContext rootContext = factory.getRootContext();
        
        // Create a sub-context
        Name contextName = new Name();
        contextName.addComponent(new NameComponent("sub", "context"));
        
        NamingContext subContext = rootContext.bind_new_context(contextName);
        System.out.println("Created sub-context 'sub.context'");
        
        // Create a test object to bind in the sub-context
        String testObject = "Sub-Context Test Object";
        
        // Create a name for the object in the sub-context
        Name name = new Name();
        name.addComponent(new NameComponent("test", "object"));
        
        // Bind the object in the sub-context
        subContext.bind(name, testObject);
        System.out.println("Bound test object to name 'test.object' in sub-context");
        
        // Resolve the sub-context
        Object resolvedContext = rootContext.resolve(contextName);
        
        if (!(resolvedContext instanceof NamingContext)) {
            throw new AssertionError("Resolved object is not a NamingContext");
        }
        
        NamingContext resolvedSubContext = (NamingContext) resolvedContext;
        System.out.println("Successfully resolved sub-context");
        
        // Resolve the object in the sub-context
        Object resolvedObj = resolvedSubContext.resolve(name);
        
        if (!(resolvedObj instanceof String)) {
            throw new AssertionError("Resolved object is not a String");
        }
        
        String resolvedString = (String) resolvedObj;
        
        if (!resolvedString.equals(testObject)) {
            throw new AssertionError("Resolved object does not match the bound object");
        }
        
        System.out.println("Successfully resolved object in sub-context: " + resolvedString);
        
        // Resolve the object using a compound name
        Name compoundName = new Name();
        compoundName.addComponent(new NameComponent("sub", "context"));
        compoundName.addComponent(new NameComponent("test", "object"));
        
        resolvedObj = rootContext.resolve(compoundName);
        
        if (!(resolvedObj instanceof String)) {
            throw new AssertionError("Resolved object is not a String");
        }
        
        resolvedString = (String) resolvedObj;
        
        if (!resolvedString.equals(testObject)) {
            throw new AssertionError("Resolved object does not match the bound object");
        }
        
        System.out.println("Successfully resolved object using compound name: " + resolvedString);
        
        // Clean up
        subContext.unbind(name);
        rootContext.unbind(contextName);
        System.out.println("Cleaned up sub-context and objects");
        
        System.out.println("Naming service sub-contexts test passed");
    }
    
    /**
     * Tests naming service list_bindings functionality.
     */
    private static void testNamingServiceListBindings() throws Exception {
        System.out.println("\n=== Testing Naming Service List Bindings ===");
        
        // Get the naming service factory
        NamingServiceFactory factory = NamingServiceFactory.getInstance();
        NamingContext rootContext = factory.getRootContext();
        
        // Create and bind several test objects
        String testObject1 = "Test Object 1";
        String testObject2 = "Test Object 2";
        Integer testObject3 = 42;
        
        Name name1 = new Name();
        name1.addComponent(new NameComponent("test1", "object"));
        
        Name name2 = new Name();
        name2.addComponent(new NameComponent("test2", "object"));
        
        Name name3 = new Name();
        name3.addComponent(new NameComponent("test3", "object"));
        
        rootContext.bind(name1, testObject1);
        rootContext.bind(name2, testObject2);
        rootContext.bind(name3, testObject3);
        
        System.out.println("Bound three test objects");
        
        // Create a sub-context
        Name contextName = new Name();
        contextName.addComponent(new NameComponent("list", "context"));
        
        NamingContext subContext = rootContext.bind_new_context(contextName);
        System.out.println("Created sub-context 'list.context'");
        
        // List bindings
        BindingListHolder blHolder = new BindingListHolder();
        BindingIteratorHolder biHolder = new BindingIteratorHolder();
        
        rootContext.list(100, blHolder, biHolder);
        
        BindingList bindingList = blHolder.value;
        BindingIterator bindingIterator = biHolder.value;
        
        // Verify that we have at least 4 bindings (3 objects + 1 sub-context)
        if (bindingList.size() < 4) {
            throw new AssertionError("Expected at least 4 bindings, but got " + bindingList.size());
        }
        
        System.out.println("Found " + bindingList.size() + " bindings:");
        
        boolean foundTest1 = false;
        boolean foundTest2 = false;
        boolean foundTest3 = false;
        boolean foundSubContext = false;
        
        for (int i = 0; i < bindingList.size(); i++) {
            Binding binding = bindingList.get(i);
            Name bindingName = binding.binding_name;
            BindingType bindingType = binding.binding_type;
            
            String nameStr = "";
            for (int j = 0; j < bindingName.size(); j++) {
                NameComponent nc = bindingName.get(j);
                nameStr += nc.id;
                if (nc.kind != null && !nc.kind.isEmpty()) {
                    nameStr += "." + nc.kind;
                }
            }
            
            System.out.println("  " + nameStr + " (" + 
                              (bindingType == BindingType.ncontext ? "context" : "object") + ")");
            
            if (bindingName.size() == 1) {
                NameComponent nc = bindingName.get(0);
                if (nc.id.equals("test1") && nc.kind.equals("object")) {
                    foundTest1 = true;
                } else if (nc.id.equals("test2") && nc.kind.equals("object")) {
                    foundTest2 = true;
                } else if (nc.id.equals("test3") && nc.kind.equals("object")) {
                    foundTest3 = true;
                } else if (nc.id.equals("list") && nc.kind.equals("context")) {
                    foundSubContext = true;
                    if (bindingType != BindingType.ncontext) {
                        throw new AssertionError("Sub-context has wrong binding type");
                    }
                }
            }
        }
        
        if (!foundTest1 || !foundTest2 || !foundTest3 || !foundSubContext) {
            throw new AssertionError("Not all expected bindings were found");
        }
        
        System.out.println("Verified all expected bindings were found");
        
        // Clean up the binding iterator if it exists
        if (bindingIterator != null) {
            bindingIterator.destroy();
        }
        
        // Clean up
        rootContext.unbind(name1);
        rootContext.unbind(name2);
        rootContext.unbind(name3);
        rootContext.unbind(contextName);
        System.out.println("Cleaned up test objects and sub-context");
        
        System.out.println("Naming service list bindings test passed");
    }
    
    /**
     * Tests event channel push model functionality.
     */
    private static void testEventChannelPushModel() throws Exception {
        System.out.println("\n=== Testing Event Channel Push Model ===");
        
        // Get the event channel factory
        EventChannelFactory factory = EventChannelFactory.getInstance();
        
        // Create an event channel
        EventChannel channel = factory.createEventChannel();
        System.out.println("Created event channel");
        
        // Create a test consumer
        TestPushConsumer consumer = new TestPushConsumer("TestConsumer");
        
        // Connect the consumer to the channel
        ConsumerAdmin consumerAdmin = channel.for_consumers();
        ProxyPushSupplier proxySupplier = consumerAdmin.obtain_push_supplier();
        proxySupplier.connect_push_consumer(consumer);
        System.out.println("Connected push consumer to event channel");
        
        // Create a test supplier
        TestPushSupplier supplier = new TestPushSupplier("TestSupplier");
        
        // Connect the supplier to the channel
        SupplierAdmin supplierAdmin = channel.for_suppliers();
        ProxyPushConsumer proxyConsumer = supplierAdmin.obtain_push_consumer();
        proxyConsumer.connect_push_supplier(supplier);
        System.out.println("Connected push supplier to event channel");
        
        // Push an event
        String eventData = "Test Event Data";
        supplier.sendEvent(eventData);
        System.out.println("Pushed event: " + eventData);
        
        // Wait for the event to be received
        if (!consumer.waitForEvent(WAIT_TIMEOUT_SECONDS)) {
            throw new AssertionError("Event was not received within the timeout period");
        }
        
        System.out.println("Event was successfully received");
        
        // Verify the event data
        if (!consumer.getLastEvent().equals(eventData)) {
            throw new AssertionError("Received event data does not match sent event data");
        }
        
        System.out.println("Verified received event data matches sent event data");
        
        // Test disconnection
        proxySupplier.disconnect_push_supplier();
        proxyConsumer.disconnect_push_consumer();
        System.out.println("Disconnected consumer and supplier");
        
        System.out.println("Event channel push model test passed");
    }
    
    /**
     * Tests event channel pull model functionality.
     */
    private static void testEventChannelPullModel() throws Exception {
        System.out.println("\n=== Testing Event Channel Pull Model ===");
        
        // Get the event channel factory
        EventChannelFactory factory = EventChannelFactory.getInstance();
        
        // Create an event channel
        EventChannel channel = factory.createEventChannel();
        System.out.println("Created event channel");
        
        // Create a test supplier
        TestPullSupplier supplier = new TestPullSupplier("TestPullSupplier");
        
        // Connect the supplier to the channel
        SupplierAdmin supplierAdmin = channel.for_suppliers();
        ProxyPullConsumer proxyConsumer = supplierAdmin.obtain_pull_consumer();
        proxyConsumer.connect_pull_supplier(supplier);
        System.out.println("Connected pull supplier to event channel");
        
        // Create a test consumer
        TestPullConsumer consumer = new TestPullConsumer("TestPullConsumer");
        
        // Connect the consumer to the channel
        ConsumerAdmin consumerAdmin = channel.for_consumers();
        ProxyPullSupplier proxySupplier = consumerAdmin.obtain_pull_supplier();
        proxySupplier.connect_pull_consumer(consumer);
        System.out.println("Connected pull consumer to event channel");
        
        // Set up test data
        String eventData = "Test Pull Event Data";
        supplier.setNextEvent(eventData);
        
        // Pull an event
        Object pulledData = proxySupplier.pull();
        System.out.println("Pulled event: " + pulledData);
        
        // Verify the event data
        if (!pulledData.equals(eventData)) {
            throw new AssertionError("Pulled event data does not match supplier event data");
        }
        
        System.out.println("Verified pulled event data matches supplier event data");
        
        // Test try_pull
        supplier.setNextEvent("Try Pull Event Data");
        BooleanHolder hasEvent = new BooleanHolder();
        Object tryPulledData = proxySupplier.try_pull(hasEvent);
        
        if (!hasEvent.value) {
            throw new AssertionError("try_pull reported no event available when one should be available");
        }
        
        System.out.println("try_pull successfully retrieved event: " + tryPulledData);
        
        // Test try_pull with no event available
        supplier.setHasEvent(false);
        tryPulledData = proxySupplier.try_pull(hasEvent);
        
        if (hasEvent.value) {
            throw new AssertionError("try_pull reported event available when none should be available");
        }
        
        System.out.println("try_pull correctly reported no event available");
        
        // Test disconnection
        proxySupplier.disconnect_pull_supplier();
        proxyConsumer.disconnect_pull_consumer();
        System.out.println("Disconnected consumer and supplier");
        
        System.out.println("Event channel pull model test passed");
    }
    
    /**
     * Tests combined functionality of naming service and event channel.
     */
    private static void testEventChannelWithNamingService() throws Exception {
        System.out.println("\n=== Testing Event Channel with Naming Service ===");
        
        // Get the factories
        EventChannelFactory channelFactory = EventChannelFactory.getInstance();
        NamingServiceFactory namingFactory = NamingServiceFactory.getInstance();
        NamingContext rootContext = namingFactory.getRootContext();
        
        // Create an event channel
        EventChannel channel = channelFactory.createEventChannel();
        System.out.println("Created event channel");
        
        // Register the event channel with the naming service
        Name channelName = MigrationHelper.createNameFromPath("EventChannel/Test");
        rootContext.bind(channelName, channel);
        System.out.println("Registered event channel with naming service as 'EventChannel/Test'");
        
        // Resolve the event channel from the naming service
        Object resolvedObj = rootContext.resolve(channelName);
        
        if (!(resolvedObj instanceof EventChannel)) {
            throw new AssertionError("Resolved object is not an EventChannel");
        }
        
        EventChannel resolvedChannel = (EventChannel) resolvedObj;
        System.out.println("Successfully resolved event channel from naming service");
        
        // Create a test consumer and supplier
        TestPushConsumer consumer = new TestPushConsumer("TestConsumer");
        TestPushSupplier supplier = new TestPushSupplier("TestSupplier");
        
        // Connect the consumer and supplier to the resolved channel
        ConsumerAdmin consumerAdmin = resolvedChannel.for_consumers();
        ProxyPushSupplier proxySupplier = consumerAdmin.obtain_push_supplier();
        proxySupplier.connect_push_consumer(consumer);
        System.out.println("Connected push consumer to resolved event channel");
        
        SupplierAdmin supplierAdmin = resolvedChannel.for_suppliers();
        ProxyPushConsumer proxyConsumer = supplierAdmin.obtain_push_consumer();
        proxyConsumer.connect_push_supplier(supplier);
        System.out.println("Connected push supplier to resolved event channel");
        
        // Push an event
        String eventData = "Test Event via Naming Service";
        supplier.sendEvent(eventData);
        System.out.println("Pushed event: " + eventData);
        
        // Wait for the event to be received
        if (!consumer.waitForEvent(WAIT_TIMEOUT_SECONDS)) {
            throw new AssertionError("Event was not received within the timeout period");
        }
        
        System.out.println("Event was successfully received");
        
        // Verify the event data
        if (!consumer.getLastEvent().equals(eventData)) {
            throw new AssertionError("Received event data does not match sent event data");
        }
        
        System.out.println("Verified received event data matches sent event data");
        
        // Clean up
        proxySupplier.disconnect_push_supplier();
        proxyConsumer.disconnect_push_consumer();
        rootContext.unbind(channelName);
        System.out.println("Cleaned up connections and unregistered event channel");
        
        System.out.println("Event channel with naming service test passed");
    }
    
    /**
     * Test implementation of PushConsumer for testing purposes.
     */
    static class TestPushConsumer implements PushConsumer {
        private final String name;
        private final CountDownLatch eventLatch = new CountDownLatch(1);
        private String lastEvent;
        
        public TestPushConsumer(String name) {
            this.name = name;
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
        }
    }
    
    /**
     * Test implementation of PullSupplier for testing purposes.
     */
    static class TestPullSupplier implements PullSupplier {
        private final String name;
        private String nextEvent = "Default Event";
        private boolean hasEvent = true;
        
        public TestPullSupplier(String name) {
            this.name = name;
        }
        
        public void setNextEvent(String nextEvent) {
            this.nextEvent = nextEvent;
            this.hasEvent = true;
        }
        
        public void setHasEvent(boolean hasEvent) {
            this.hasEvent = hasEvent;
        }
        
        @Override
        public Object pull() throws Disconnected {
            System.out.println(name + ": Providing event: " + nextEvent);
            return nextEvent;
        }
        
        @Override
        public Object try_pull(BooleanHolder hasEvent) throws Disconnected {
            hasEvent.value = this.hasEvent;
            
            if (this.hasEvent) {
                System.out.println(name + ": Providing event: " + nextEvent);
                return nextEvent;
            } else {
                System.out.println(name + ": No event available");
                return null;
            }
        }
        
        @Override
        public void disconnect_pull_supplier() {
            System.out.println(name + ": Disconnected");
        }
    }
    
    /**
     * Test implementation of PullConsumer for testing purposes.
     */
    static class TestPullConsumer implements PullConsumer {
        private final String name;
        
        public TestPullConsumer(String name) {
            this.name = name;
        }
        
        @Override
        public void disconnect_pull_consumer() {
            System.out.println(name + ": Disconnected");
        }
    }
}
