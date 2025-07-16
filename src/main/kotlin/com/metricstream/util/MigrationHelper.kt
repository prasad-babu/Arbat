package com.metricstream.util

import com.metricstream.naming.*
import com.metricstream.event.*
import com.metricstream.eventchannel.*
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

/**
 * Helper class for migrating from CORBA to non-CORBA implementation.
 * Provides utilities for converting between CORBA and non-CORBA types.
 */
object MigrationHelper {
    /**
     * Converts a CORBA NameComponent array to a non-CORBA Name.
     * 
     * @param corbaName The CORBA NameComponent array
     * @return The non-CORBA Name
     */
    @JvmStatic
    fun convertName(corbaName: Array<*>): Name {
        val name = Name()
        
        for (component in corbaName) {
            // Using reflection to access id and kind fields from CORBA NameComponent
            val id = component?.javaClass?.getMethod("id")?.invoke(component) as? String ?: ""
            val kind = component?.javaClass?.getMethod("kind")?.invoke(component) as? String ?: ""
            name.addComponent(NameComponent(id, kind))
        }
        
        return name
    }
    
    /**
     * Converts a non-CORBA Name to a CORBA NameComponent array.
     * 
     * @param name The non-CORBA Name
     * @param corbaNameComponentClass The CORBA NameComponent class
     * @return The CORBA NameComponent array
     */
    @JvmStatic
    fun convertToCorbaName(name: Name, corbaNameComponentClass: Class<*>): Array<Any> {
        val result = java.lang.reflect.Array.newInstance(corbaNameComponentClass, name.size()) as Array<Any>
        
        for (i in 0 until name.size()) {
            val component = name.components[i]
            val corbaComponent = corbaNameComponentClass.getConstructor(String::class.java, String::class.java)
                .newInstance(component.id, component.kind)
            result[i] = corbaComponent
        }
        
        return result
    }
    
    /**
     * Extracts a value from a CORBA Any.
     * 
     * @param any The CORBA Any
     * @return The extracted value
     */
    @JvmStatic
    fun extractFromAny(any: Any): Any? {
        // Try to extract using common CORBA Any methods
        return try {
            val extractMethod = findExtractMethod(any)
            extractMethod?.invoke(any)
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract value from Any" }
            null
        }
    }
    
    /**
     * Finds an appropriate extract_X method on a CORBA Any object.
     * 
     * @param any The CORBA Any
     * @return The extract method, or null if not found
     */
    private fun findExtractMethod(any: Any): java.lang.reflect.Method? {
        val methods = any.javaClass.methods
        
        // Try to determine the type and find the appropriate extract method
        val typeMethod = methods.find { it.name == "type" }
        val type = typeMethod?.invoke(any)
        
        // If we have a type, try to find the appropriate extract method
        if (type != null) {
            // Try to get the kind from the type
            val kindMethod = type.javaClass.methods.find { it.name == "kind" }
            val kind = kindMethod?.invoke(type)
            
            // Based on the kind, find the appropriate extract method
            return when (kind?.toString()) {
                "tk_null", "tk_void" -> null
                "tk_short" -> methods.find { it.name == "extract_short" }
                "tk_long" -> methods.find { it.name == "extract_long" }
                "tk_ushort" -> methods.find { it.name == "extract_ushort" }
                "tk_ulong" -> methods.find { it.name == "extract_ulong" }
                "tk_float" -> methods.find { it.name == "extract_float" }
                "tk_double" -> methods.find { it.name == "extract_double" }
                "tk_boolean" -> methods.find { it.name == "extract_boolean" }
                "tk_char" -> methods.find { it.name == "extract_char" }
                "tk_octet" -> methods.find { it.name == "extract_octet" }
                "tk_string" -> methods.find { it.name == "extract_string" }
                "tk_any" -> methods.find { it.name == "extract_any" }
                "tk_TypeCode" -> methods.find { it.name == "extract_TypeCode" }
                "tk_objref" -> methods.find { it.name == "extract_Object" }
                else -> methods.find { it.name == "extract_Object" }
            }
        }
        
        // If we couldn't determine the type, try common extract methods
        return methods.find { it.name == "extract_string" }
            ?: methods.find { it.name == "extract_Object" }
            ?: methods.find { it.name.startsWith("extract_") }
    }
    
