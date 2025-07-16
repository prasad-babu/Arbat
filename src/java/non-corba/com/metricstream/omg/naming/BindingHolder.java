package com.metricstream.omg.naming;

/**
 * Holder class for Binding, used for out parameters in method calls.
 * This is the non-CORBA equivalent of org.omg.CosNaming.BindingHolder.
 */
public class BindingHolder {
    
    /**
     * The value being held.
     */
    public Binding value;
    
    /**
     * Creates a new holder with a null value.
     */
    public BindingHolder() {
        this.value = null;
    }
    
    /**
     * Creates a new holder with the specified value.
     * 
     * @param value The initial value
     */
    public BindingHolder(Binding value) {
        this.value = value;
    }
}
