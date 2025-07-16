package com.metricstream.omg.naming;

/**
 * Interface for the naming context, which is a container for name-to-object bindings.
 * This is the non-CORBA equivalent of org.omg.CosNaming.NamingContext.
 */
public interface NamingContext {

    /**
     * Binds a name to an object in this naming context.
     * 
     * @param n The name to bind
     * @param obj The object to bind
     * @throws NotFound If the name contains a component that does not exist
     * @throws CannotProceed If the implementation cannot proceed with the operation
     * @throws InvalidName If the name is invalid
     * @throws AlreadyBound If the name is already bound
     */
    void bind(Name n, Object obj) throws NotFound, CannotProceed, InvalidName, AlreadyBound;

    /**
     * Binds a name to an object in this naming context, even if the name is already bound.
     * 
     * @param n The name to bind
     * @param obj The object to bind
     * @throws NotFound If the name contains a component that does not exist
     * @throws CannotProceed If the implementation cannot proceed with the operation
     * @throws InvalidName If the name is invalid
     */
    void rebind(Name n, Object obj) throws NotFound, CannotProceed, InvalidName;

    /**
     * Creates a new naming context and binds it to the specified name.
     * 
     * @param n The name to bind
     * @return The newly created naming context
     * @throws NotFound If the name contains a component that does not exist
     * @throws CannotProceed If the implementation cannot proceed with the operation
     * @throws InvalidName If the name is invalid
     * @throws AlreadyBound If the name is already bound
     */
    NamingContext bind_new_context(Name n) throws NotFound, CannotProceed, InvalidName, AlreadyBound;

    /**
     * Resolves a name to an object.
     * 
     * @param n The name to resolve
     * @return The object bound to the name
     * @throws NotFound If the name cannot be found
     * @throws CannotProceed If the implementation cannot proceed with the operation
     * @throws InvalidName If the name is invalid
     */
    Object resolve(Name n) throws NotFound, CannotProceed, InvalidName;

    /**
     * Unbinds a name from an object.
     * 
     * @param n The name to unbind
     * @throws NotFound If the name cannot be found
     * @throws CannotProceed If the implementation cannot proceed with the operation
     * @throws InvalidName If the name is invalid
     */
    void unbind(Name n) throws NotFound, CannotProceed, InvalidName;

    /**
     * Lists the bindings in this naming context.
     * 
     * @param how_many The maximum number of bindings to return
     * @param bl The binding list to fill
     * @param bi The binding iterator to fill
     */
    void list(int how_many, BindingListHolder bl, BindingIteratorHolder bi);

    /**
     * Destroys this naming context.
     * 
     * @throws NotEmpty If the naming context is not empty
     */
    void destroy() throws NotEmpty;
}
