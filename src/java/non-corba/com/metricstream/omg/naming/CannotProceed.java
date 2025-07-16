package com.metricstream.omg.naming;

/**
 * Exception thrown when the naming service cannot proceed with an operation.
 * This is the non-CORBA equivalent of org.omg.CosNaming.NamingContextPackage.CannotProceed.
 */
public class CannotProceed extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The naming context where the operation failed.
     */
    public NamingContext cxt;
    
    /**
     * The remaining part of the name that could not be processed.
     */
    public Name rest_of_name;
    
    /**
     * Creates a new CannotProceed exception with the specified context and remaining name.
     * 
     * @param cxt The naming context where the operation failed
     * @param rest_of_name The remaining part of the name that could not be processed
     */
    public CannotProceed(NamingContext cxt, Name rest_of_name) {
        super("Cannot proceed with operation, rest of name: " + rest_of_name);
        this.cxt = cxt;
        this.rest_of_name = rest_of_name;
    }
}
