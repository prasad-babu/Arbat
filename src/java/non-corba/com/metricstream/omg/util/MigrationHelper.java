package com.metricstream.omg.util;

import com.metricstream.omg.naming.*;
import com.metricstream.omg.event.*;
import com.metricstream.omg.eventchannel.*;

/**
 * Utility class for converting between CORBA and non-CORBA types and providing
 * helper methods for migration.
 */
public class MigrationHelper {

    /**
     * Converts a CORBA NameComponent array to a non-CORBA Name object.
     * 
     * @param corbaName The CORBA NameComponent array
     * @return A non-CORBA Name object
     */
    public static Name convertToName(org.omg.CosNaming.NameComponent[] corbaName) {
        if (corbaName == null) {
            return null;
        }
        
        Name name = new Name();
        for (org.omg.CosNaming.NameComponent nc : corbaName) {
            name.addComponent(new NameComponent(nc.id, nc.kind));
        }
        
        return name;
    }
    
    /**
     * Converts a non-CORBA Name object to a CORBA NameComponent array.
     * 
     * @param name The non-CORBA Name object
     * @return A CORBA NameComponent array
     */
    public static org.omg.CosNaming.NameComponent[] convertToCorbaName(Name name) {
        if (name == null) {
            return null;
        }
        
        org.omg.CosNaming.NameComponent[] corbaName = 
            new org.omg.CosNaming.NameComponent[name.size()];
        
        for (int i = 0; i < name.size(); i++) {
            NameComponent nc = name.get(i);
            corbaName[i] = new org.omg.CosNaming.NameComponent(nc.id, nc.kind);
        }
        
        return corbaName;
    }
    
    /**
     * Converts a CORBA Any object to a Java Object.
     * 
     * @param any The CORBA Any object
     * @return A Java Object
     */
    public static Object convertToObject(org.omg.CORBA.Any any) {
        if (any == null) {
            return null;
        }
        
        // Extract the appropriate type based on the Any's type code
        org.omg.CORBA.TypeCode tc = any.type();
        
        try {
            switch (tc.kind().value()) {
                case org.omg.CORBA.TCKind._tk_null:
                    return null;
                case org.omg.CORBA.TCKind._tk_boolean:
                    return any.extract_boolean();
                case org.omg.CORBA.TCKind._tk_char:
                    return any.extract_char();
                case org.omg.CORBA.TCKind._tk_wchar:
                    return any.extract_wchar();
                case org.omg.CORBA.TCKind._tk_octet:
                    return any.extract_octet();
                case org.omg.CORBA.TCKind._tk_short:
                    return any.extract_short();
                case org.omg.CORBA.TCKind._tk_ushort:
                    return any.extract_ushort();
                case org.omg.CORBA.TCKind._tk_long:
                    return any.extract_long();
                case org.omg.CORBA.TCKind._tk_ulong:
                    return any.extract_ulong();
                case org.omg.CORBA.TCKind._tk_longlong:
                    return any.extract_longlong();
                case org.omg.CORBA.TCKind._tk_ulonglong:
                    return any.extract_ulonglong();
                case org.omg.CORBA.TCKind._tk_float:
                    return any.extract_float();
                case org.omg.CORBA.TCKind._tk_double:
                    return any.extract_double();
                case org.omg.CORBA.TCKind._tk_string:
                    return any.extract_string();
                case org.omg.CORBA.TCKind._tk_wstring:
                    return any.extract_wstring();
                case org.omg.CORBA.TCKind._tk_any:
                    return convertToObject(any.extract_any());
                case org.omg.CORBA.TCKind._tk_objref:
                    return any.extract_Object();
                default:
                    // For complex types, just return the Any itself
                    // In a real implementation, you would handle more types
                    return any;
            }
        } catch (Exception e) {
            // If extraction fails, return the Any itself
            return any;
        }
    }
    
