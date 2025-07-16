package com.metricstream.naming

import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

/**
 * Implementation of the NamingContext interface.
 * Provides a thread-safe naming context using ConcurrentHashMap.
 */
class NamingContextImpl : NamingContext {
    private val bindings = ConcurrentHashMap<String, Pair<Any, BindingType>>()
    private val destroyed = AtomicBoolean(false)
    
    /**
     * Checks if this context has been destroyed.
     * @throws CannotProceed if the context has been destroyed
     */
    private fun checkDestroyed() {
        if (destroyed.get()) {
            throw CannotProceed(this, Name())
        }
    }
    
    /**
     * Validates that the name is not empty.
     * @param name The name to validate
     * @throws InvalidName if the name is empty
     */
    private fun validateName(name: Name) {
        if (name.size() == 0) {
            throw InvalidName()
        }
    }
    
    /**
     * Creates a key for the bindings map from a NameComponent.
     * @param component The name component
     * @return The key string
     */
    private fun createKey(component: NameComponent): String {
        return "${component.id}|${component.kind}"
    }
    
    override fun bind(name: Name, obj: Any) {
        checkDestroyed()
        validateName(name)
        
        if (name.size() == 1) {
            // Binding directly in this context
            val key = createKey(name.components[0])
            if (bindings.containsKey(key)) {
                throw AlreadyBound()
            }
            bindings[key] = obj to BindingType.OBJECT
            logger.debug { "Bound object to name: $name" }
        } else {
            // Need to traverse to the right context
            val firstComponent = name.components[0]
            val key = createKey(firstComponent)
            val pair = bindings[key] ?: throw NotFound(NotFoundReason.MISSING_NODE, name)
            
            if (pair.second != BindingType.CONTEXT) {
                throw NotFound(NotFoundReason.NOT_CONTEXT, name)
            }
            
            val context = pair.first as? NamingContext 
                ?: throw NotFound(NotFoundReason.NOT_CONTEXT, name)
            
            try {
                context.bind(name.suffix(1), obj)
            } catch (e: NotFound) {
                throw NotFound(e.reason, Name((listOf(firstComponent) + e.restOfName.components).toMutableList()))
            } catch (e: CannotProceed) {
                throw CannotProceed(this, name)
            }
        }
    }
    
    override fun bind_context(name: Name, context: NamingContext) {
        checkDestroyed()
        validateName(name)
        
        if (name.size() == 1) {
            // Binding directly in this context
            val key = createKey(name.components[0])
            if (bindings.containsKey(key)) {
                throw AlreadyBound()
            }
            bindings[key] = context to BindingType.CONTEXT
            logger.debug { "Bound context to name: $name" }
        } else {
            // Need to traverse to the right context
            val firstComponent = name.components[0]
            val key = createKey(firstComponent)
            val pair = bindings[key] ?: throw NotFound(NotFoundReason.MISSING_NODE, name)
            
            if (pair.second != BindingType.CONTEXT) {
                throw NotFound(NotFoundReason.NOT_CONTEXT, name)
            }
            
            val existingContext = pair.first as? NamingContext 
                ?: throw NotFound(NotFoundReason.NOT_CONTEXT, name)
            
            try {
                existingContext.bind_context(name.suffix(1), context)
            } catch (e: NotFound) {
                throw NotFound(e.reason, Name((listOf(firstComponent) + e.restOfName.components).toMutableList()))
            } catch (e: CannotProceed) {
                throw CannotProceed(this, name)
            }
        }
    }
    
    override fun rebind(name: Name, obj: Any) {
        checkDestroyed()
        validateName(name)
        
        try {
            unbind(name)
        } catch (e: NotFound) {
            // Ignore, we'll create it
        }
        
        try {
            bind(name, obj)
        } catch (e: AlreadyBound) {
            // This shouldn't happen as we just unbound it
            logger.error { "Unexpected AlreadyBound exception after unbind: $e" }
            throw CannotProceed(this, name)
        }
    }
    
    override fun rebind_context(name: Name, context: NamingContext) {
        checkDestroyed()
        validateName(name)
        
        try {
            unbind(name)
        } catch (e: NotFound) {
            // Ignore, we'll create it
        }
        
        try {
            bind_context(name, context)
        } catch (e: AlreadyBound) {
            // This shouldn't happen as we just unbound it
            logger.error { "Unexpected AlreadyBound exception after unbind: $e" }
            throw CannotProceed(this, name)
        }
    }
    
