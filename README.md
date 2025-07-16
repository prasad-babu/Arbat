# Arbat
Light Java CORBA services

## Non-CORBA Implementation

This project provides a pure Java implementation of CORBA services without requiring a CORBA ORB. The implementation includes:

1. **Naming Service**: A complete implementation of the CosNaming service
2. **Event Service**: A complete implementation of the CosEventComm and CosEventChannelAdmin services

## Features

- **Thread-safe**: All implementations use Java concurrency utilities for thread safety
- **Asynchronous Event Processing**: Event delivery uses ExecutorService for asynchronous processing
- **Factory Pattern**: Factory classes replace CORBA ORB initial references
- **Migration Support**: Helper classes for migrating from CORBA to non-CORBA implementation
- **Comprehensive Testing**: Full test coverage for all components
- **Example Applications**: Demonstration of both push and pull event models

## Getting Started

### Building the Project

```bash
ant build
```

### Running Tests

```bash
ant run-tests
```

### Running Examples

```bash
ant run-examples
```

### Generating Documentation

```bash
ant javadoc
```

### Creating Distribution Package

```bash
ant dist
```

## Package Structure

- `com.metricstream.omg.naming`: Naming service interfaces and classes (replaces CosNaming)
- `com.metricstream.omg.event`: Event communication interfaces (replaces CosEventComm)
- `com.metricstream.omg.eventchannel`: Event channel administration interfaces (replaces CosEventChannelAdmin)
- `com.metricstream.omg.util`: Utility classes for migration and type handling
- `com.metricstream.omg.examples`: Example applications demonstrating usage
- `com.metricstream.omg.test`: Test classes for verifying functionality

## Migration from CORBA

See the [Migration Guide](MIGRATION_GUIDE.md) for detailed instructions on migrating from CORBA to this non-CORBA implementation.

## Examples

### Naming Service

```java
// Get the naming service factory
NamingServiceFactory namingFactory = NamingServiceFactory.getInstance();

// Get the root naming context
NamingContext rootContext = namingFactory.getRootContext();

// Create a name
Name name = new Name();
name.addComponent(new NameComponent("MyService", ""));

// Bind an object to the name
MyService service = new MyServiceImpl();
rootContext.bind(name, service);

// Resolve the name to get the object
Object obj = rootContext.resolve(name);
MyService resolvedService = (MyService) obj;
```

### Event Channel

```java
// Get the event channel factory
EventChannelFactory eventFactory = EventChannelFactory.getInstance();

// Create an event channel
EventChannel eventChannel = eventFactory.createEventChannel();

// Get the consumer admin
ConsumerAdmin consumerAdmin = eventChannel.for_consumers();

// Get a proxy push supplier
ProxyPushSupplier proxySupplier = consumerAdmin.obtain_push_supplier();

// Connect a push consumer
PushConsumer pushConsumer = new MyPushConsumer();
proxySupplier.connect_push_consumer(pushConsumer);

// Get the supplier admin
SupplierAdmin supplierAdmin = eventChannel.for_suppliers();

// Get a proxy push consumer
ProxyPushConsumer proxyConsumer = supplierAdmin.obtain_push_consumer();

// Connect a push supplier
PushSupplier pushSupplier = new MyPushSupplier();
proxyConsumer.connect_push_supplier(pushSupplier);

// Push an event
proxyConsumer.push("Hello, World!");
```

## License

This project is licensed under the terms of the license included in the repository.
