package com.metricstream.eventchannel

import com.metricstream.naming.Name
import com.metricstream.naming.NameComponent
import com.metricstream.naming.NamingServiceFactory
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Factory for creating and accessing event channels.
 * Replaces the CORBA ORB's resolve_initial_references functionality.
 */
class EventChannelFactory private constructor() {
    private val channels = ConcurrentHashMap<String, EventChannel>()
    private val namingFactory = NamingServiceFactory.getInstance()
    
    /**
     * Creates a new event channel.
     * 
     * @return A new event channel
     */
    fun createEventChannel(): EventChannel {
        logger.debug { "Creating new event channel" }
        return EventChannelImpl()
    }
    
    /**
     * Creates a new event channel and registers it with the naming service.
     * 
     * @param name The name to register the channel under
     * @return The new event channel
     */
    fun createEventChannel(name: String): EventChannel {
        logger.debug { "Creating new event channel with name: $name" }
        val channel = createEventChannel()
        registerEventChannel(name, channel)
        return channel
    }
    
    /**
     * Registers an event channel with the naming service.
     * 
     * @param name The name to register the channel under
     * @param channel The event channel to register
     */
    fun registerEventChannel(name: String, channel: EventChannel) {
        logger.debug { "Registering event channel with name: $name" }
        channels[name] = channel
        
        // Also register with the naming service
        val namingContext = namingFactory.getRootContext()
        val nameObj = Name().addComponent(NameComponent("EventChannel", name))
        
        try {
            namingContext.rebind(nameObj, channel)
        } catch (e: Exception) {
            logger.error(e) { "Error registering event channel with naming service" }
        }
    }
    
    /**
     * Looks up an event channel by name.
     * 
     * @param name The name of the channel
     * @return The event channel, or null if not found
     */
    fun lookupEventChannel(name: String): EventChannel? {
        logger.debug { "Looking up event channel with name: $name" }
        return channels[name]
    }
    
    /**
     * Unregisters an event channel.
     * 
     * @param name The name of the channel to unregister
     */
    fun unregisterEventChannel(name: String) {
        logger.debug { "Unregistering event channel with name: $name" }
        val channel = channels.remove(name)
        
        if (channel != null) {
            // Also unregister from the naming service
            val namingContext = namingFactory.getRootContext()
            val nameObj = Name().addComponent(NameComponent("EventChannel", name))
            
            try {
                namingContext.unbind(nameObj)
            } catch (e: Exception) {
                logger.error(e) { "Error unregistering event channel from naming service" }
            }
        }
    }
    
    companion object {
        private val INSTANCE = EventChannelFactory()
        
        /**
         * Gets the singleton instance of the factory.
         * 
         * @return The factory instance
         */
        @JvmStatic
        fun getInstance(): EventChannelFactory {
            return INSTANCE
        }
    }
}
