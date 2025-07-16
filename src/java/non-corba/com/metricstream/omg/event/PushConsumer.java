package com.metricstream.omg.event;

/**
 * Interface for a push consumer, which receives events pushed by suppliers.
 * This is the non-CORBA equivalent of org.omg.CosEventComm.PushConsumer.
 */
public interface PushConsumer {
    
    /**
     * Receives an event pushed by a supplier.
     * 
     * @param data The event data
     * @throws Disconnected If the consumer is disconnected
     */
    void push(Object data) throws Disconnected;
    
    /**
     * Notifies the consumer that it has been disconnected from the event channel.
     */
    void disconnect_push_consumer();
}
