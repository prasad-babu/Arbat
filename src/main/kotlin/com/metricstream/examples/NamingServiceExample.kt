package com.metricstream.examples

import com.metricstream.naming.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Example demonstrating the usage of the naming service implementation.
 */
fun main() {
    logger.info { "Starting Naming Service Example" }
    
    // Get the naming service factory
    val factory = NamingServiceFactory.getInstance()
    
    // Get the root naming context
    val rootContext = factory.getRootContext()
    logger.info { "Obtained root naming context" }
    
    // Create some objects to bind
    val testObject1 = "Test Object 1"
    val testObject2 = "Test Object 2"
    val testObject3 = HashMap<String, Int>().apply {
        put("one", 1)
        put("two", 2)
        put("three", 3)
    }
    
    // Create names for the objects
    val name1 = Name().addComponent(NameComponent("TestObject", "1"))
    val name2 = Name().addComponent(NameComponent("TestObject", "2"))
    val name3 = Name()
        .addComponent(NameComponent("TestFolder", ""))
        .addComponent(NameComponent("TestObject", "3"))
    
    logger.info { "Created names: $name1, $name2, $name3" }
    
    // Bind the objects to the names
    logger.info { "Binding objects to names" }
    rootContext.bind(name1, testObject1)
    rootContext.bind(name2, testObject2)
    
    // Create a new context for the third object
    val subContext = factory.createNamingContext()
    val contextName = Name().addComponent(NameComponent("TestFolder", ""))
    rootContext.bind_context(contextName, subContext)
    
    // Bind the third object to the sub-context
    val relativeName = Name().addComponent(NameComponent("TestObject", "3"))
    subContext.bind(relativeName, testObject3)
    
    // Resolve the objects
    logger.info { "Resolving objects" }
    val resolvedObject1 = rootContext.resolve(name1)
    val resolvedObject2 = rootContext.resolve(name2)
    val resolvedObject3 = rootContext.resolve(name3)
    
    logger.info { "Resolved object 1: $resolvedObject1" }
    logger.info { "Resolved object 2: $resolvedObject2" }
    logger.info { "Resolved object 3: $resolvedObject3" }
    
    // List the bindings in the root context
    logger.info { "Listing bindings in root context" }
    val bindingIterator = BooleanHolder()
    val bindings = rootContext.list(100, bindingIterator)
    
    bindings.forEach { binding ->
        logger.info { "Binding: ${binding.binding_name} -> ${binding.binding_type}" }
    }
    
    // Rebind an object
    logger.info { "Rebinding object 1" }
    val newObject1 = "New Test Object 1"
    rootContext.rebind(name1, newObject1)
    
    val resolvedNewObject1 = rootContext.resolve(name1)
    logger.info { "Resolved new object 1: $resolvedNewObject1" }
    
    // Unbind objects
    logger.info { "Unbinding objects" }
    rootContext.unbind(name1)
    rootContext.unbind(name2)
    
    // Try to resolve an unbound name (should throw NotFound)
    logger.info { "Trying to resolve unbound name" }
    try {
        rootContext.resolve(name1)
    } catch (e: NamingException) {
        logger.info { "Expected exception: ${e.message}" }
    }
    
    // Create a name from string and resolve
    logger.info { "Creating name from string" }
    val nameFromString = Name.fromString("TestFolder./TestObject.3")
    val resolvedFromString = rootContext.resolve(nameFromString)
    logger.info { "Resolved from string: $resolvedFromString" }
    
    // Use the string helper method from factory
    logger.info { "Using string helper method" }
    val resolvedWithHelper = factory.resolveString("TestFolder./TestObject.3")
    logger.info { "Resolved with helper: $resolvedWithHelper" }
    
    // Destroy the sub-context
    logger.info { "Destroying sub-context" }
    subContext.destroy()
    
    logger.info { "Naming Service Example completed" }
}
