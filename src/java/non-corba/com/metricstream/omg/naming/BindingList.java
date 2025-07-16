package com.metricstream.omg.naming;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A list of bindings in the naming service.
 * This is the non-CORBA equivalent of org.omg.CosNaming.BindingList.
 */
public class BindingList extends ArrayList<Binding> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new empty binding list.
     */
    public BindingList() {
        super();
    }
    
    /**
     * Creates a new binding list with the specified initial capacity.
     * 
     * @param initialCapacity The initial capacity of the list
     */
    public BindingList(int initialCapacity) {
        super(initialCapacity);
    }
    
    /**
     * Creates a new binding list containing the elements of the specified collection.
     * 
     * @param c The collection whose elements are to be placed into this list
     */
    public BindingList(Collection<? extends Binding> c) {
        super(c);
    }
}
