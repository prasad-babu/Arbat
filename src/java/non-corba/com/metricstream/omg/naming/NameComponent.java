package com.metricstream.omg.naming;

import java.io.Serializable;

/**
 * A component of a name in the naming service.
 * This is the non-CORBA equivalent of org.omg.CosNaming.NameComponent.
 */
public class NameComponent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The identifier part of the name component.
     */
    public String id;
    
    /**
     * The kind part of the name component.
     */
    public String kind;
    
    /**
     * Creates a new name component with empty id and kind.
     */
    public NameComponent() {
        this.id = "";
        this.kind = "";
    }
    
    /**
     * Creates a new name component with the specified id and kind.
     * 
     * @param id The identifier part of the name component
     * @param kind The kind part of the name component
     */
    public NameComponent(String id, String kind) {
        this.id = id;
        this.kind = kind;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        NameComponent other = (NameComponent) obj;
        
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        
        if (kind == null) {
            if (other.kind != null) {
                return false;
            }
        } else if (!kind.equals(other.kind)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((kind == null) ? 0 : kind.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        if (kind == null || kind.isEmpty()) {
            return id;
        } else {
            return id + "." + kind;
        }
    }
}
