package com.metricstream.omg.eventchannel;

import com.metricstream.omg.event.PushConsumer;
import com.metricstream.omg.event.PushSupplier;
import com.metricstream.omg.event.Disconnected;

/**
 * Interface for a proxy push consumer, which receives events from suppliers and forwards them to the event channel.
 * This is the non-CORBA equivalent of org.omg.CosEventChannelAdmin.ProxyPushConsumer.
 */
public interface ProxyPushConsumer extends PushConsumer {
    
    /**
     * Connects a push supplier to this proxy.
     * 
     * @param push_supplier The push supplier to connect
     * @throws AlreadyConnected If a supplier is already connected to this proxy
     */
    void connect_push_supplier(PushSupplier push_supplier) throws AlreadyConnected;
}
