package com.metricstream.omg.eventchannel;

import com.metricstream.omg.event.PullConsumer;
import com.metricstream.omg.event.PullSupplier;
import com.metricstream.omg.event.Disconnected;

/**
 * Interface for a proxy pull consumer, which requests events from suppliers and forwards them to the event channel.
 * This is the non-CORBA equivalent of org.omg.CosEventChannelAdmin.ProxyPullConsumer.
 */
public interface ProxyPullConsumer extends PullConsumer {
    
    /**
     * Connects a pull supplier to this proxy.
     * 
     * @param pull_supplier The pull supplier to connect
     * @throws AlreadyConnected If a supplier is already connected to this proxy
     */
    void connect_pull_supplier(PullSupplier pull_supplier) throws AlreadyConnected;
}
