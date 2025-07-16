package com.metricstream.omg.eventchannel;

/**
 * Interface for the supplier administration object, which creates proxy consumers for suppliers.
 * This is the non-CORBA equivalent of org.omg.CosEventChannelAdmin.SupplierAdmin.
 */
public interface SupplierAdmin {
    
    /**
     * Gets a proxy push consumer for a supplier to connect to.
     * 
     * @return A proxy push consumer
     */
    ProxyPushConsumer obtain_push_consumer();
    
    /**
     * Gets a proxy pull consumer for a supplier to connect to.
     * 
     * @return A proxy pull consumer
     */
    ProxyPullConsumer obtain_pull_consumer();
}
