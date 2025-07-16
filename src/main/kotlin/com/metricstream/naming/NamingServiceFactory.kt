package com.metricstream.naming

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Factory for creating and accessing naming service components.
 * Replaces the CORBA ORB's resolve_initial_references functionality.
 */
class NamingServiceFactory private constructor() {
    private val rootContext = NamingContextImpl()
    
    /**
     * Gets the root naming context.
     * Equivalent to CORBA's resolve_initial_references("NameService").
     * 
     * @return The root naming context
     */
    fun getRootContext(): NamingContext {
        logger.debug { "Returning root naming context" }
        return rootContext
    }
    
    /**
     * Creates a new naming context.
     * 
     * @return A new naming context
     */
    fun createNamingContext(): NamingContext {
        logger.debug { "Creating new naming context" }
        return NamingContextImpl()
    }
    
    /**
     * Binds an object to a name in the root context.
     * 
     * @param name The name to bind
     * @param obj The object to bind
     */
    fun bind(name: Name, obj: Any) {
        rootContext.bind(name, obj)
    }
    
    /**
     * Resolves a name in the root context.
     * 
     * @param name The name to resolve
     * @return The object bound to the name
     */
    fun resolve(name: Name): Any {
        return rootContext.resolve(name)
    }
    
    /**
     * Resolves a name in the root context using a string representation.
     * Format: "id1.kind1/id2.kind2/id3.kind3"
     * 
     * @param nameStr The string representation of the name
     * @return The object bound to the name
     */
    fun resolveString(nameStr: String): Any {
        val name = Name.fromString(nameStr)
        return resolve(name)
    }
    
    companion object {
        private val INSTANCE = NamingServiceFactory()
        
        /**
         * Gets the singleton instance of the factory.
         * 
         * @return The factory instance
         */
        @JvmStatic
        fun getInstance(): NamingServiceFactory {
            return INSTANCE
        }
    }
}
