package com.metricstream.omg.naming;

/**
 * Exception thrown when attempting to destroy a naming context that is not empty.
 * This is the non-CORBA equivalent of org.omg.CosNaming.NamingContextPackage.NotEmpty.
 */
public class NotEmpty extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new NotEmpty exception.
     */
    public NotEmpty() {
        super("Naming context is not empty");
    }
    
    /**
     * Creates a new NotEmpty exception with the specified message.
     * 
     * @param message The detail message
     */
    public NotEmpty(String message) {
        super(message);
    }
}
