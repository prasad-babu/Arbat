package com.metricstream.naming

/**
 * Interface for a naming context.
 * Equivalent to CosNaming::NamingContext in CORBA.
 */
interface NamingContext {
    /**
     * Binds a name to an object.
     * 
     * @param name The name to bind
     * @param obj The object to bind
     * @throws NotFound if a component of the name does not exist
     * @throws CannotProceed if the implementation cannot proceed
     * @throws InvalidName if the name is invalid
     * @throws AlreadyBound if the name is already bound
     */
    @Throws(NotFound::class, CannotProceed::class, InvalidName::class, AlreadyBound::class)
    fun bind(name: Name, obj: Any)
    
    /**
     * Binds a name to a naming context.
     * 
     * @param name The name to bind
     * @param context The naming context to bind
     * @throws NotFound if a component of the name does not exist
     * @throws CannotProceed if the implementation cannot proceed
     * @throws InvalidName if the name is invalid
     * @throws AlreadyBound if the name is already bound
     */
    @Throws(NotFound::class, CannotProceed::class, InvalidName::class, AlreadyBound::class)
    fun bind_context(name: Name, context: NamingContext)
    
    /**
     * Rebinds a name to an object.
     * 
     * @param name The name to rebind
     * @param obj The object to rebind
     * @throws NotFound if a component of the name does not exist
     * @throws CannotProceed if the implementation cannot proceed
     * @throws InvalidName if the name is invalid
     */
    @Throws(NotFound::class, CannotProceed::class, InvalidName::class)
    fun rebind(name: Name, obj: Any)
    
    /**
     * Rebinds a name to a naming context.
     * 
     * @param name The name to rebind
     * @param context The naming context to rebind
     * @throws NotFound if a component of the name does not exist
     * @throws CannotProceed if the implementation cannot proceed
     * @throws InvalidName if the name is invalid
     */
    @Throws(NotFound::class, CannotProceed::class, InvalidName::class)
    fun rebind_context(name: Name, context: NamingContext)
    
    /**
     * Resolves a name to an object.
     * 
     * @param name The name to resolve
     * @return The object bound to the name
     * @throws NotFound if the name does not exist
     * @throws CannotProceed if the implementation cannot proceed
     * @throws InvalidName if the name is invalid
     */
    @Throws(NotFound::class, CannotProceed::class, InvalidName::class)
    fun resolve(name: Name): Any
    
    /**
     * Unbinds a name.
     * 
     * @param name The name to unbind
     * @throws NotFound if the name does not exist
     * @throws CannotProceed if the implementation cannot proceed
     * @throws InvalidName if the name is invalid
     */
    @Throws(NotFound::class, CannotProceed::class, InvalidName::class)
    fun unbind(name: Name)
    
    /**
     * Creates a new context and binds it to the specified name.
     * 
     * @param name The name to bind
     * @return The new naming context
     * @throws NotFound if a component of the name does not exist
     * @throws CannotProceed if the implementation cannot proceed
     * @throws InvalidName if the name is invalid
     * @throws AlreadyBound if the name is already bound
     */
    @Throws(NotFound::class, CannotProceed::class, InvalidName::class, AlreadyBound::class)
    fun bind_new_context(name: Name): NamingContext
    
    /**
     * Destroys this naming context.
     * 
     * @throws NotEmpty if the context is not empty
     */
    @Throws(NotEmpty::class)
    fun destroy()
    
    /**
     * Lists the bindings in this naming context.
     * 
     * @param howMany The maximum number of bindings to return
     * @param bindingList The list to fill with bindings
     * @param iterator An iterator for the remaining bindings
     * @return true if there are more bindings
     */
    fun list(howMany: Int, bindingList: BindingList): BindingIterator?
}

/**
 * Interface for iterating over bindings in a naming context.
 * Equivalent to CosNaming::BindingIterator in CORBA.
 */
interface BindingIterator {
    /**
     * Returns the next binding.
     * 
     * @return The next binding, or null if there are no more bindings
     */
    fun next_one(): Binding?
    
    /**
     * Returns the next n bindings.
     * 
     * @param howMany The maximum number of bindings to return
     * @param bindingList The list to fill with bindings
     * @return true if there are more bindings
     */
    fun next_n(howMany: Int, bindingList: BindingList): Boolean
    
    /**
     * Destroys this iterator.
     */
    fun destroy()
}
