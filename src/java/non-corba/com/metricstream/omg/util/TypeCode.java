package com.metricstream.omg.util;

/**
 * A simplified version of the CORBA TypeCode for use in the non-CORBA implementation.
 * This class provides basic type information for objects used in the event system.
 */
public class TypeCode {
    
    /**
     * Enumeration of basic types supported by this TypeCode implementation.
     */
    public enum TCKind {
        tk_null,
        tk_void,
        tk_short,
        tk_long,
        tk_ushort,
        tk_ulong,
        tk_float,
        tk_double,
        tk_boolean,
        tk_char,
        tk_octet,
        tk_any,
        tk_TypeCode,
        tk_Principal,
        tk_objref,
        tk_struct,
        tk_union,
        tk_enum,
        tk_string,
        tk_sequence,
        tk_array,
        tk_alias,
        tk_except,
        tk_longlong,
        tk_ulonglong,
        tk_longdouble,
        tk_wchar,
        tk_wstring,
        tk_fixed,
        tk_value,
        tk_value_box,
        tk_native,
        tk_abstract_interface,
        tk_local_interface
    }
    
    private final TCKind kind;
    private final String id;
    private final String name;
    
    /**
     * Creates a new TypeCode with the specified kind, id, and name.
     * 
     * @param kind The kind of type
     * @param id The repository ID of the type
     * @param name The name of the type
     */
    public TypeCode(TCKind kind, String id, String name) {
        this.kind = kind;
        this.id = id;
        this.name = name;
    }
    
    /**
     * Creates a new TypeCode with the specified kind.
     * 
     * @param kind The kind of type
     */
    public TypeCode(TCKind kind) {
        this(kind, "", "");
    }
    
    /**
     * Gets the kind of this type.
     * 
     * @return The kind
     */
    public TCKind kind() {
        return kind;
    }
    
    /**
     * Gets the repository ID of this type.
     * 
     * @return The repository ID
     */
    public String id() {
        return id;
    }
    
    /**
     * Gets the name of this type.
     * 
     * @return The name
     */
    public String name() {
        return name;
    }
    
    /**
     * Gets a TypeCode for a primitive type.
     * 
     * @param kind The kind of primitive type
     * @return The TypeCode
     */
    public static TypeCode get(TCKind kind) {
        return new TypeCode(kind);
    }
    
    /**
     * Gets a TypeCode for a string.
     * 
     * @return The TypeCode
     */
    public static TypeCode string() {
        return new TypeCode(TCKind.tk_string);
    }
    
    /**
     * Gets a TypeCode for a long.
     * 
     * @return The TypeCode
     */
    public static TypeCode longType() {
        return new TypeCode(TCKind.tk_long);
    }
    
    /**
     * Gets a TypeCode for a double.
     * 
     * @return The TypeCode
     */
    public static TypeCode doubleType() {
        return new TypeCode(TCKind.tk_double);
    }
    
    /**
     * Gets a TypeCode for a boolean.
     * 
     * @return The TypeCode
     */
    public static TypeCode booleanType() {
        return new TypeCode(TCKind.tk_boolean);
    }
    
    /**
     * Gets a TypeCode for an object reference.
     * 
     * @param id The repository ID of the object
     * @param name The name of the object
     * @return The TypeCode
     */
    public static TypeCode objref(String id, String name) {
        return new TypeCode(TCKind.tk_objref, id, name);
    }
    
    /**
     * Gets a TypeCode for a struct.
     * 
     * @param id The repository ID of the struct
     * @param name The name of the struct
     * @return The TypeCode
     */
    public static TypeCode struct(String id, String name) {
        return new TypeCode(TCKind.tk_struct, id, name);
    }
    
    /**
     * Gets a TypeCode for an enum.
     * 
     * @param id The repository ID of the enum
     * @param name The name of the enum
     * @return The TypeCode
     */
    public static TypeCode enumType(String id, String name) {
        return new TypeCode(TCKind.tk_enum, id, name);
    }
    
    /**
     * Gets a TypeCode for a sequence.
     * 
     * @param id The repository ID of the sequence
     * @param name The name of the sequence
     * @return The TypeCode
     */
    public static TypeCode sequence(String id, String name) {
        return new TypeCode(TCKind.tk_sequence, id, name);
    }
    
    /**
     * Gets a TypeCode for an array.
     * 
     * @param id The repository ID of the array
     * @param name The name of the array
     * @return The TypeCode
     */
    public static TypeCode array(String id, String name) {
        return new TypeCode(TCKind.tk_array, id, name);
    }
    
    /**
     * Gets a TypeCode for an exception.
     * 
     * @param id The repository ID of the exception
     * @param name The name of the exception
     * @return The TypeCode
     */
    public static TypeCode except(String id, String name) {
        return new TypeCode(TCKind.tk_except, id, name);
    }
    
    /**
     * Gets a TypeCode for a value.
     * 
     * @param id The repository ID of the value
     * @param name The name of the value
     * @return The TypeCode
     */
    public static TypeCode value(String id, String name) {
        return new TypeCode(TCKind.tk_value, id, name);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        TypeCode other = (TypeCode) obj;
        
        if (kind != other.kind) {
            return false;
        }
        
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((kind == null) ? 0 : kind.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        if (id.isEmpty() && name.isEmpty()) {
            return kind.toString();
        } else {
            return kind + "(" + id + ", " + name + ")";
        }
    }
}