    override fun resolve(name: Name): Any {
        checkDestroyed()
        validateName(name)
        
        if (name.size() == 1) {
            // Resolving directly in this context
            val key = createKey(name.components[0])
            val pair = bindings[key] ?: throw NotFound(NotFoundReason.MISSING_NODE, name)
            return pair.first
        } else {
            // Need to traverse to the right context
            val firstComponent = name.components[0]
            val key = createKey(firstComponent)
            val pair = bindings[key] ?: throw NotFound(NotFoundReason.MISSING_NODE, name)
            
            if (pair.second != BindingType.CONTEXT) {
                throw NotFound(NotFoundReason.NOT_CONTEXT, name)
            }
            
            val context = pair.first as? NamingContext 
                ?: throw NotFound(NotFoundReason.NOT_CONTEXT, name)
            
            try {
                return context.resolve(name.suffix(1))
            } catch (e: NotFound) {
                throw NotFound(e.reason, Name((listOf(firstComponent) + e.restOfName.components).toMutableList()))
            } catch (e: CannotProceed) {
                throw CannotProceed(this, name)
            }
        }
    }
    
    override fun unbind(name: Name) {
        checkDestroyed()
        validateName(name)
        
        if (name.size() == 1) {
            // Unbinding directly in this context
            val key = createKey(name.components[0])
            if (!bindings.containsKey(key)) {
                throw NotFound(NotFoundReason.MISSING_NODE, name)
            }
            bindings.remove(key)
            logger.debug { "Unbound name: $name" }
        } else {
            // Need to traverse to the right context
            val firstComponent = name.components[0]
            val key = createKey(firstComponent)
            val pair = bindings[key] ?: throw NotFound(NotFoundReason.MISSING_NODE, name)
            
            if (pair.second != BindingType.CONTEXT) {
                throw NotFound(NotFoundReason.NOT_CONTEXT, name)
            }
            
            val context = pair.first as? NamingContext 
                ?: throw NotFound(NotFoundReason.NOT_CONTEXT, name)
            
            try {
                context.unbind(name.suffix(1))
            } catch (e: NotFound) {
                throw NotFound(e.reason, Name((listOf(firstComponent) + e.restOfName.components).toMutableList()))
            } catch (e: CannotProceed) {
                throw CannotProceed(this, name)
            }
        }
    }
    
    override fun bind_new_context(name: Name): NamingContext {
        checkDestroyed()
        validateName(name)
        
        val newContext = NamingContextImpl()
        bind_context(name, newContext)
        return newContext
    }
    
    override fun destroy() {
        checkDestroyed()
        
        if (!bindings.isEmpty()) {
            throw NotEmpty()
        }
        
        destroyed.set(true)
        logger.debug { "Context destroyed" }
    }
    
    override fun list(howMany: Int, bindingList: BindingList): BindingIterator? {
        checkDestroyed()
        
        val allBindings = bindings.map { (key, pair) ->
            val parts = key.split("|", limit = 2)
            val nameComponent = NameComponent(parts[0], parts[1])
            Binding(Name(mutableListOf(nameComponent)), pair.second)
        }
        
        val iterator = BindingIteratorImpl(allBindings)
        
        // Fill the binding list with the first howMany bindings
        iterator.next_n(howMany, bindingList)
        
        // Return the iterator only if there are more bindings
        return if (iterator.hasMore()) iterator else null
    }
}

/**
 * Implementation of the BindingIterator interface.
 */
class BindingIteratorImpl(private val bindings: List<Binding>) : BindingIterator {
    private var index = 0
    private val destroyed = AtomicBoolean(false)
    
    /**
     * Checks if this iterator has been destroyed.
     */
    private fun checkDestroyed() {
        if (destroyed.get()) {
            throw IllegalStateException("Iterator has been destroyed")
        }
    }
    
    /**
     * Checks if there are more bindings.
     * @return true if there are more bindings
     */
    fun hasMore(): Boolean {
        return index < bindings.size
    }
    
    override fun next_one(): Binding? {
        checkDestroyed()
        
        if (!hasMore()) {
            return null
        }
        
        return bindings[index++]
    }
    
    override fun next_n(howMany: Int, bindingList: BindingList): Boolean {
        checkDestroyed()
        
        var count = 0
        while (count < howMany && hasMore()) {
            next_one()?.let {
                bindingList.add(it)
                count++
            }
        }
        
        return hasMore()
    }
    
    override fun destroy() {
        destroyed.set(true)
    }
}
