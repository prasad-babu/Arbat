package com.metricstream.omg.naming;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the NamingContext interface.
 * This is the non-CORBA equivalent of the CORBA NamingContext implementation.
 */
public class NamingContextImpl implements NamingContext {
    
    /**
     * Map of bindings in this context, keyed by the string representation of the name component.
     */
    private final Map<String, BindingEntry> bindings = new ConcurrentHashMap<>();
    
    /**
     * Entry in the bindings map, containing the object and its binding type.
     */
    private static class BindingEntry {
        Object object;
        BindingType type;
        
        BindingEntry(Object object, BindingType type) {
            this.object = object;
            this.type = type;
        }
    }
    
    /**
     * Creates a new naming context implementation.
     */
    public NamingContextImpl() {
        // Nothing to initialize
    }
    
    @Override
    public void bind(Name n, Object obj) throws NotFound, CannotProceed, InvalidName, AlreadyBound {
        if (n == null || obj == null) {
            throw new InvalidName("Name or object is null");
        }
        
        if (n.size() == 0) {
            throw new InvalidName("Empty name");
        }
        
        if (n.size() == 1) {
            // Binding directly in this context
            NameComponent nc = n.get(0);
            String key = getKey(nc);
            
            synchronized (bindings) {
                if (bindings.containsKey(key)) {
                    throw new AlreadyBound("Name already bound: " + key);
                }
                
                BindingType type = (obj instanceof NamingContext) ? BindingType.ncontext : BindingType.nobject;
                bindings.put(key, new BindingEntry(obj, type));
            }
        } else {
            // Binding in a sub-context
            NameComponent nc = n.get(0);
            String key = getKey(nc);
            
            BindingEntry entry = bindings.get(key);
            if (entry == null) {
                throw new NotFound(NotFoundReason.missing_node, n);
            }
            
            if (entry.type != BindingType.ncontext) {
                throw new NotFound(NotFoundReason.not_context, n);
            }
            
            NamingContext subContext = (NamingContext) entry.object;
            subContext.bind(n.suffix(1), obj);
        }
    }
    
    @Override
    public void rebind(Name n, Object obj) throws NotFound, CannotProceed, InvalidName {
        if (n == null || obj == null) {
            throw new InvalidName("Name or object is null");
        }
        
        if (n.size() == 0) {
            throw new InvalidName("Empty name");
        }
        
        if (n.size() == 1) {
            // Rebinding directly in this context
            NameComponent nc = n.get(0);
            String key = getKey(nc);
            
            BindingType type = (obj instanceof NamingContext) ? BindingType.ncontext : BindingType.nobject;
            bindings.put(key, new BindingEntry(obj, type));
        } else {
            // Rebinding in a sub-context
            NameComponent nc = n.get(0);
            String key = getKey(nc);
            
            BindingEntry entry = bindings.get(key);
            if (entry == null) {
                throw new NotFound(NotFoundReason.missing_node, n);
            }
            
            if (entry.type != BindingType.ncontext) {
                throw new NotFound(NotFoundReason.not_context, n);
            }
            
            NamingContext subContext = (NamingContext) entry.object;
            subContext.rebind(n.suffix(1), obj);
        }
    }
    
    @Override
    public NamingContext bind_new_context(Name n) throws NotFound, CannotProceed, InvalidName, AlreadyBound {
        if (n == null) {
            throw new InvalidName("Name is null");
        }
        
        if (n.size() == 0) {
            throw new InvalidName("Empty name");
        }
        
        if (n.size() == 1) {
            // Creating a new context directly in this context
            NameComponent nc = n.get(0);
            String key = getKey(nc);
            
            synchronized (bindings) {
                if (bindings.containsKey(key)) {
                    throw new AlreadyBound("Name already bound: " + key);
                }
                
                NamingContext newContext = new NamingContextImpl();
                bindings.put(key, new BindingEntry(newContext, BindingType.ncontext));
                return newContext;
            }
        } else {
            // Creating a new context in a sub-context
            NameComponent nc = n.get(0);
            String key = getKey(nc);
            
            BindingEntry entry = bindings.get(key);
            if (entry == null) {
                throw new NotFound(NotFoundReason.missing_node, n);
            }
            
            if (entry.type != BindingType.ncontext) {
                throw new NotFound(NotFoundReason.not_context, n);
            }
            
            NamingContext subContext = (NamingContext) entry.object;
            return subContext.bind_new_context(n.suffix(1));
        }
    }
    
