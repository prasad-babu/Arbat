package com.metricstream.omg.eventchannel;

/**
 * Exception thrown when an operation is attempted with an incompatible type.
 * This is the non-CORBA equivalent of org.omg.CosEventChannelAdmin.TypeError.
 */
public class TypeError extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new TypeError exception.
     */
    public TypeError() {
        super("Type error in event channel operation");
    }
    
    /**
     * Creates a new TypeError exception with the specified message.
     * 
     * @param message The detail message
     */
    public TypeError(String message) {
        super(message);
    }
}
