package com.metricstream.omg.util;

/**
 * A simplified version of the CORBA Any type for use in the non-CORBA implementation.
 * This class provides a container for values of any type, with type information.
 */
public class Any {
    
    private Object value;
    private TypeCode typeCode;
    
    /**
     * Creates a new Any with null value and null type.
     */
    public Any() {
        this.value = null;
        this.typeCode = TypeCode.get(TypeCode.TCKind.tk_null);
    }
    
    /**
     * Creates a new Any with the specified value and type.
     * 
     * @param value The value
     * @param typeCode The type code
     */
    public Any(Object value, TypeCode typeCode) {
        this.value = value;
        this.typeCode = typeCode;
    }
    
    /**
     * Gets the type code of this Any.
     * 
     * @return The type code
     */
    public TypeCode type() {
        return typeCode;
    }
    
    /**
     * Sets the type code of this Any.
     * 
     * @param typeCode The type code
     */
    public void type(TypeCode typeCode) {
        this.typeCode = typeCode;
    }
    
    /**
     * Gets the value of this Any.
     * 
     * @return The value
     */
    public Object value() {
        return value;
    }
    
    /**
     * Inserts a boolean value into this Any.
     * 
     * @param value The value
     */
    public void insert_boolean(boolean value) {
        this.value = value;
        this.typeCode = TypeCode.booleanType();
    }
    
    /**
     * Extracts a boolean value from this Any.
     * 
     * @return The boolean value
     * @throws ClassCastException If the value is not a boolean
     */
    public boolean extract_boolean() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new ClassCastException("Value is not a boolean");
    }
    
    /**
     * Inserts a char value into this Any.
     * 
     * @param value The value
     */
    public void insert_char(char value) {
        this.value = value;
        this.typeCode = TypeCode.get(TypeCode.TCKind.tk_char);
    }
    
    /**
     * Extracts a char value from this Any.
     * 
     * @return The char value
     * @throws ClassCastException If the value is not a char
     */
    public char extract_char() {
        if (value instanceof Character) {
            return (Character) value;
        }
        throw new ClassCastException("Value is not a char");
    }
    
    /**
     * Inserts a byte value into this Any.
     * 
     * @param value The value
     */
    public void insert_octet(byte value) {
        this.value = value;
        this.typeCode = TypeCode.get(TypeCode.TCKind.tk_octet);
    }
    
    /**
     * Extracts a byte value from this Any.
     * 
     * @return The byte value
     * @throws ClassCastException If the value is not a byte
     */
    public byte extract_octet() {
        if (value instanceof Byte) {
            return (Byte) value;
        }
        throw new ClassCastException("Value is not a byte");
    }
    
    /**
     * Inserts a short value into this Any.
     * 
     * @param value The value
     */
    public void insert_short(short value) {
        this.value = value;
        this.typeCode = TypeCode.get(TypeCode.TCKind.tk_short);
    }
    
    /**
     * Extracts a short value from this Any.
     * 
     * @return The short value
     * @throws ClassCastException If the value is not a short
     */
    public short extract_short() {
        if (value instanceof Short) {
            return (Short) value;
        }
        throw new ClassCastException("Value is not a short");
    }
    
    /**
     * Inserts an int value into this Any.
     * 
     * @param value The value
     */
    public void insert_long(int value) {
        this.value = value;
        this.typeCode = TypeCode.longType();
    }
    
    /**
     * Extracts an int value from this Any.
     * 
     * @return The int value
     * @throws ClassCastException If the value is not an int
     */
    public int extract_long() {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        throw new ClassCastException("Value is not an int");
    }
    
    /**
     * Inserts a long value into this Any.
     * 
     * @param value The value
     */
    public void insert_longlong(long value) {
        this.value = value;
        this.typeCode = TypeCode.get(TypeCode.TCKind.tk_longlong);
    }
    
    /**
     * Extracts a long value from this Any.
     * 
     * @return The long value
     * @throws ClassCastException If the value is not a long
     */
    public long extract_longlong() {
        if (value instanceof Long) {
            return (Long) value;
        }
        throw new ClassCastException("Value is not a long");
    }
    
    /**
     * Inserts a float value into this Any.
     * 
     * @param value The value
     */
    public void insert_float(float value) {
        this.value = value;
        this.typeCode = TypeCode.get(TypeCode.TCKind.tk_float);
    }
    
    /**
     * Extracts a float value from this Any.
     * 
     * @return The float value
     * @throws ClassCastException If the value is not a float
     */
    public float extract_float() {
        if (value instanceof Float) {
            return (Float) value;
        }
        throw new ClassCastException("Value is not a float");
    }
    
    /**
     * Inserts a double value into this Any.
     * 
     * @param value The value
     */
    public void insert_double(double value) {
        this.value = value;
        this.typeCode = TypeCode.doubleType();
    }
    
    /**
     * Extracts a double value from this Any.
     * 
     * @return The double value
     * @throws ClassCastException If the value is not a double
     */
    public double extract_double() {
        if (value instanceof Double) {
            return (Double) value;
        }
        throw new ClassCastException("Value is not a double");
    }
    
    /**
     * Inserts a string value into this Any.
     * 
     * @param value The value
     */
    public void insert_string(String value) {
        this.value = value;
        this.typeCode = TypeCode.string();
    }
    
    /**
     * Extracts a string value from this Any.
     * 
     * @return The string value
     * @throws ClassCastException If the value is not a string
     */
    public String extract_string() {
        if (value instanceof String) {
            return (String) value;
        }
        throw new ClassCastException("Value is not a string");
    }
    
    /**
     * Inserts an object value into this Any.
     * 
     * @param value The value
     * @param typeCode The type code of the object
     */
    public void insert_Object(Object value, TypeCode typeCode) {
        this.value = value;
        this.typeCode = typeCode;
    }
    
    /**
     * Extracts an object value from this Any.
     * 
     * @return The object value
     */
    public Object extract_Object() {
        return value;
    }
    
    /**
     * Inserts an Any value into this Any.
     * 
     * @param value The value
     */
    public void insert_any(Any value) {
        this.value = value;
        this.typeCode = TypeCode.get(TypeCode.TCKind.tk_any);
    }
    
    /**
     * Extracts an Any value from this Any.
     * 
     * @return The Any value
     * @throws ClassCastException If the value is not an Any
     */
    public Any extract_any() {
        if (value instanceof Any) {
            return (Any) value;
        }
        throw new ClassCastException("Value is not an Any");
    }
    
    /**
     * Inserts a TypeCode value into this Any.
     * 
     * @param value The value
     */
    public void insert_TypeCode(TypeCode value) {
        this.value = value;
        this.typeCode = TypeCode.get(TypeCode.TCKind.tk_TypeCode);
    }
    
    /**
     * Extracts a TypeCode value from this Any.
     * 
     * @return The TypeCode value
     * @throws ClassCastException If the value is not a TypeCode
     */
    public TypeCode extract_TypeCode() {
        if (value instanceof TypeCode) {
            return (TypeCode) value;
        }
        throw new ClassCastException("Value is not a TypeCode");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        Any other = (Any) obj;
        
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        
        if (typeCode == null) {
            if (other.typeCode != null) {
                return false;
            }
        } else if (!typeCode.equals(other.typeCode)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((typeCode == null) ? 0 : typeCode.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        return "Any[" + typeCode + ", " + value + "]";
    }
}
