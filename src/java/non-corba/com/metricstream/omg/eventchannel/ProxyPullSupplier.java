package com.metricstream.omg.eventchannel;

import com.metricstream.omg.event.PullConsumer;
import com.metricstream.omg.event.PullSupplier;
import com.metricstream.omg.event.Disconnected;

/**
 * Interface for a proxy pull supplier, which provides events from the event channel when requested by consumers.
 * This is the non-CORBA equivalent of org.omg.CosEventChannelAdmin.ProxyPullSupplier.
 */
public interface ProxyPullSupplier extends PullSupplier {
    
    /**
     * Connects a pull consumer to this proxy.
     * 
     * @param pull_consumer The pull consumer to connect
     * @throws AlreadyConnected If a consumer is already connected to this proxy
     */
    void connect_pull_consumer(PullConsumer pull_consumer) throws AlreadyConnected;
}
