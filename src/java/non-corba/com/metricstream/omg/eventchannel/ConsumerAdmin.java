package com.metricstream.omg.eventchannel;

/**
 * Interface for the consumer administration object, which creates proxy suppliers for consumers.
 * This is the non-CORBA equivalent of org.omg.CosEventChannelAdmin.ConsumerAdmin.
 */
public interface ConsumerAdmin {
    
    /**
     * Gets a proxy push supplier for a consumer to connect to.
     * 
     * @return A proxy push supplier
     */
    ProxyPushSupplier obtain_push_supplier();
    
    /**
     * Gets a proxy pull supplier for a consumer to connect to.
     * 
     * @return A proxy pull supplier
     */
    ProxyPullSupplier obtain_pull_supplier();
}
