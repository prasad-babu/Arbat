package com.metricstream.omg.naming;

/**
 * Holder class for BindingIterator, used for out parameters in method calls.
 * This is the non-CORBA equivalent of org.omg.CosNaming.BindingIteratorHolder.
 */
public class BindingIteratorHolder {
    
    /**
     * The value being held.
     */
    public BindingIterator value;
    
    /**
     * Creates a new holder with a null value.
     */
    public BindingIteratorHolder() {
        this.value = null;
    }
    
    /**
     * Creates a new holder with the specified value.
     * 
     * @param value The initial value
     */
    public BindingIteratorHolder(BindingIterator value) {
        this.value = value;
    }
}
