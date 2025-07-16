package com.metricstream.eventchannel

import com.metricstream.event.BooleanHolder
import com.metricstream.event.Disconnected
import com.metricstream.event.PullConsumer
import com.metricstream.event.PullSupplier
import com.metricstream.event.PushConsumer
import com.metricstream.event.PushSupplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private val logger = KotlinLogging.logger {}

/**
 * Implementation of the EventChannel interface.
 * Provides a thread-safe event channel using Kotlin coroutines for asynchronous event processing.
 */
class EventChannelImpl : EventChannel {
    private val consumerAdmin = ConsumerAdminImpl(this)
    private val supplierAdmin = SupplierAdminImpl(this)
    private val destroyed = AtomicBoolean(false)
    
    // Coroutine scope for event dispatching
    private val eventScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Event queue for pull model
    private val eventQueue = LinkedBlockingQueue<Any>()
    
    // Lists of proxies for event distribution
    private val pushConsumerProxies = CopyOnWriteArrayList<ProxyPushConsumerImpl>()
    private val pushSupplierProxies = CopyOnWriteArrayList<ProxyPushSupplierImpl>()
    private val pullConsumerProxies = CopyOnWriteArrayList<ProxyPullConsumerImpl>()
    private val pullSupplierProxies = CopyOnWriteArrayList<ProxyPullSupplierImpl>()
    
    /**
     * Checks if this channel has been destroyed.
     * @throws IllegalStateException if the channel has been destroyed
     */
    private fun checkDestroyed() {
        if (destroyed.get()) {
            throw IllegalStateException("Event channel has been destroyed")
        }
    }
    
    /**
     * Adds a proxy push consumer to the list.
     * @param proxy The proxy to add
     */
    internal fun addProxyPushConsumer(proxy: ProxyPushConsumerImpl) {
        checkDestroyed()
        pushConsumerProxies.add(proxy)
    }
    
    /**
     * Adds a proxy push supplier to the list.
     * @param proxy The proxy to add
     */
    internal fun addProxyPushSupplier(proxy: ProxyPushSupplierImpl) {
        checkDestroyed()
        pushSupplierProxies.add(proxy)
    }
    
    /**
     * Adds a proxy pull consumer to the list.
     * @param proxy The proxy to add
     */
    internal fun addProxyPullConsumer(proxy: ProxyPullConsumerImpl) {
        checkDestroyed()
        pullConsumerProxies.add(proxy)
    }
    
    /**
     * Adds a proxy pull supplier to the list.
     * @param proxy The proxy to add
     */
    internal fun addProxyPullSupplier(proxy: ProxyPullSupplierImpl) {
        checkDestroyed()
        pullSupplierProxies.add(proxy)
    }
    
    /**
     * Removes a proxy push consumer from the list.
     * @param proxy The proxy to remove
     */
    internal fun removeProxyPushConsumer(proxy: ProxyPushConsumerImpl) {
        pushConsumerProxies.remove(proxy)
    }
    
    /**
     * Removes a proxy push supplier from the list.
     * @param proxy The proxy to remove
     */
    internal fun removeProxyPushSupplier(proxy: ProxyPushSupplierImpl) {
        pushSupplierProxies.remove(proxy)
    }
    
    /**
     * Removes a proxy pull consumer from the list.
     * @param proxy The proxy to remove
     */
    internal fun removeProxyPullConsumer(proxy: ProxyPullConsumerImpl) {
        pullConsumerProxies.remove(proxy)
    }
    
    /**
     * Removes a proxy pull supplier from the list.
     * @param proxy The proxy to remove
     */
    internal fun removeProxyPullSupplier(proxy: ProxyPullSupplierImpl) {
        pullSupplierProxies.remove(proxy)
    }
    
    /**
     * Pushes an event to all connected push suppliers.
     * @param event The event to push
     */
    internal fun pushEvent(event: Any) {
        checkDestroyed()
        logger.debug { "Pushing event: $event" }
        
        // Add to event queue for pull model
        eventQueue.offer(event)
        
        // Push to all connected push consumers asynchronously
        pushSupplierProxies.forEach { proxy ->
            eventScope.launch {
                try {
                    proxy.pushToConsumer(event)
                } catch (e: Exception) {
                    logger.warn(e) { "Error pushing event to consumer" }
                }
            }
        }
    }
    
