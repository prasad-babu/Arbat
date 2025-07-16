package com.metricstream.omg.eventchannel;

/**
 * Exception thrown when attempting to connect a supplier or consumer that is already connected.
 * This is the non-CORBA equivalent of org.omg.CosEventChannelAdmin.AlreadyConnected.
 */
public class AlreadyConnected extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new AlreadyConnected exception.
     */
    public AlreadyConnected() {
        super("Supplier or consumer is already connected");
    }
    
    /**
     * Creates a new AlreadyConnected exception with the specified message.
     * 
     * @param message The detail message
     */
    public AlreadyConnected(String message) {
        super(message);
    }
}
