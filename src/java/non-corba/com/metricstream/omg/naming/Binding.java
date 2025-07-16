package com.metricstream.omg.naming;

import java.io.Serializable;

/**
 * A binding in the naming service, associating a name with an object or context.
 * This is the non-CORBA equivalent of org.omg.CosNaming.Binding.
 */
public class Binding implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The name of the binding.
     */
    public Name binding_name;
    
    /**
     * The type of the binding (object or context).
     */
    public BindingType binding_type;
    
    /**
     * Creates a new binding with null name and object type.
     */
    public Binding() {
        this.binding_name = new Name();
        this.binding_type = BindingType.nobject;
    }
    
    /**
     * Creates a new binding with the specified name and type.
     * 
     * @param binding_name The name of the binding
     * @param binding_type The type of the binding
     */
    public Binding(Name binding_name, BindingType binding_type) {
        this.binding_name = binding_name;
        this.binding_type = binding_type;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        Binding other = (Binding) obj;
        
        if (binding_name == null) {
            if (other.binding_name != null) {
                return false;
            }
        } else if (!binding_name.equals(other.binding_name)) {
            return false;
        }
        
        if (binding_type != other.binding_type) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((binding_name == null) ? 0 : binding_name.hashCode());
        result = prime * result + ((binding_type == null) ? 0 : binding_type.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        return binding_name + " (" + binding_type + ")";
    }
}