    @Override
    public Object resolve(Name n) throws NotFound, CannotProceed, InvalidName {
        if (n == null) {
            throw new InvalidName("Name is null");
        }
        
        if (n.size() == 0) {
            throw new InvalidName("Empty name");
        }
        
        if (n.size() == 1) {
            // Resolving directly in this context
            NameComponent nc = n.get(0);
            String key = getKey(nc);
            
            BindingEntry entry = bindings.get(key);
            if (entry == null) {
                throw new NotFound(NotFoundReason.missing_node, n);
            }
            
            return entry.object;
        } else {
            // Resolving in a sub-context
            NameComponent nc = n.get(0);
            String key = getKey(nc);
            
            BindingEntry entry = bindings.get(key);
            if (entry == null) {
                throw new NotFound(NotFoundReason.missing_node, n);
            }
            
            if (entry.type != BindingType.ncontext) {
                throw new NotFound(NotFoundReason.not_context, n);
            }
            
            NamingContext subContext = (NamingContext) entry.object;
            return subContext.resolve(n.suffix(1));
        }
    }
    
    @Override
    public void unbind(Name n) throws NotFound, CannotProceed, InvalidName {
        if (n == null) {
            throw new InvalidName("Name is null");
        }
        
        if (n.size() == 0) {
            throw new InvalidName("Empty name");
        }
        
        if (n.size() == 1) {
            // Unbinding directly in this context
            NameComponent nc = n.get(0);
            String key = getKey(nc);
            
            BindingEntry entry = bindings.remove(key);
            if (entry == null) {
                throw new NotFound(NotFoundReason.missing_node, n);
            }
        } else {
            // Unbinding in a sub-context
            NameComponent nc = n.get(0);
            String key = getKey(nc);
            
            BindingEntry entry = bindings.get(key);
            if (entry == null) {
                throw new NotFound(NotFoundReason.missing_node, n);
            }
            
            if (entry.type != BindingType.ncontext) {
                throw new NotFound(NotFoundReason.not_context, n);
            }
            
            NamingContext subContext = (NamingContext) entry.object;
            subContext.unbind(n.suffix(1));
        }
    }
    
    @Override
    public void list(int how_many, BindingListHolder bl, BindingIteratorHolder bi) {
        if (bl == null) {
            bl = new BindingListHolder(new BindingList());
        } else if (bl.value == null) {
            bl.value = new BindingList();
        }
        
        List<Binding> allBindings = new ArrayList<>();
        
        for (Map.Entry<String, BindingEntry> entry : bindings.entrySet()) {
            String key = entry.getKey();
            BindingEntry bindingEntry = entry.getValue();
            
            NameComponent nc = parseKey(key);
            Name name = new Name();
            name.addComponent(nc);
            
            Binding binding = new Binding(name, bindingEntry.type);
            allBindings.add(binding);
        }
        
        // Fill the binding list with up to how_many bindings
        int count = Math.min(how_many, allBindings.size());
        for (int i = 0; i < count; i++) {
            bl.value.add(allBindings.get(i));
        }
        
        // Create an iterator for the remaining bindings if necessary
        if (count < allBindings.size() && bi != null) {
            List<Binding> remainingBindings = allBindings.subList(count, allBindings.size());
            bi.value = new BindingIteratorImpl(remainingBindings);
        } else if (bi != null) {
            bi.value = null;
        }
    }
    
    @Override
    public void destroy() throws NotEmpty {
        if (!bindings.isEmpty()) {
            throw new NotEmpty("Cannot destroy non-empty naming context");
        }
    }
    
    /**
     * Gets a key for the bindings map from a name component.
     * 
     * @param nc The name component
     * @return The key
     */
    private String getKey(NameComponent nc) {
        if (nc.kind == null || nc.kind.isEmpty()) {
            return nc.id;
        } else {
            return nc.id + "." + nc.kind;
        }
    }
    
    /**
     * Parses a key from the bindings map into a name component.
     * 
     * @param key The key
     * @return The name component
     */
    private NameComponent parseKey(String key) {
        int dotIndex = key.indexOf('.');
        if (dotIndex >= 0) {
            String id = key.substring(0, dotIndex);
            String kind = key.substring(dotIndex + 1);
            return new NameComponent(id, kind);
        } else {
            return new NameComponent(key, "");
        }
    }
}
