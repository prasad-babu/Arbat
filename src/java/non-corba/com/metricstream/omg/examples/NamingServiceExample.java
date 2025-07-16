package com.metricstream.omg.examples;

import com.metricstream.omg.naming.*;
import com.metricstream.omg.util.MigrationHelper;

/**
 * Example application demonstrating the usage of the non-CORBA naming service.
 * This example shows how to:
 * - Create and bind objects in the naming service
 * - Create and use sub-contexts
 * - Resolve objects by name
 * - List bindings in a context
 * - Unbind objects
 * 
 * It also includes a migration snippet showing the comparison between CORBA and non-CORBA code.
 */
public class NamingServiceExample {

    public static void main(String[] args) {
        System.out.println("Starting NamingServiceExample...");
        
        try {
            // Get the naming service factory
            NamingServiceFactory factory = NamingServiceFactory.getInstance();
            NamingContext rootContext = factory.getRootContext();
            
            System.out.println("Obtained root naming context");
            
            // Example 1: Simple binding and resolving
            simpleBindingExample(rootContext);
            
            // Example 2: Working with sub-contexts
            subContextExample(rootContext);
            
            // Example 3: Listing bindings
            listBindingsExample(rootContext);
            
            // Example 4: Path-based binding and resolving
            pathBasedExample(rootContext);
            
            // Example 5: Migration comparison
            showMigrationComparison();
            
            System.out.println("\nNamingServiceExample completed successfully!");
        } catch (Exception e) {
            System.err.println("\nNamingServiceExample failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Example demonstrating simple binding and resolving of objects.
     */
    private static void simpleBindingExample(NamingContext rootContext) throws Exception {
        System.out.println("\n=== Simple Binding Example ===");
        
        // Create a test object to bind
        String testObject = "Hello, Naming Service!";
        
        // Create a name for the object
        Name name = new Name();
        name.addComponent(new NameComponent("hello", "string"));
        
        // Bind the object
        rootContext.bind(name, testObject);
        System.out.println("Bound string object to name 'hello.string'");
        
        // Resolve the object
        Object resolvedObj = rootContext.resolve(name);
        System.out.println("Resolved object: " + resolvedObj);
        
        // Rebind with a different object
        Integer intObject = 42;
        rootContext.rebind(name, intObject);
        System.out.println("Rebound integer object to name 'hello.string'");
        
        // Resolve again
        resolvedObj = rootContext.resolve(name);
        System.out.println("Resolved object after rebind: " + resolvedObj);
        
        // Unbind the object
        rootContext.unbind(name);
        System.out.println("Unbound object from name 'hello.string'");
        
        // Try to resolve (should fail)
        try {
            rootContext.resolve(name);
            System.out.println("ERROR: Object still exists after unbinding!");
        } catch (NotFound e) {
            System.out.println("Verified object no longer exists (expected NotFound exception)");
        }
    }
    
    /**
     * Example demonstrating working with sub-contexts.
     */
    private static void subContextExample(NamingContext rootContext) throws Exception {
        System.out.println("\n=== Sub-Context Example ===");
        
        // Create a sub-context
        Name contextName = new Name();
        contextName.addComponent(new NameComponent("department", "context"));
        
        NamingContext deptContext = rootContext.bind_new_context(contextName);
        System.out.println("Created sub-context 'department.context'");
        
        // Create a sub-sub-context
        Name subContextName = new Name();
        subContextName.addComponent(new NameComponent("engineering", "context"));
        
        NamingContext engContext = deptContext.bind_new_context(subContextName);
        System.out.println("Created sub-sub-context 'engineering.context' within 'department.context'");
        
        // Bind objects in different contexts
        String deptInfo = "Department Information";
        Name deptInfoName = new Name();
        deptInfoName.addComponent(new NameComponent("info", "string"));
        deptContext.bind(deptInfoName, deptInfo);
        System.out.println("Bound 'info.string' in department context");
        
        String engInfo = "Engineering Information";
        Name engInfoName = new Name();
        engInfoName.addComponent(new NameComponent("info", "string"));
        engContext.bind(engInfoName, engInfo);
        System.out.println("Bound 'info.string' in engineering context");
        
        // Resolve objects using their respective contexts
        Object deptObj = deptContext.resolve(deptInfoName);
        System.out.println("Resolved from department context: " + deptObj);
        
        Object engObj = engContext.resolve(engInfoName);
        System.out.println("Resolved from engineering context: " + engObj);
        
        // Resolve using compound names from root context
        Name compoundDeptName = new Name();
        compoundDeptName.addComponent(new NameComponent("department", "context"));
        compoundDeptName.addComponent(new NameComponent("info", "string"));
        
        Object compoundDeptObj = rootContext.resolve(compoundDeptName);
        System.out.println("Resolved department info using compound name: " + compoundDeptObj);
        
        Name compoundEngName = new Name();
        compoundEngName.addComponent(new NameComponent("department", "context"));
        compoundEngName.addComponent(new NameComponent("engineering", "context"));
        compoundEngName.addComponent(new NameComponent("info", "string"));
        
        Object compoundEngObj = rootContext.resolve(compoundEngName);
        System.out.println("Resolved engineering info using compound name: " + compoundEngObj);
        
        // Clean up
        engContext.unbind(engInfoName);
        deptContext.unbind(deptInfoName);
        deptContext.unbind(subContextName);
        rootContext.unbind(contextName);
        System.out.println("Cleaned up contexts and objects");
    }
    
    /**
     * Example demonstrating listing bindings in a context.
     */
    private static void listBindingsExample(NamingContext rootContext) throws Exception {
        System.out.println("\n=== List Bindings Example ===");
        
        // Create and bind several objects
        for (int i = 1; i <= 5; i++) {
            Name name = new Name();
            name.addComponent(new NameComponent("object" + i, "example"));
            rootContext.bind(name, "Example Object " + i);
            System.out.println("Bound 'object" + i + ".example'");
        }
        
        // Create a sub-context
        Name contextName = new Name();
        contextName.addComponent(new NameComponent("list", "context"));
        rootContext.bind_new_context(contextName);
        System.out.println("Created sub-context 'list.context'");
        
        // List all bindings
        System.out.println("\nListing all bindings in root context:");
        BindingListHolder blHolder = new BindingListHolder();
        BindingIteratorHolder biHolder = new BindingIteratorHolder();
        
        rootContext.list(100, blHolder, biHolder);
        
        BindingList bindingList = blHolder.value;
        BindingIterator bindingIterator = biHolder.value;
        
        // Print all bindings
        for (int i = 0; i < bindingList.size(); i++) {
            Binding binding = bindingList.get(i);
            Name bindingName = binding.binding_name;
            BindingType bindingType = binding.binding_type;
            
            String nameStr = "";
            for (int j = 0; j < bindingName.size(); j++) {
                NameComponent nc = bindingName.get(j);
                nameStr += nc.id;
                if (nc.kind != null && !nc.kind.isEmpty()) {
                    nameStr += "." + nc.kind;
                }
                if (j < bindingName.size() - 1) {
                    nameStr += "/";
                }
            }
            
            System.out.println("  " + nameStr + " (" + 
                              (bindingType == BindingType.ncontext ? "context" : "object") + ")");
        }
        
        // Clean up the binding iterator if it exists
        if (bindingIterator != null) {
            bindingIterator.destroy();
        }
        
        // Clean up
        for (int i = 1; i <= 5; i++) {
            Name name = new Name();
            name.addComponent(new NameComponent("object" + i, "example"));
            rootContext.unbind(name);
        }
        rootContext.unbind(contextName);
        System.out.println("\nCleaned up objects and context");
    }
    
    /**
     * Example demonstrating path-based binding and resolving.
     */
    private static void pathBasedExample(NamingContext rootContext) throws Exception {
        System.out.println("\n=== Path-Based Example ===");
        
        // Create a path-based name
        Name pathName = MigrationHelper.createNameFromPath("services/database/connection");
        System.out.println("Created name from path: services/database/connection");
        
        // Create intermediate contexts
        Name servicesPath = MigrationHelper.createNameFromPath("services");
        NamingContext servicesContext = rootContext.bind_new_context(servicesPath);
        System.out.println("Created 'services' context");
        
        Name dbPath = MigrationHelper.createNameFromPath("database");
        NamingContext dbContext = servicesContext.bind_new_context(dbPath);
        System.out.println("Created 'database' context under 'services'");
        
        // Bind an object
        String connectionString = "jdbc:mysql://localhost:3306/mydb";
        Name connName = MigrationHelper.createNameFromPath("connection");
        dbContext.bind(connName, connectionString);
        System.out.println("Bound connection string to 'connection' in database context");
        
        // Resolve using the full path
        Object resolvedObj = rootContext.resolve(pathName);
        System.out.println("Resolved object using path: " + resolvedObj);
        
        // Convert name to path string
        String path = MigrationHelper.convertNameToPath(pathName);
        System.out.println("Converted name back to path: " + path);
        
        // Clean up
        dbContext.unbind(connName);
        servicesContext.unbind(dbPath);
        rootContext.unbind(servicesPath);
        System.out.println("Cleaned up contexts and objects");
    }
    
    /**
     * Shows a comparison between CORBA and non-CORBA naming service usage.
     */
    private static void showMigrationComparison() {
        System.out.println("\n=== Migration Comparison ===");
        
        System.out.println("CORBA Naming Service Example:");
        System.out.println("```java");
        System.out.println("// Initialize the ORB");
        System.out.println("ORB orb = ORB.init(args, null);");
        System.out.println("");
        System.out.println("// Get the root naming context");
        System.out.println("org.omg.CORBA.Object objRef = orb.resolve_initial_references(\"NameService\");");
        System.out.println("NamingContext rootContext = NamingContextHelper.narrow(objRef);");
        System.out.println("");
        System.out.println("// Create a name");
        System.out.println("NameComponent[] name = new NameComponent[1];");
        System.out.println("name[0] = new NameComponent(\"Example\", \"Object\");");
        System.out.println("");
        System.out.println("// Bind an object");
        System.out.println("rootContext.bind(name, servant);");
        System.out.println("");
        System.out.println("// Resolve an object");
        System.out.println("org.omg.CORBA.Object obj = rootContext.resolve(name);");
        System.out.println("ExampleInterface example = ExampleInterfaceHelper.narrow(obj);");
        System.out.println("```");
        
        System.out.println("\nNon-CORBA Naming Service Example:");
        System.out.println("```java");
        System.out.println("// Get the naming service factory");
        System.out.println("NamingServiceFactory factory = NamingServiceFactory.getInstance();");
        System.out.println("NamingContext rootContext = factory.getRootContext();");
        System.out.println("");
        System.out.println("// Create a name");
        System.out.println("Name name = new Name();");
        System.out.println("name.addComponent(new NameComponent(\"Example\", \"Object\"));");
        System.out.println("");
        System.out.println("// Bind an object");
        System.out.println("rootContext.bind(name, object);");
        System.out.println("");
        System.out.println("// Resolve an object");
        System.out.println("Object obj = rootContext.resolve(name);");
        System.out.println("ExampleInterface example = (ExampleInterface) obj;");
        System.out.println("```");
        
        System.out.println("\nKey Differences:");
        System.out.println("1. No need for ORB initialization");
        System.out.println("2. Direct Java object references instead of CORBA object references");
        System.out.println("3. No need for Helper classes to narrow objects");
        System.out.println("4. Simplified name creation with Name class");
        System.out.println("5. Factory pattern for obtaining the naming service");
    }
}
