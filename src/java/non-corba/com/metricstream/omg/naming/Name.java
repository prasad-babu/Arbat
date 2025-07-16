package com.metricstream.omg.naming;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A name in the naming service, consisting of a sequence of name components.
 * This is the non-CORBA equivalent of org.omg.CosNaming.NameComponent[].
 */
public class Name implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The list of name components.
     */
    private List<NameComponent> components;
    
    /**
     * Creates a new empty name.
     */
    public Name() {
        this.components = new ArrayList<>();
    }
    
    /**
     * Creates a new name with the specified components.
     * 
     * @param components The list of name components
     */
    public Name(List<NameComponent> components) {
        this.components = new ArrayList<>(components);
    }
    
    /**
     * Adds a component to this name.
     * 
     * @param component The component to add
     * @return This name, for method chaining
     */
    public Name addComponent(NameComponent component) {
        components.add(component);
        return this;
    }
    
    /**
     * Gets the component at the specified index.
     * 
     * @param index The index of the component to get
     * @return The component at the specified index
     * @throws IndexOutOfBoundsException If the index is out of range
     */
    public NameComponent get(int index) {
        return components.get(index);
    }
    
    /**
     * Gets the number of components in this name.
     * 
     * @return The number of components
     */
    public int size() {
        return components.size();
    }
    
    /**
     * Checks if this name is empty.
     * 
     * @return true if this name has no components, false otherwise
     */
    public boolean isEmpty() {
        return components.isEmpty();
    }
    
    /**
     * Gets a copy of the components in this name.
     * 
     * @return A copy of the components
     */
    public List<NameComponent> getComponents() {
        return new ArrayList<>(components);
    }
    
    /**
     * Creates a new name that is a prefix of this name.
     * 
     * @param length The number of components to include in the prefix
     * @return A new name containing the first 'length' components of this name
     * @throws IndexOutOfBoundsException If length is negative or greater than the size of this name
     */
    public Name prefix(int length) {
        if (length < 0 || length > components.size()) {
            throw new IndexOutOfBoundsException("Invalid prefix length: " + length);
        }
        
        Name prefix = new Name();
        for (int i = 0; i < length; i++) {
            prefix.addComponent(components.get(i));
        }
        
        return prefix;
    }
    
    /**
     * Creates a new name that is a suffix of this name.
     * 
     * @param startIndex The index of the first component to include in the suffix
     * @return A new name containing the components of this name starting from 'startIndex'
     * @throws IndexOutOfBoundsException If startIndex is negative or greater than the size of this name
     */
    public Name suffix(int startIndex) {
        if (startIndex < 0 || startIndex > components.size()) {
            throw new IndexOutOfBoundsException("Invalid suffix start index: " + startIndex);
        }
        
        Name suffix = new Name();
        for (int i = startIndex; i < components.size(); i++) {
            suffix.addComponent(components.get(i));
        }
        
        return suffix;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        Name other = (Name) obj;
        
        if (components.size() != other.components.size()) {
            return false;
        }
        
        for (int i = 0; i < components.size(); i++) {
            if (!components.get(i).equals(other.components.get(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = 1;
        for (NameComponent component : components) {
            result = 31 * result + component.hashCode();
        }
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) {
                sb.append("/");
            }
            sb.append(components.get(i).toString());
        }
        return sb.toString();
    }
}