    /**
     * Converts a Java Object to a CORBA Any object.
     * 
     * @param obj The Java Object
     * @param orb The ORB to use for creating the Any
     * @return A CORBA Any object
     */
    public static org.omg.CORBA.Any convertToAny(Object obj, org.omg.CORBA.ORB orb) {
        if (obj == null) {
            return null;
        }
        
        org.omg.CORBA.Any any = orb.create_any();
        
        if (obj instanceof Boolean) {
            any.insert_boolean((Boolean) obj);
        } else if (obj instanceof Character) {
            any.insert_char((Character) obj);
        } else if (obj instanceof Byte) {
            any.insert_octet((Byte) obj);
        } else if (obj instanceof Short) {
            any.insert_short((Short) obj);
        } else if (obj instanceof Integer) {
            any.insert_long((Integer) obj);
        } else if (obj instanceof Long) {
            any.insert_longlong((Long) obj);
        } else if (obj instanceof Float) {
            any.insert_float((Float) obj);
        } else if (obj instanceof Double) {
            any.insert_double((Double) obj);
        } else if (obj instanceof String) {
            any.insert_string((String) obj);
        } else if (obj instanceof org.omg.CORBA.Object) {
            any.insert_Object((org.omg.CORBA.Object) obj);
        } else if (obj instanceof org.omg.CORBA.Any) {
            any.insert_any((org.omg.CORBA.Any) obj);
        } else {
            // For complex types, you would need to handle them specifically
            // This is a simplified implementation
            any.insert_string(obj.toString());
        }
        
        return any;
    }
    
