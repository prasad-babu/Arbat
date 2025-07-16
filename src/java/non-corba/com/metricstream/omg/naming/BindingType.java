package com.metricstream.omg.naming;

/**
 * Enumeration of binding types in the naming service.
 * This is the non-CORBA equivalent of org.omg.CosNaming.BindingType.
 */
public enum BindingType {
    /**
     * The binding is to an object.
     */
    nobject,
    
    /**
     * The binding is to a naming context.
     */
    ncontext;
    
    /**
     * Gets the integer value of this binding type.
     * 
     * @return The integer value
     */
    public int value() {
        return ordinal();
    }
    
    /**
     * Gets the binding type from an integer value.
     * 
     * @param value The integer value
     * @return The corresponding binding type
     * @throws IllegalArgumentException If the value is not valid
     */
    public static BindingType from_int(int value) {
        switch (value) {
            case 0:
                return nobject;
            case 1:
                return ncontext;
            default:
                throw new IllegalArgumentException("Invalid BindingType value: " + value);
        }
    }
}
