package com.metricstream.examples

import com.metricstream.event.Disconnected
import com.metricstream.event.PushConsumer
import com.metricstream.event.PushSupplier
import com.metricstream.eventchannel.EventChannelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * Example demonstrating the usage of the event channel implementation.
 */
fun main() {
    logger.info { "Starting Event Channel Example" }
    
    // Get the event channel factory
    val factory = EventChannelFactory.getInstance()
    
    // Create an event channel and register it with a name
    val channelName = "ExampleChannel"
    val eventChannel = factory.createEventChannel(channelName)
    
    logger.info { "Created event channel: $channelName" }
    
    // Create a consumer admin and a supplier admin
    val consumerAdmin = eventChannel.for_consumers()
    val supplierAdmin = eventChannel.for_suppliers()
    
    // Create a latch to wait for events
    val eventLatch = CountDownLatch(3)
    
    // Create a consumer
    val consumer = object : PushConsumer {
        override fun push(data: Any) {
            logger.info { "Consumer received event: $data" }
            eventLatch.countDown()
        }
        
        override fun disconnect_push_consumer() {
            logger.info { "Consumer disconnected" }
        }
    }
    
    // Create a supplier
    val supplier = object : PushSupplier {
        override fun disconnect_push_supplier() {
            logger.info { "Supplier disconnected" }
        }
    }
    
    // Get proxies
    val proxyPushSupplier = consumerAdmin.obtain_push_supplier()
    val proxyPushConsumer = supplierAdmin.obtain_push_consumer()
    
    // Connect the consumer and supplier to the proxies
    logger.info { "Connecting consumer and supplier" }
    proxyPushSupplier.connect_push_consumer(consumer)
    proxyPushConsumer.connect_push_supplier(supplier)
    
    // Push some events
    logger.info { "Pushing events" }
    runBlocking {
        repeat(3) { i ->
            logger.info { "Pushing event $i" }
            try {
                proxyPushConsumer.push("Event $i")
                delay(500) // Wait a bit between events
            } catch (e: Disconnected) {
                logger.error { "Failed to push event: ${e.message}" }
            }
        }
    }
    
    // Wait for all events to be received
    val allReceived = eventLatch.await(5, TimeUnit.SECONDS)
    if (allReceived) {
        logger.info { "All events were received successfully" }
    } else {
        logger.warn { "Not all events were received within the timeout" }
    }
    
    // Look up the event channel by name
    val lookedUpChannel = factory.lookupEventChannel(channelName)
    logger.info { "Looked up event channel: ${lookedUpChannel != null}" }
    
    // Disconnect and clean up
    logger.info { "Disconnecting and cleaning up" }
    proxyPushSupplier.disconnect_push_supplier()
    proxyPushConsumer.disconnect_push_consumer()
    
    // Destroy the event channel
    eventChannel.destroy()
    
    // Unregister the event channel
    factory.unregisterEventChannel(channelName)
    
    logger.info { "Event Channel Example completed" }
}
