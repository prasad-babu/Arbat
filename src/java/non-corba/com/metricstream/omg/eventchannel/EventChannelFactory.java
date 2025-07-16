package com.metricstream.omg.eventchannel;

/**
 * Factory class for creating and accessing event channel components.
 * This replaces the CORBA ORB initialization and initial references for event channels.
 */
public class EventChannelFactory {
    
    private static EventChannelFactory instance;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private EventChannelFactory() {
        // Nothing to initialize
    }
    
    /**
     * Gets the singleton instance of the factory.
     * 
     * @return The factory instance
     */
    public static synchronized EventChannelFactory getInstance() {
        if (instance == null) {
            instance = new EventChannelFactory();
        }
        return instance;
    }
    
    /**
     * Creates a new event channel.
     * 
     * @return A new event channel
     */
    public EventChannel createEventChannel() {
        return new EventChannelImpl();
    }
}
