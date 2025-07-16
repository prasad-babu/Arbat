package com.metricstream.omg.event;

/**
 * Holder class for boolean values, used for out parameters in method calls.
 * This is the non-CORBA equivalent of org.omg.CORBA.BooleanHolder.
 */
public class BooleanHolder {
    
    /**
     * The value being held.
     */
    public boolean value;
    
    /**
     * Creates a new holder with the default value (false).
     */
    public BooleanHolder() {
        this.value = false;
    }
    
    /**
     * Creates a new holder with the specified value.
     * 
     * @param value The initial value
     */
    public BooleanHolder(boolean value) {
        this.value = value;
    }
}
