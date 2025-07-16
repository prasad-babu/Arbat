package com.metricstream.omg.naming;

/**
 * Holder class for BindingList, used for out parameters in method calls.
 * This is the non-CORBA equivalent of org.omg.CosNaming.BindingListHolder.
 */
public class BindingListHolder {
    
    /**
     * The value being held.
     */
    public BindingList value;
    
    /**
     * Creates a new holder with a null value.
     */
    public BindingListHolder() {
        this.value = null;
    }
    
    /**
     * Creates a new holder with the specified value.
     * 
     * @param value The initial value
     */
    public BindingListHolder(BindingList value) {
        this.value = value;
    }
}
