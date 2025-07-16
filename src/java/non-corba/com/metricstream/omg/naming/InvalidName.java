package com.metricstream.omg.naming;

/**
 * Exception thrown when a name is invalid.
 * This is the non-CORBA equivalent of org.omg.CosNaming.NamingContextPackage.InvalidName.
 */
public class InvalidName extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new InvalidName exception.
     */
    public InvalidName() {
        super("Invalid name");
    }
    
    /**
     * Creates a new InvalidName exception with the specified message.
     * 
     * @param message The detail message
     */
    public InvalidName(String message) {
        super(message);
    }
}
