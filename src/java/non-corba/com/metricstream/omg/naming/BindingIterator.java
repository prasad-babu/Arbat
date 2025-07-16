package com.metricstream.omg.naming;

/**
 * Interface for iterating through bindings in the naming service.
 * This is the non-CORBA equivalent of org.omg.CosNaming.BindingIterator.
 */
public interface BindingIterator {

    /**
     * Gets the next binding in the iteration.
     * 
     * @param b The holder for the binding
     * @return true if a binding was returned, false if there are no more bindings
     */
    boolean next_one(BindingHolder b);
    
    /**
     * Gets the next n bindings in the iteration.
     * 
     * @param how_many The maximum number of bindings to return
     * @param bl The holder for the binding list
     * @return true if at least one binding was returned, false if there are no more bindings
     */
    boolean next_n(int how_many, BindingListHolder bl);
    
    /**
     * Destroys this iterator.
     */
    void destroy();
}
