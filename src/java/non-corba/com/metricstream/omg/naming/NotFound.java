package com.metricstream.omg.naming;

/**
 * Exception thrown when a name cannot be found in the naming service.
 * This is the non-CORBA equivalent of org.omg.CosNaming.NamingContextPackage.NotFound.
 */
public class NotFound extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The reason why the name was not found.
     */
    public NotFoundReason why;
    
    /**
     * The remaining part of the name that could not be resolved.
     */
    public Name rest_of_name;
    
    /**
     * Creates a new NotFound exception with the specified reason and remaining name.
     * 
     * @param why The reason why the name was not found
     * @param rest_of_name The remaining part of the name that could not be resolved
     */
    public NotFound(NotFoundReason why, Name rest_of_name) {
        super("Name not found: " + why + ", rest of name: " + rest_of_name);
        this.why = why;
        this.rest_of_name = rest_of_name;
    }
}
