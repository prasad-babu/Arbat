package com.metricstream.omg.naming;

/**
 * Enumeration of reasons why a name was not found in the naming service.
 * This is the non-CORBA equivalent of org.omg.CosNaming.NamingContextPackage.NotFoundReason.
 */
public enum NotFoundReason {
    /**
     * The name component was not found.
     */
    missing_node,
    
    /**
     * The name component was found but was not a context.
     */
    not_context,
    
    /**
     * The name component was found but was not an object.
     */
    not_object;
    
    /**
     * Gets the integer value of this reason.
     * 
     * @return The integer value
     */
    public int value() {
        return ordinal();
    }
    
    /**
     * Gets the reason from an integer value.
     * 
     * @param value The integer value
     * @return The corresponding reason
     * @throws IllegalArgumentException If the value is not valid
     */
    public static NotFoundReason from_int(int value) {
        switch (value) {
            case 0:
                return missing_node;
            case 1:
                return not_context;
            case 2:
                return not_object;
            default:
                throw new IllegalArgumentException("Invalid NotFoundReason value: " + value);
        }
    }
}
