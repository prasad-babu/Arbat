package com.metricstream.omg.eventchannel;

import com.metricstream.omg.event.PushConsumer;
import com.metricstream.omg.event.PushSupplier;
import com.metricstream.omg.event.Disconnected;

/**
 * Interface for a proxy push supplier, which receives events from the event channel and forwards them to consumers.
 * This is the non-CORBA equivalent of org.omg.CosEventChannelAdmin.ProxyPushSupplier.
 */
public interface ProxyPushSupplier extends PushSupplier {
    
    /**
     * Connects a push consumer to this proxy.
     * 
     * @param push_consumer The push consumer to connect
     * @throws AlreadyConnected If a consumer is already connected to this proxy
     */
    void connect_push_consumer(PushConsumer push_consumer) throws AlreadyConnected;
}