    /**
     * Creates a Name object from a path string.
     * Path components are separated by forward slashes.
     * 
     * @param path The path string (e.g., "context/subcontext/object")
     * @return A Name object
     */
    public static Name createNameFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return new Name();
        }
        
        Name name = new Name();
        String[] components = path.split("/");
        
        for (String component : components) {
            if (!component.isEmpty()) {
                // By default, use the component as id and empty string as kind
                String id = component;
                String kind = "";
                
                // If the component contains a dot, split it into id and kind
                int dotIndex = component.indexOf('.');
                if (dotIndex >= 0) {
                    id = component.substring(0, dotIndex);
                    kind = component.substring(dotIndex + 1);
                }
                
                name.addComponent(new NameComponent(id, kind));
            }
        }
        
        return name;
    }
    
    /**
     * Converts a Name object to a path string.
     * 
     * @param name The Name object
     * @return A path string (e.g., "context/subcontext/object")
     */
    public static String convertNameToPath(Name name) {
        if (name == null || name.size() == 0) {
            return "";
        }
        
        StringBuilder path = new StringBuilder();
        
        for (int i = 0; i < name.size(); i++) {
            NameComponent nc = name.get(i);
            
            if (i > 0) {
                path.append("/");
            }
            
            path.append(nc.id);
            
            if (nc.kind != null && !nc.kind.isEmpty()) {
                path.append(".").append(nc.kind);
            }
        }
        
        return path.toString();
    }
    
    /**
     * Resolves a CORBA object reference as a non-CORBA object.
     * This is a convenience method for narrowing CORBA objects.
     * 
     * @param obj The CORBA object reference
     * @param expectedType The expected Java class
     * @return The narrowed object
     * @throws ClassCastException if the object cannot be cast to the expected type
     */
    public static <T> T resolveObject(org.omg.CORBA.Object obj, Class<T> expectedType) {
        if (obj == null) {
            return null;
        }
        
        // In a real implementation, you would use the appropriate Helper class
        // This is a simplified implementation that assumes the object is already
        // of the expected type
        return expectedType.cast(obj);
    }
    
    /**
     * Converts a CORBA NotFoundReason to a non-CORBA NotFoundReason.
     * 
     * @param reason The CORBA NotFoundReason
     * @return The non-CORBA NotFoundReason
     */
    public static NotFoundReason convertNotFoundReason(org.omg.CosNaming.NamingContextPackage.NotFoundReason reason) {
        if (reason == null) {
            return null;
        }
        
        switch (reason.value()) {
            case org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.missing_node:
                return NotFoundReason.missing_node;
            case org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.not_context:
                return NotFoundReason.not_context;
            case org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.not_object:
                return NotFoundReason.not_object;
            default:
                return NotFoundReason.missing_node;
        }
    }
    
    /**
     * Converts a CORBA BindingType to a non-CORBA BindingType.
     * 
     * @param type The CORBA BindingType
     * @return The non-CORBA BindingType
     */
    public static BindingType convertBindingType(org.omg.CosNaming.BindingType type) {
        if (type == null) {
            return null;
        }
        
        switch (type.value()) {
            case org.omg.CosNaming.BindingTypeHelper.nobject:
                return BindingType.nobject;
            case org.omg.CosNaming.BindingTypeHelper.ncontext:
                return BindingType.ncontext;
            default:
                return BindingType.nobject;
        }
    }
    
    /**
     * Converts a CORBA Binding to a non-CORBA Binding.
     * 
     * @param binding The CORBA Binding
     * @return The non-CORBA Binding
     */
    public static Binding convertBinding(org.omg.CosNaming.Binding binding) {
        if (binding == null) {
            return null;
        }
        
        Name name = convertToName(binding.binding_name);
        BindingType type = convertBindingType(binding.binding_type);
        
        return new Binding(name, type);
    }
    
    /**
     * Converts a CORBA BindingList to a non-CORBA BindingList.
     * 
     * @param bindingList The CORBA BindingList
     * @return The non-CORBA BindingList
     */
    public static BindingList convertBindingList(org.omg.CosNaming.Binding[] bindingList) {
        if (bindingList == null) {
            return null;
        }
        
        BindingList list = new BindingList();
        
        for (org.omg.CosNaming.Binding binding : bindingList) {
            list.add(convertBinding(binding));
        }
        
        return list;
    }
    
    /**
     * Converts a non-CORBA exception to a CORBA exception.
     * This is useful when migrating code that catches CORBA exceptions.
     * 
     * @param ex The non-CORBA exception
     * @return The equivalent CORBA exception
     */
    public static org.omg.CORBA.UserException convertToCORBAException(Exception ex) {
        if (ex == null) {
            return null;
        }
        
        if (ex instanceof NotFound) {
            NotFound nf = (NotFound) ex;
            try {
                return new org.omg.CosNaming.NamingContextPackage.NotFound(
                    org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.from_int(nf.why.value()),
                    convertToCorbaName(nf.rest_of_name)
                );
            } catch (Exception e) {
                // Fallback
                return new org.omg.CORBA.UNKNOWN("NotFound: " + ex.getMessage());
            }
        } else if (ex instanceof CannotProceed) {
            CannotProceed cp = (CannotProceed) ex;
            try {
                return new org.omg.CosNaming.NamingContextPackage.CannotProceed(
                    null, // We can't convert NamingContext to CORBA NamingContext here
                    convertToCorbaName(cp.rest_of_name)
                );
            } catch (Exception e) {
                // Fallback
                return new org.omg.CORBA.UNKNOWN("CannotProceed: " + ex.getMessage());
            }
        } else if (ex instanceof InvalidName) {
            return new org.omg.CosNaming.NamingContextPackage.InvalidName();
        } else if (ex instanceof AlreadyBound) {
            return new org.omg.CosNaming.NamingContextPackage.AlreadyBound();
        } else if (ex instanceof Disconnected) {
            return new org.omg.CosEventComm.Disconnected();
        } else if (ex instanceof AlreadyConnected) {
            return new org.omg.CosEventChannelAdmin.AlreadyConnected();
        } else if (ex instanceof TypeError) {
            return new org.omg.CosEventChannelAdmin.TypeError();
        } else {
            // For other exceptions, wrap them in a CORBA UNKNOWN exception
            return new org.omg.CORBA.UNKNOWN(ex.getMessage());
        }
    }
    
    /**
     * Converts a CORBA exception to a non-CORBA exception.
     * This is useful when migrating code that throws CORBA exceptions.
     * 
     * @param ex The CORBA exception
     * @return The equivalent non-CORBA exception
     */
    public static Exception convertFromCORBAException(org.omg.CORBA.UserException ex) {
        if (ex == null) {
            return null;
        }
        
        if (ex instanceof org.omg.CosNaming.NamingContextPackage.NotFound) {
            org.omg.CosNaming.NamingContextPackage.NotFound nf = 
                (org.omg.CosNaming.NamingContextPackage.NotFound) ex;
            return new NotFound(
                convertNotFoundReason(nf.why),
                convertToName(nf.rest_of_name)
            );
        } else if (ex instanceof org.omg.CosNaming.NamingContextPackage.CannotProceed) {
            org.omg.CosNaming.NamingContextPackage.CannotProceed cp = 
                (org.omg.CosNaming.NamingContextPackage.CannotProceed) ex;
            return new CannotProceed(
                null, // We can't convert CORBA NamingContext to NamingContext here
                convertToName(cp.rest_of_name)
            );
        } else if (ex instanceof org.omg.CosNaming.NamingContextPackage.InvalidName) {
            return new InvalidName();
        } else if (ex instanceof org.omg.CosNaming.NamingContextPackage.AlreadyBound) {
            return new AlreadyBound();
        } else if (ex instanceof org.omg.CosEventComm.Disconnected) {
            return new Disconnected();
        } else if (ex instanceof org.omg.CosEventChannelAdmin.AlreadyConnected) {
            return new AlreadyConnected();
        } else if (ex instanceof org.omg.CosEventChannelAdmin.TypeError) {
            return new TypeError();
        } else {
            // For other exceptions, wrap them in a RuntimeException
            return new RuntimeException(ex.getMessage());
        }
    }
}