    /**
     * Creates a CORBA Any from a value.
     * 
     * @param orb The CORBA ORB
     * @param value The value
     * @return The CORBA Any
     */
    @JvmStatic
    fun createAny(orb: Any, value: Any?): Any? {
        // Try to create an Any using the ORB
        return try {
            val createAnyMethod = orb.javaClass.getMethod("create_any")
            val any = createAnyMethod.invoke(orb)
            
            if (value != null) {
                insertIntoAny(any, value)
            }
            
            any
        } catch (e: Exception) {
            logger.warn(e) { "Failed to create Any" }
            null
        }
    }
    
    /**
     * Inserts a value into a CORBA Any.
     * 
     * @param any The CORBA Any
     * @param value The value to insert
     */
    private fun insertIntoAny(any: Any, value: Any) {
        val methods = any.javaClass.methods
        
        // Based on the value type, find the appropriate insert method
        val insertMethod = when (value) {
            is String -> methods.find { it.name == "insert_string" }
            is Int -> methods.find { it.name == "insert_long" }
            is Short -> methods.find { it.name == "insert_short" }
            is Long -> methods.find { it.name == "insert_longlong" }
            is Float -> methods.find { it.name == "insert_float" }
            is Double -> methods.find { it.name == "insert_double" }
            is Boolean -> methods.find { it.name == "insert_boolean" }
            is Char -> methods.find { it.name == "insert_char" }
            is Byte -> methods.find { it.name == "insert_octet" }
            else -> methods.find { it.name == "insert_Object" }
        }
        
        insertMethod?.invoke(any, value)
    }
    
    /**
     * Converts a CORBA object to a non-CORBA object.
     * 
     * @param corbaObj The CORBA object
     * @param targetClass The target non-CORBA class
     * @return The non-CORBA object
     */
    @JvmStatic
    fun <T : Any> convertToNonCorba(corbaObj: Any?, targetClass: KClass<T>): T? {
        if (corbaObj == null) {
            return null
        }
        
        // If the object is already of the target type, return it
        if (targetClass.java.isInstance(corbaObj)) {
            @Suppress("UNCHECKED_CAST")
            return corbaObj as T
        }
        
        // For specific types, use specialized conversion
        return when (targetClass) {
            Name::class -> {
                // Convert CORBA NameComponent[] to Name
                if (corbaObj is Array<*>) {
                    @Suppress("UNCHECKED_CAST")
                    convertName(corbaObj) as T
                } else {
                    null
                }
            }
            else -> {
                // For other types, try to create a new instance and copy properties
                try {
                    val instance = targetClass.java.getDeclaredConstructor().newInstance()
                    
                    // Copy properties using reflection
                    for (property in targetClass.java.methods) {
                        if (property.name.startsWith("set")) {
                            val getterName = "get" + property.name.substring(3)
                            val getter = corbaObj.javaClass.getMethod(getterName)
                            val value = getter.invoke(corbaObj)
                            property.invoke(instance, value)
                        }
                    }
                    
                    instance
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to convert CORBA object to ${targetClass.simpleName}" }
                    null
                }
            }
        }
    }
    
    /**
     * Resolves a CORBA object as a non-CORBA object.
     * 
     * @param corbaObj The CORBA object
     * @param narrowHelper The CORBA helper class for narrowing
     * @param targetClass The target non-CORBA class
     * @return The non-CORBA object
     */
    @JvmStatic
    fun <T : Any> resolveAsNonCorba(corbaObj: Any?, narrowHelper: Class<*>, targetClass: KClass<T>): T? {
        if (corbaObj == null) {
            return null
        }
        
        try {
            // Use the narrow method from the helper class
            val narrowMethod = narrowHelper.getMethod("narrow", Class.forName("org.omg.CORBA.Object"))
            val narrowed = narrowMethod.invoke(null, corbaObj)
            
            // Convert the narrowed object to the target class
            return convertToNonCorba(narrowed, targetClass)
        } catch (e: Exception) {
            logger.warn(e) { "Failed to resolve CORBA object as ${targetClass.simpleName}" }
            return null
        }
    }
    
    /**
     * Creates a string representation of a CORBA name.
     * 
     * @param corbaName The CORBA NameComponent array
     * @return The string representation
     */
    @JvmStatic
    fun nameToString(corbaName: Array<*>): String {
        return convertName(corbaName).toString()
    }
    
    /**
     * Creates a CORBA NameComponent array from a string representation.
     * 
     * @param nameStr The string representation
     * @param corbaNameComponentClass The CORBA NameComponent class
     * @return The CORBA NameComponent array
     */
    @JvmStatic
    fun stringToName(nameStr: String, corbaNameComponentClass: Class<*>): Array<Any> {
        val name = Name.fromString(nameStr)
        return convertToCorbaName(name, corbaNameComponentClass)
    }
}
