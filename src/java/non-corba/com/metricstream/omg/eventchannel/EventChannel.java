package com.metricstream.omg.eventchannel;

/**
 * Interface for an event channel, which decouples suppliers and consumers of events.
 * This is the non-CORBA equivalent of org.omg.CosEventChannelAdmin.EventChannel.
 */
public interface EventChannel {
    
    /**
     * Gets the consumer administration object for this event channel.
     * 
     * @return The consumer admin
     */
    ConsumerAdmin for_consumers();
    
    /**
     * Gets the supplier administration object for this event channel.
     * 
     * @return The supplier admin
     */
    SupplierAdmin for_suppliers();
    
    /**
     * Destroys this event channel.
     */
    void destroy();
}
