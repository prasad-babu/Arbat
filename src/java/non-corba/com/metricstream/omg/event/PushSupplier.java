package com.metricstream.omg.event;

/**
 * Interface for a push supplier, which pushes events to consumers.
 * This is the non-CORBA equivalent of org.omg.CosEventComm.PushSupplier.
 */
public interface PushSupplier {
    
    /**
     * Notifies the supplier that it has been disconnected from the event channel.
     */
    void disconnect_push_supplier();
}
