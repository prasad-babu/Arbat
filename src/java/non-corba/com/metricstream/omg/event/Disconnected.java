package com.metricstream.omg.event;

/**
 * Exception thrown when an operation is attempted on a disconnected event channel component.
 * This is the non-CORBA equivalent of org.omg.CosEventComm.Disconnected.
 */
public class Disconnected extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new Disconnected exception.
     */
    public Disconnected() {
        super("Event channel component is disconnected");
    }
    
    /**
     * Creates a new Disconnected exception with the specified message.
     * 
     * @param message The detail message
     */
    public Disconnected(String message) {
        super(message);
    }
}
