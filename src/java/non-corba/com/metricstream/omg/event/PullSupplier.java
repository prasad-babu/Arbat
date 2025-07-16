package com.metricstream.omg.event;

/**
 * Interface for a pull supplier, which provides events when requested by consumers.
 * This is the non-CORBA equivalent of org.omg.CosEventComm.PullSupplier.
 */
public interface PullSupplier {
    
    /**
     * Tries to get an event without blocking.
     * 
     * @return The event data, or null if no event is available
     * @throws Disconnected If the supplier is disconnected
     */
    Object try_pull(BooleanHolder has_event) throws Disconnected;
    
    /**
     * Gets an event, blocking if necessary until one is available.
     * 
     * @return The event data
     * @throws Disconnected If the supplier is disconnected
     */
    Object pull() throws Disconnected;
    
    /**
     * Notifies the supplier that it has been disconnected from the event channel.
     */
    void disconnect_pull_supplier();
}