    /**
     * Pulls an event from the event queue.
     * @return The event, or null if no event is available
     */
    internal fun pullEvent(): Any? {
        checkDestroyed()
        return eventQueue.poll()
    }
    
    /**
     * Tries to pull an event from the event queue with a timeout.
     * @param timeout The timeout in milliseconds
     * @return The event, or null if no event is available within the timeout
     */
    internal fun tryPullEvent(timeout: Long): Any? {
        checkDestroyed()
        return try {
            eventQueue.poll(timeout, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            null
        }
    }
    
    override fun for_consumers(): ConsumerAdmin {
        checkDestroyed()
        return consumerAdmin
    }
    
    override fun for_suppliers(): SupplierAdmin {
        checkDestroyed()
        return supplierAdmin
    }
    
    override fun destroy() {
        if (destroyed.getAndSet(true)) {
            return
        }
        
        logger.debug { "Destroying event channel" }
        
        // Cancel all coroutines
        eventScope.cancel()
        
        // Disconnect all proxies
        pushConsumerProxies.forEach { it.disconnect() }
        pushSupplierProxies.forEach { it.disconnect() }
        pullConsumerProxies.forEach { it.disconnect() }
        pullSupplierProxies.forEach { it.disconnect() }
        
        // Clear all lists
        pushConsumerProxies.clear()
        pushSupplierProxies.clear()
        pullConsumerProxies.clear()
        pullSupplierProxies.clear()
        
        // Clear event queue
        eventQueue.clear()
    }
    
    /**
     * Implementation of the ConsumerAdmin interface.
     */
    private class ConsumerAdminImpl(private val channel: EventChannelImpl) : ConsumerAdmin {
        override fun obtain_push_supplier(): ProxyPushSupplier {
            channel.checkDestroyed()
            val proxy = ProxyPushSupplierImpl(channel)
            channel.addProxyPushSupplier(proxy)
            return proxy
        }
        
        override fun obtain_pull_supplier(): ProxyPullSupplier {
            channel.checkDestroyed()
            val proxy = ProxyPullSupplierImpl(channel)
            channel.addProxyPullSupplier(proxy)
            return proxy
        }
    }
    
    /**
     * Implementation of the SupplierAdmin interface.
     */
    private class SupplierAdminImpl(private val channel: EventChannelImpl) : SupplierAdmin {
        override fun obtain_push_consumer(): ProxyPushConsumer {
            channel.checkDestroyed()
            val proxy = ProxyPushConsumerImpl(channel)
            channel.addProxyPushConsumer(proxy)
            return proxy
        }
        
        override fun obtain_pull_consumer(): ProxyPullConsumer {
            channel.checkDestroyed()
            val proxy = ProxyPullConsumerImpl(channel)
            channel.addProxyPullConsumer(proxy)
            return proxy
        }
    }
    
    /**
     * Implementation of the ProxyPushConsumer interface.
     */
    class ProxyPushConsumerImpl(private val channel: EventChannelImpl) : ProxyPushConsumer {
        private val supplier = AtomicReference<PushSupplier?>()
        private val connected = AtomicBoolean(false)
        private val disconnected = AtomicBoolean(false)
        
        /**
         * Disconnects this proxy.
         */
        internal fun disconnect() {
            if (disconnected.getAndSet(true)) {
                return
            }
            
            val currentSupplier = supplier.getAndSet(null)
            currentSupplier?.disconnect_push_supplier()
            connected.set(false)
            channel.removeProxyPushConsumer(this)
        }
        
        override fun connect_push_supplier(pushSupplier: PushSupplier?) {
            if (disconnected.get()) {
                throw Disconnected()
            }
            
            if (connected.getAndSet(true)) {
                throw AlreadyConnected()
            }
            
            supplier.set(pushSupplier)
        }
        
        override fun push(data: Any) {
            if (disconnected.get()) {
                throw Disconnected()
            }
            
            channel.pushEvent(data)
        }
        
        override fun disconnect_push_consumer() {
            disconnect()
        }
    }
    
    /**
     * Implementation of the ProxyPushSupplier interface.
     */
    class ProxyPushSupplierImpl(private val channel: EventChannelImpl) : ProxyPushSupplier {
        private val consumer = AtomicReference<PushConsumer?>()
        private val connected = AtomicBoolean(false)
        private val disconnected = AtomicBoolean(false)
        
        /**
         * Disconnects this proxy.
         */
        internal fun disconnect() {
            if (disconnected.getAndSet(true)) {
                return
            }
            
            val currentConsumer = consumer.getAndSet(null)
            currentConsumer?.disconnect_push_consumer()
            connected.set(false)
            channel.removeProxyPushSupplier(this)
        }
        
        /**
         * Pushes an event to the connected consumer.
         * @param data The event data
         */
        internal fun pushToConsumer(data: Any) {
            if (disconnected.get()) {
                return
            }
            
            val currentConsumer = consumer.get() ?: return
            
            try {
                currentConsumer.push(data)
            } catch (e: Disconnected) {
                disconnect()
            } catch (e: Exception) {
                logger.warn(e) { "Error pushing event to consumer" }
            }
        }
        
        override fun connect_push_consumer(pushConsumer: PushConsumer?) {
            if (disconnected.get()) {
                throw Disconnected()
            }
            
            if (connected.getAndSet(true)) {
                throw AlreadyConnected()
            }
            
            consumer.set(pushConsumer)
        }
        
        override fun disconnect_push_supplier() {
            disconnect()
        }
    }
    
    /**
     * Implementation of the ProxyPullConsumer interface.
     */
    class ProxyPullConsumerImpl(private val channel: EventChannelImpl) : ProxyPullConsumer {
        private val supplier = AtomicReference<PullSupplier?>()
        private val connected = AtomicBoolean(false)
        private val disconnected = AtomicBoolean(false)
        private val pullScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        private var pullJob: Job? = null
        
        /**
         * Disconnects this proxy.
         */
        internal fun disconnect() {
            if (disconnected.getAndSet(true)) {
                return
            }
            
            pullJob?.cancel()
            pullScope.cancel()
            
            val currentSupplier = supplier.getAndSet(null)
            currentSupplier?.disconnect_pull_supplier()
            connected.set(false)
            channel.removeProxyPullConsumer(this)
        }
        
        /**
         * Starts pulling events from the supplier.
         */
        private fun startPulling() {
            pullJob = pullScope.launch {
                while (isActive) {
                    val currentSupplier = supplier.get() ?: break
                    
                    try {
                        val hasEvent = BooleanHolder()
                        val event = currentSupplier.try_pull(hasEvent)
                        
                        if (hasEvent.value && event != null) {
                            channel.pushEvent(event)
                        }
                    } catch (e: Disconnected) {
                        disconnect()
                        break
                    } catch (e: Exception) {
                        logger.warn(e) { "Error pulling event from supplier" }
                    }
                    
                    delay(100) // Poll every 100ms
                }
            }
        }
        
        override fun connect_pull_supplier(pullSupplier: PullSupplier?) {
            if (disconnected.get()) {
                throw Disconnected()
            }
            
            if (connected.getAndSet(true)) {
                throw AlreadyConnected()
            }
            
            supplier.set(pullSupplier)
            
            if (pullSupplier != null) {
                startPulling()
            }
        }
        
        override fun disconnect_pull_consumer() {
            disconnect()
        }
    }
    
    /**
     * Implementation of the ProxyPullSupplier interface.
     */
    class ProxyPullSupplierImpl(private val channel: EventChannelImpl) : ProxyPullSupplier {
        private val consumer = AtomicReference<PullConsumer?>()
        private val connected = AtomicBoolean(false)
        private val disconnected = AtomicBoolean(false)
        
        /**
         * Disconnects this proxy.
         */
        internal fun disconnect() {
            if (disconnected.getAndSet(true)) {
                return
            }
            
            val currentConsumer = consumer.getAndSet(null)
            currentConsumer?.disconnect_pull_consumer()
            connected.set(false)
            channel.removeProxyPullSupplier(this)
        }
        
        override fun connect_pull_consumer(pullConsumer: PullConsumer?) {
            if (disconnected.get()) {
                throw Disconnected()
            }
            
            if (connected.getAndSet(true)) {
                throw AlreadyConnected()
            }
            
            consumer.set(pullConsumer)
        }
        
        override fun pull(): Any {
            if (disconnected.get()) {
                throw Disconnected()
            }
            
            // Block until an event is available
            return channel.eventQueue.take()
        }
        
        override fun try_pull(hasEvent: BooleanHolder): Any? {
            if (disconnected.get()) {
                throw Disconnected()
            }
            
            val event = channel.pullEvent()
            hasEvent.value = event != null
            return event
        }
        
        override fun disconnect_pull_supplier() {
            disconnect()
        }
    }
}
