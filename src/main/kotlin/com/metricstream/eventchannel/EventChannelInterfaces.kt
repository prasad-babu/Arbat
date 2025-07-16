package com.metricstream.eventchannel

import com.metricstream.event.PushConsumer
import com.metricstream.event.PushSupplier
import com.metricstream.event.PullConsumer
import com.metricstream.event.PullSupplier
import com.metricstream.event.Disconnected

/**
 * Exception thrown when a component is already connected.
 * Equivalent to CosEventChannelAdmin::AlreadyConnected in CORBA.
 */
class AlreadyConnected : Exception("Component is already connected")

/**
 * Exception thrown when there is a type mismatch.
 * Equivalent to CosEventChannelAdmin::TypeError in CORBA.
 */
class TypeError : Exception("Type mismatch")

/**
 * Interface for a proxy push consumer.
 * Equivalent to CosEventChannelAdmin::ProxyPushConsumer in CORBA.
 */
interface ProxyPushConsumer : PushConsumer {
    /**
     * Connects a push supplier to this proxy.
     * 
     * @param pushSupplier The push supplier to connect
     * @throws AlreadyConnected if a supplier is already connected
     * @throws TypeError if there is a type mismatch
     */
    @Throws(AlreadyConnected::class, TypeError::class)
    fun connect_push_supplier(pushSupplier: PushSupplier?)
}

/**
 * Interface for a proxy pull supplier.
 * Equivalent to CosEventChannelAdmin::ProxyPullSupplier in CORBA.
 */
interface ProxyPullSupplier : PullSupplier {
    /**
     * Connects a pull consumer to this proxy.
     * 
     * @param pullConsumer The pull consumer to connect
     * @throws AlreadyConnected if a consumer is already connected
     * @throws TypeError if there is a type mismatch
     */
    @Throws(AlreadyConnected::class, TypeError::class)
    fun connect_pull_consumer(pullConsumer: PullConsumer?)
}

/**
 * Interface for a proxy pull consumer.
 * Equivalent to CosEventChannelAdmin::ProxyPullConsumer in CORBA.
 */
interface ProxyPullConsumer : PullConsumer {
    /**
     * Connects a pull supplier to this proxy.
     * 
     * @param pullSupplier The pull supplier to connect
     * @throws AlreadyConnected if a supplier is already connected
     * @throws TypeError if there is a type mismatch
     */
    @Throws(AlreadyConnected::class, TypeError::class)
    fun connect_pull_supplier(pullSupplier: PullSupplier?)
}

/**
 * Interface for a proxy push supplier.
 * Equivalent to CosEventChannelAdmin::ProxyPushSupplier in CORBA.
 */
interface ProxyPushSupplier : PushSupplier {
    /**
     * Connects a push consumer to this proxy.
     * 
     * @param pushConsumer The push consumer to connect
     * @throws AlreadyConnected if a consumer is already connected
     * @throws TypeError if there is a type mismatch
     */
    @Throws(AlreadyConnected::class, TypeError::class)
    fun connect_push_consumer(pushConsumer: PushConsumer?)
}

/**
 * Interface for a consumer administration object.
 * Equivalent to CosEventChannelAdmin::ConsumerAdmin in CORBA.
 */
interface ConsumerAdmin {
    /**
     * Obtains a proxy push supplier.
     * 
     * @return A proxy push supplier
     */
    fun obtain_push_supplier(): ProxyPushSupplier
    
    /**
     * Obtains a proxy pull supplier.
     * 
     * @return A proxy pull supplier
     */
    fun obtain_pull_supplier(): ProxyPullSupplier
}

/**
 * Interface for a supplier administration object.
 * Equivalent to CosEventChannelAdmin::SupplierAdmin in CORBA.
 */
interface SupplierAdmin {
    /**
     * Obtains a proxy push consumer.
     * 
     * @return A proxy push consumer
     */
    fun obtain_push_consumer(): ProxyPushConsumer
    
    /**
     * Obtains a proxy pull consumer.
     * 
     * @return A proxy pull consumer
     */
    fun obtain_pull_consumer(): ProxyPullConsumer
}

/**
 * Interface for an event channel.
 * Equivalent to CosEventChannelAdmin::EventChannel in CORBA.
 */
interface EventChannel {
    /**
     * Gets the consumer administration object.
     * 
     * @return The consumer admin
     */
    fun for_consumers(): ConsumerAdmin
    
    /**
     * Gets the supplier administration object.
     * 
     * @return The supplier admin
     */
    fun for_suppliers(): SupplierAdmin
    
    /**
     * Destroys the event channel.
     */
    fun destroy()
}
