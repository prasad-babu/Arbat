package com.metricstream.naming

/**
 * Represents a component of a name in the naming service.
 * Equivalent to CosNaming::NameComponent in CORBA.
 */
data class NameComponent(val id: String, val kind: String = "") {
    override fun toString(): String = if (kind.isEmpty()) id else "$id.$kind"
}

/**
 * Represents a name composed of a sequence of name components.
 * Equivalent to CosNaming::Name in CORBA.
 */
data class Name(val components: MutableList<NameComponent> = mutableListOf()) {
    /**
     * Adds a component to this name.
     */
    fun addComponent(component: NameComponent): Name {
        components.add(component)
        return this
    }
    
    /**
     * Creates a new Name containing the first n components of this name.
     */
    fun prefix(length: Int): Name {
        require(length <= components.size) { "Length exceeds name size" }
        return Name(components.take(length).toMutableList())
    }
    
    /**
     * Creates a new Name containing all but the first n components of this name.
     */
    fun suffix(startIndex: Int): Name {
        require(startIndex <= components.size) { "Start index exceeds name size" }
        return Name(components.drop(startIndex).toMutableList())
    }
    
    /**
     * Returns the number of components in this name.
     */
    fun size(): Int = components.size
    
    /**
     * Returns a string representation of this name.
     */
    override fun toString(): String = components.joinToString("/")
    
    companion object {
        /**
         * Creates a Name from a string representation.
         * Format: "id1.kind1/id2.kind2/id3.kind3"
         */
        fun fromString(str: String): Name {
            if (str.isEmpty()) return Name()
            
            return Name(str.split("/").map { component ->
                val parts = component.split(".", limit = 2)
                when (parts.size) {
                    1 -> NameComponent(parts[0])
                    else -> NameComponent(parts[0], parts[1])
                }
            }.toMutableList())
        }
    }
}

/**
 * Represents the type of binding in the naming service.
 * Equivalent to CosNaming::BindingType in CORBA.
 */
enum class BindingType(val value: Int) {
    OBJECT(0),
    CONTEXT(1);
    
    companion object {
        fun fromValue(value: Int): BindingType = BindingType.entries.first { it.value == value }
    }
}

/**
 * Represents a binding between a name and an object in the naming service.
 * Equivalent to CosNaming::Binding in CORBA.
 */
data class Binding(val name: Name, val type: BindingType) {
    override fun toString(): String = "$name (${if (type == BindingType.OBJECT) "Object" else "Context"})"
}

/**
 * Represents a list of bindings in the naming service.
 * Equivalent to CosNaming::BindingList in CORBA.
 */
typealias BindingList = MutableList<Binding>

/**
 * Reasons why a name resolution might fail.
 * Equivalent to CosNaming::NotFoundReason in CORBA.
 */
enum class NotFoundReason(val value: Int) {
    MISSING_NODE(0),
    NOT_CONTEXT(1),
    NOT_OBJECT(2);
    
    companion object {
        fun fromValue(value: Int): NotFoundReason = values().first { it.value == value }
    }
}

/**
 * Base sealed class for naming service exceptions.
 * Equivalent to various CosNaming exceptions in CORBA.
 */
sealed class NamingException(message: String) : Exception(message)

/**
 * Exception thrown when a name cannot be found.
 * Equivalent to CosNaming::NotFound in CORBA.
 */
class NotFound(val reason: NotFoundReason, val restOfName: Name) : 
    NamingException("Name not found, reason: $reason")

/**
 * Exception thrown when a naming operation cannot proceed.
 * Equivalent to CosNaming::CannotProceed in CORBA.
 */
class CannotProceed(val context: NamingContext, val restOfName: Name) :
    NamingException("Cannot proceed with operation")

/**
 * Exception thrown when a name is invalid.
 * Equivalent to CosNaming::InvalidName in CORBA.
 */
class InvalidName : NamingException("Invalid name")

/**
 * Exception thrown when a name is already bound.
 * Equivalent to CosNaming::AlreadyBound in CORBA.
 */
class AlreadyBound : NamingException("Name already bound")

/**
 * Exception thrown when trying to destroy a non-empty context.
 * Equivalent to CosNaming::NotEmpty in CORBA.
 */
class NotEmpty : NamingException("Context not empty")
