package com.metricstream.event

/**
 * Exception thrown when a component is disconnected.
 * Equivalent to CosEventComm::Disconnected in CORBA.
 */
class Disconnected : Exception("Component has been disconnected")

/**
 * Interface for a push-model consumer.
 * Equivalent to CosEventComm::PushConsumer in CORBA.
 */
interface PushConsumer {
    /**
     * Receives an event from a supplier.
     * 
     * @param data The event data
     * @throws Disconnected if the consumer has been disconnected
     */
    @Throws(Disconnected::class)
    fun push(data: Any)
    
    /**
     * Disconnects the push consumer.
     */
    fun disconnect_push_consumer()
}

/**
 * Interface for a push-model supplier.
 * Equivalent to CosEventComm::PushSupplier in CORBA.
 */
interface PushSupplier {
    /**
     * Disconnects the push supplier.
     */
    fun disconnect_push_supplier()
}

/**
 * Interface for a pull-model consumer.
 * Equivalent to CosEventComm::PullConsumer in CORBA.
 */
interface PullConsumer {
    /**
     * Disconnects the pull consumer.
     */
    fun disconnect_pull_consumer()
}

/**
 * Interface for a pull-model supplier.
 * Equivalent to CosEventComm::PullSupplier in CORBA.
 */
interface PullSupplier {
    /**
     * Provides an event to a consumer.
     * 
     * @return The event data
     * @throws Disconnected if the supplier has been disconnected
     */
    @Throws(Disconnected::class)
    fun pull(): Any
    
    /**
     * Tries to provide an event to a consumer.
     * 
     * @param hasEvent Set to true if an event is available
     * @return The event data, or null if no event is available
     * @throws Disconnected if the supplier has been disconnected
     */
    @Throws(Disconnected::class)
    fun try_pull(hasEvent: BooleanHolder): Any?
    
    /**
     * Disconnects the pull supplier.
     */
    fun disconnect_pull_supplier()
}

/**
 * Holder class for boolean values, used for out parameters.
 * Equivalent to CORBA's BooleanHolder.
 */
class BooleanHolder(var value: Boolean = false)
