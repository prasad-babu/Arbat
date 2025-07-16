package com.metricstream.omg.naming;

/**
 * Exception thrown when a name is already bound to an object.
 * This is the non-CORBA equivalent of org.omg.CosNaming.NamingContextPackage.AlreadyBound.
 */
public class AlreadyBound extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new AlreadyBound exception.
     */
    public AlreadyBound() {
        super("Name already bound");
    }
    
    /**
     * Creates a new AlreadyBound exception with the specified message.
     * 
     * @param message The detail message
     */
    public AlreadyBound(String message) {
        super(message);
    }
}
