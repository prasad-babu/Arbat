package com.metricstream.omg.naming;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the BindingIterator interface.
 * This is the non-CORBA equivalent of the CORBA BindingIterator implementation.
 */
public class BindingIteratorImpl implements BindingIterator {
    
    /**
     * The list of bindings to iterate over.
     */
    private final List<Binding> bindings;
    
    /**
     * The current position in the list.
     */
    private int position;
    
    /**
     * Creates a new binding iterator with the specified bindings.
     * 
     * @param bindings The bindings to iterate over
     */
    public BindingIteratorImpl(List<Binding> bindings) {
        this.bindings = new ArrayList<>(bindings);
        this.position = 0;
    }
    
    @Override
    public boolean next_one(BindingHolder b) {
        if (position >= bindings.size()) {
            return false;
        }
        
        b.value = bindings.get(position);
        position++;
        return true;
    }
    
    @Override
    public boolean next_n(int how_many, BindingListHolder bl) {
        if (position >= bindings.size()) {
            return false;
        }
        
        if (bl.value == null) {
            bl.value = new BindingList();
        }
        
        int count = Math.min(how_many, bindings.size() - position);
        for (int i = 0; i < count; i++) {
            bl.value.add(bindings.get(position));
            position++;
        }
        
        return count > 0;
    }
    
    @Override
    public void destroy() {
        bindings.clear();
        position = 0;
    }
}
