package com.metricstream.examples

import com.metricstream.event.BooleanHolder
import com.metricstream.event.Disconnected
import com.metricstream.event.PullConsumer
import com.metricstream.event.PullSupplier
import com.metricstream.eventchannel.EventChannelFactory
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

/**
 * Example demonstrating the pull model for event communication.
 */
fun main() {
    logger.info { "Starting Pull Event Example" }
    
    // Get the event channel factory
    val factory = EventChannelFactory.getInstance()
    
    // Create an event channel and register it with a name
    val channelName = "PullExampleChannel"
    val eventChannel = factory.createEventChannel(channelName)
    
    logger.info { "Created event channel: $channelName" }
    
    // Create a consumer admin and a supplier admin
    val consumerAdmin = eventChannel.for_consumers()
    val supplierAdmin = eventChannel.for_suppliers()
    
    // Create a latch to wait for events
    val eventLatch = CountDownLatch(3)
    
    // Create a counter for events
    val eventCounter = AtomicInteger(0)
    
    // Create a pull supplier that will generate events
    val supplier = object : PullSupplier {
        private val events = listOf("Event 1", "Event 2", "Event 3")
        private val eventIndex = AtomicInteger(0)
        private var disconnected = false
        
        override fun pull(): Any {
            if (disconnected) {
                throw Disconnected()
            }
            
            // Simulate some processing time
            Thread.sleep(500)
            
            val index = eventIndex.getAndIncrement()
            if (index < events.size) {
                logger.info { "Supplier providing event: ${events[index]}" }
                return events[index]
            } else {
                logger.info { "No more events, waiting..." }
                Thread.sleep(1000)
                throw Disconnected()
            }
        }
        
        override fun try_pull(hasEvent: BooleanHolder): Any? {
            if (disconnected) {
                throw Disconnected()
            }
            
            val index = eventIndex.get()
            if (index < events.size) {
                hasEvent.value = true
                return pull()
            } else {
                hasEvent.value = false
                return null
            }
        }
        
        override fun disconnect_pull_supplier() {
            logger.info { "Pull supplier disconnected" }
            disconnected = true
        }
    }
    
    // Create a pull consumer
    val consumer = object : PullConsumer {
        override fun disconnect_pull_consumer() {
            logger.info { "Pull consumer disconnected" }
        }
    }
    
    // Get proxies
    val proxyPullSupplier = consumerAdmin.obtain_pull_supplier()
    val proxyPullConsumer = supplierAdmin.obtain_pull_consumer()
    
    // Connect the consumer and supplier to the proxies
    logger.info { "Connecting consumer and supplier" }
    proxyPullSupplier.connect_pull_consumer(consumer)
    proxyPullConsumer.connect_pull_supplier(supplier)
    
    // Create a coroutine to pull events
    val pullJob = CoroutineScope(Dispatchers.Default).launch {
        try {
            // Pull events using try_pull
            while (isActive && eventCounter.get() < 3) {
                val hasEvent = BooleanHolder()
                val event = proxyPullSupplier.try_pull(hasEvent)
                
                if (hasEvent.value && event != null) {
                    logger.info { "Consumer received event: $event" }
                    eventCounter.incrementAndGet()
                    eventLatch.countDown()
                } else {
                    logger.info { "No event available, waiting..." }
                    delay(200)
                }
            }
            
            // Try a blocking pull as well
            try {
                val event = proxyPullSupplier.pull()
                logger.info { "Consumer received event via blocking pull: $event" }
            } catch (e: Disconnected) {
                logger.info { "Pull supplier disconnected as expected" }
            }
            
        } catch (e: Exception) {
            logger.error(e) { "Error in pull consumer" }
        }
    }
    
    // Wait for all events to be received
    val allReceived = eventLatch.await(10, TimeUnit.SECONDS)
    if (allReceived) {
        logger.info { "All events were received successfully" }
    } else {
        logger.warn { "Not all events were received within the timeout" }
    }
    
    // Cancel the pull job
    runBlocking {
        pullJob.cancelAndJoin()
    }
    
    // Disconnect and clean up
    logger.info { "Disconnecting and cleaning up" }
    proxyPullSupplier.disconnect_pull_supplier()
    proxyPullConsumer.disconnect_pull_consumer()
    
    // Destroy the event channel
    eventChannel.destroy()
    
    // Unregister the event channel
    factory.unregisterEventChannel(channelName)
    
    logger.info { "Pull Event Example completed" }
}
