package com.metricstream.omg.event;

/**
 * Interface for a pull consumer, which requests events from suppliers.
 * This is the non-CORBA equivalent of org.omg.CosEventComm.PullConsumer.
 */
public interface PullConsumer {
    
    /**
     * Notifies the consumer that it has been disconnected from the event channel.
     */
    void disconnect_pull_consumer();
}
