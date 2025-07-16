package com.metricstream.omg.naming;

/**
 * Factory class for creating and accessing naming service components.
 * This replaces the CORBA ORB initialization and initial references for the naming service.
 */
public class NamingServiceFactory {
    
    private static NamingServiceFactory instance;
    private NamingContext rootContext;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private NamingServiceFactory() {
        // Create the root naming context
        rootContext = new NamingContextImpl();
    }
    
    /**
     * Gets the singleton instance of the factory.
     * 
     * @return The factory instance
     */
    public static synchronized NamingServiceFactory getInstance() {
        if (instance == null) {
            instance = new NamingServiceFactory();
        }
        return instance;
    }
    
    /**
     * Gets the root naming context.
     * 
     * @return The root naming context
     */
    public NamingContext getRootContext() {
        return rootContext;
    }
    
    /**
     * Creates a new naming context.
     * 
     * @return A new naming context
     */
    public NamingContext createNamingContext() {
        return new NamingContextImpl();
    }
    
    /**
     * Resets the naming service, clearing all bindings.
     * This is primarily useful for testing.
     */
    public void reset() {
        rootContext = new NamingContextImpl();
    }
}
