package com.metricstream.omg.test;

import com.metricstream.omg.naming.*;
import com.metricstream.omg.event.*;
import com.metricstream.omg.eventchannel.*;
import com.metricstream.omg.util.MigrationHelper;

/**
 * Test class for verifying the functionality of the MigrationHelper utility.
 */
public class MigrationHelperTest {

    public static void main(String[] args) {
        System.out.println("Starting MigrationHelperTest...");
        
        try {
            // Test name conversion methods
            testNameConversion();
            
            // Test path conversion methods
            testPathConversion();
            
            // Test binding type conversion
            testBindingTypeConversion();
            
            // Test exception conversion
            testExceptionConversion();
            
            System.out.println("\nAll MigrationHelperTest tests passed successfully!");
        } catch (Exception e) {
            System.err.println("\nMigrationHelperTest failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tests the name conversion methods in MigrationHelper.
     */
    private static void testNameConversion() throws Exception {
        System.out.println("\n=== Testing Name Conversion ===");
        
        // Create a non-CORBA Name
        Name name = new Name();
        name.addComponent(new NameComponent("test", "object"));
        name.addComponent(new NameComponent("sub", "context"));
        
        System.out.println("Created non-CORBA Name with components: test.object, sub.context");
        
        // Convert to CORBA NameComponent array
        org.omg.CosNaming.NameComponent[] corbaName = MigrationHelper.convertToCorbaName(name);
        
        // Verify conversion
        if (corbaName == null) {
            throw new AssertionError("Converted CORBA name is null");
        }
        
        if (corbaName.length != 2) {
            throw new AssertionError("Expected 2 components, but got " + corbaName.length);
        }
        
        if (!corbaName[0].id.equals("test") || !corbaName[0].kind.equals("object")) {
            throw new AssertionError("First component mismatch: " + corbaName[0].id + "." + corbaName[0].kind);
        }
        
        if (!corbaName[1].id.equals("sub") || !corbaName[1].kind.equals("context")) {
            throw new AssertionError("Second component mismatch: " + corbaName[1].id + "." + corbaName[1].kind);
        }
        
        System.out.println("Successfully converted to CORBA NameComponent array");
        
        // Convert back to non-CORBA Name
        Name convertedName = MigrationHelper.convertToName(corbaName);
        
        // Verify conversion
        if (convertedName == null) {
            throw new AssertionError("Converted non-CORBA name is null");
        }
        
        if (convertedName.size() != 2) {
            throw new AssertionError("Expected 2 components, but got " + convertedName.size());
        }
        
        NameComponent nc1 = convertedName.get(0);
        if (!nc1.id.equals("test") || !nc1.kind.equals("object")) {
            throw new AssertionError("First component mismatch: " + nc1.id + "." + nc1.kind);
        }
        
        NameComponent nc2 = convertedName.get(1);
        if (!nc2.id.equals("sub") || !nc2.kind.equals("context")) {
            throw new AssertionError("Second component mismatch: " + nc2.id + "." + nc2.kind);
        }
        
        System.out.println("Successfully converted back to non-CORBA Name");
        
        // Test null handling
        Name nullName = null;
        org.omg.CosNaming.NameComponent[] nullCorbaName = MigrationHelper.convertToCorbaName(nullName);
        
        if (nullCorbaName != null) {
            throw new AssertionError("Expected null result for null input in convertToCorbaName");
        }
        
        org.omg.CosNaming.NameComponent[] nullCorbaNameArray = null;
        Name nullConvertedName = MigrationHelper.convertToName(nullCorbaNameArray);
        
        if (nullConvertedName != null) {
            throw new AssertionError("Expected null result for null input in convertToName");
        }
        
        System.out.println("Successfully handled null inputs");
        
        System.out.println("Name conversion test passed");
    }
    
    /**
     * Tests the path conversion methods in MigrationHelper.
     */
    private static void testPathConversion() throws Exception {
        System.out.println("\n=== Testing Path Conversion ===");
        
        // Test createNameFromPath
        String path = "context/subcontext/object";
        Name name = MigrationHelper.createNameFromPath(path);
        
        System.out.println("Created Name from path: " + path);
        
        // Verify conversion
        if (name == null) {
            throw new AssertionError("Created name is null");
        }
        
        if (name.size() != 3) {
            throw new AssertionError("Expected 3 components, but got " + name.size());
        }
        
        NameComponent nc1 = name.get(0);
        if (!nc1.id.equals("context") || !nc1.kind.equals("")) {
            throw new AssertionError("First component mismatch: " + nc1.id + "." + nc1.kind);
        }
        
        NameComponent nc2 = name.get(1);
        if (!nc2.id.equals("subcontext") || !nc2.kind.equals("")) {
            throw new AssertionError("Second component mismatch: " + nc2.id + "." + nc2.kind);
        }
        
        NameComponent nc3 = name.get(2);
        if (!nc3.id.equals("object") || !nc3.kind.equals("")) {
            throw new AssertionError("Third component mismatch: " + nc3.id + "." + nc3.kind);
        }
        
        System.out.println("Successfully created Name from path");
        
        // Test path with id.kind format
        String pathWithKind = "context.ctx/subcontext.sub/object.obj";
        Name nameWithKind = MigrationHelper.createNameFromPath(pathWithKind);
        
        System.out.println("Created Name from path with kinds: " + pathWithKind);
        
        // Verify conversion
        if (nameWithKind == null) {
            throw new AssertionError("Created name is null");
        }
        
        if (nameWithKind.size() != 3) {
            throw new AssertionError("Expected 3 components, but got " + nameWithKind.size());
        }
        
        nc1 = nameWithKind.get(0);
        if (!nc1.id.equals("context") || !nc1.kind.equals("ctx")) {
            throw new AssertionError("First component mismatch: " + nc1.id + "." + nc1.kind);
        }
        
        nc2 = nameWithKind.get(1);
        if (!nc2.id.equals("subcontext") || !nc2.kind.equals("sub")) {
            throw new AssertionError("Second component mismatch: " + nc2.id + "." + nc2.kind);
        }
        
        nc3 = nameWithKind.get(2);
        if (!nc3.id.equals("object") || !nc3.kind.equals("obj")) {
            throw new AssertionError("Third component mismatch: " + nc3.id + "." + nc3.kind);
        }
        
        System.out.println("Successfully created Name from path with kinds");
        
        // Test convertNameToPath
        String convertedPath = MigrationHelper.convertNameToPath(nameWithKind);
        
        System.out.println("Converted Name back to path: " + convertedPath);
        
        // Verify conversion
        if (!convertedPath.equals("context.ctx/subcontext.sub/object.obj")) {
            throw new AssertionError("Path mismatch: " + convertedPath);
        }
        
        System.out.println("Successfully converted Name to path");
        
        // Test empty path
        Name emptyName = MigrationHelper.createNameFromPath("");
        
        if (emptyName == null) {
            throw new AssertionError("Created name is null for empty path");
        }
        
        if (emptyName.size() != 0) {
            throw new AssertionError("Expected 0 components for empty path, but got " + emptyName.size());
        }
        
        String emptyPath = MigrationHelper.convertNameToPath(emptyName);
        
        if (!emptyPath.equals("")) {
            throw new AssertionError("Expected empty path, but got " + emptyPath);
        }
        
        System.out.println("Successfully handled empty path");
        
        // Test null path
        Name nullName = MigrationHelper.createNameFromPath(null);
        
        if (nullName == null) {
            throw new AssertionError("Created name is null for null path");
        }
        
        if (nullName.size() != 0) {
            throw new AssertionError("Expected 0 components for null path, but got " + nullName.size());
        }
        
        String nullPath = MigrationHelper.convertNameToPath(null);
        
        if (!nullPath.equals("")) {
            throw new AssertionError("Expected empty path for null name, but got " + nullPath);
        }
        
        System.out.println("Successfully handled null path");
        
        System.out.println("Path conversion test passed");
    }
    
    /**
     * Tests the binding type conversion methods in MigrationHelper.
     */
    private static void testBindingTypeConversion() throws Exception {
        System.out.println("\n=== Testing Binding Type Conversion ===");
        
        // Test NotFoundReason conversion
        org.omg.CosNaming.NamingContextPackage.NotFoundReason missingNode = 
            org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.from_int(
                org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.missing_node);
        
        NotFoundReason convertedReason = MigrationHelper.convertNotFoundReason(missingNode);
        
        if (convertedReason != NotFoundReason.missing_node) {
            throw new AssertionError("NotFoundReason mismatch: " + convertedReason);
        }
        
        System.out.println("Successfully converted NotFoundReason.missing_node");
        
        org.omg.CosNaming.NamingContextPackage.NotFoundReason notContext = 
            org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.from_int(
                org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.not_context);
        
        convertedReason = MigrationHelper.convertNotFoundReason(notContext);
        
        if (convertedReason != NotFoundReason.not_context) {
            throw new AssertionError("NotFoundReason mismatch: " + convertedReason);
        }
        
        System.out.println("Successfully converted NotFoundReason.not_context");
        
        org.omg.CosNaming.NamingContextPackage.NotFoundReason notObject = 
            org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.from_int(
                org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.not_object);
        
        convertedReason = MigrationHelper.convertNotFoundReason(notObject);
        
        if (convertedReason != NotFoundReason.not_object) {
            throw new AssertionError("NotFoundReason mismatch: " + convertedReason);
        }
        
        System.out.println("Successfully converted NotFoundReason.not_object");
        
        // Test BindingType conversion
        org.omg.CosNaming.BindingType nobject = 
            org.omg.CosNaming.BindingTypeHelper.from_int(
                org.omg.CosNaming.BindingTypeHelper.nobject);
        
        BindingType convertedType = MigrationHelper.convertBindingType(nobject);
        
        if (convertedType != BindingType.nobject) {
            throw new AssertionError("BindingType mismatch: " + convertedType);
        }
        
        System.out.println("Successfully converted BindingType.nobject");
        
        org.omg.CosNaming.BindingType ncontext = 
            org.omg.CosNaming.BindingTypeHelper.from_int(
                org.omg.CosNaming.BindingTypeHelper.ncontext);
        
        convertedType = MigrationHelper.convertBindingType(ncontext);
        
        if (convertedType != BindingType.ncontext) {
            throw new AssertionError("BindingType mismatch: " + convertedType);
        }
        
        System.out.println("Successfully converted BindingType.ncontext");
        
        // Test null handling
        convertedReason = MigrationHelper.convertNotFoundReason(null);
        
        if (convertedReason != null) {
            throw new AssertionError("Expected null result for null input in convertNotFoundReason");
        }
        
        convertedType = MigrationHelper.convertBindingType(null);
        
        if (convertedType != null) {
            throw new AssertionError("Expected null result for null input in convertBindingType");
        }
        
        System.out.println("Successfully handled null inputs");
        
        System.out.println("Binding type conversion test passed");
    }
    
    /**
     * Tests the exception conversion methods in MigrationHelper.
     */
    private static void testExceptionConversion() throws Exception {
        System.out.println("\n=== Testing Exception Conversion ===");
        
        // Create a non-CORBA NotFound exception
        Name name = new Name();
        name.addComponent(new NameComponent("test", "object"));
        
        NotFound notFound = new NotFound(NotFoundReason.missing_node, name);
        
        // Convert to CORBA exception
        org.omg.CORBA.UserException corbaEx = MigrationHelper.convertToCORBAException(notFound);
        
        // Verify conversion
        if (!(corbaEx instanceof org.omg.CosNaming.NamingContextPackage.NotFound)) {
            throw new AssertionError("Expected NotFound exception, but got " + corbaEx.getClass().getName());
        }
        
        org.omg.CosNaming.NamingContextPackage.NotFound corbaNotFound = 
            (org.omg.CosNaming.NamingContextPackage.NotFound) corbaEx;
        
        if (corbaNotFound.why.value() != org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.missing_node) {
            throw new AssertionError("NotFoundReason mismatch: " + corbaNotFound.why.value());
        }
        
        if (corbaNotFound.rest_of_name.length != 1) {
            throw new AssertionError("Expected 1 component in rest_of_name, but got " + corbaNotFound.rest_of_name.length);
        }
        
        if (!corbaNotFound.rest_of_name[0].id.equals("test") || !corbaNotFound.rest_of_name[0].kind.equals("object")) {
            throw new AssertionError("Component mismatch: " + corbaNotFound.rest_of_name[0].id + "." + corbaNotFound.rest_of_name[0].kind);
        }
        
        System.out.println("Successfully converted NotFound exception to CORBA exception");
        
        // Convert back to non-CORBA exception
        Exception nonCorbaEx = MigrationHelper.convertFromCORBAException(corbaNotFound);
        
        // Verify conversion
        if (!(nonCorbaEx instanceof NotFound)) {
            throw new AssertionError("Expected NotFound exception, but got " + nonCorbaEx.getClass().getName());
        }
        
        NotFound convertedNotFound = (NotFound) nonCorbaEx;
        
        if (convertedNotFound.why != NotFoundReason.missing_node) {
            throw new AssertionError("NotFoundReason mismatch: " + convertedNotFound.why);
        }
        
        if (convertedNotFound.rest_of_name.size() != 1) {
            throw new AssertionError("Expected 1 component in rest_of_name, but got " + convertedNotFound.rest_of_name.size());
        }
        
        NameComponent nc = convertedNotFound.rest_of_name.get(0);
        if (!nc.id.equals("test") || !nc.kind.equals("object")) {
            throw new AssertionError("Component mismatch: " + nc.id + "." + nc.kind);
        }
        
        System.out.println("Successfully converted CORBA exception back to non-CORBA exception");
        
        // Test other exceptions
        InvalidName invalidName = new InvalidName();
        corbaEx = MigrationHelper.convertToCORBAException(invalidName);
        
        if (!(corbaEx instanceof org.omg.CosNaming.NamingContextPackage.InvalidName)) {
            throw new AssertionError("Expected InvalidName exception, but got " + corbaEx.getClass().getName());
        }
        
        System.out.println("Successfully converted InvalidName exception");
        
        AlreadyBound alreadyBound = new AlreadyBound();
        corbaEx = MigrationHelper.convertToCORBAException(alreadyBound);
        
        if (!(corbaEx instanceof org.omg.CosNaming.NamingContextPackage.AlreadyBound)) {
            throw new AssertionError("Expected AlreadyBound exception, but got " + corbaEx.getClass().getName());
        }
        
        System.out.println("Successfully converted AlreadyBound exception");
        
        Disconnected disconnected = new Disconnected();
        corbaEx = MigrationHelper.convertToCORBAException(disconnected);
        
        if (!(corbaEx instanceof org.omg.CosEventComm.Disconnected)) {
            throw new AssertionError("Expected Disconnected exception, but got " + corbaEx.getClass().getName());
        }
        
        System.out.println("Successfully converted Disconnected exception");
        
        AlreadyConnected alreadyConnected = new AlreadyConnected();
        corbaEx = MigrationHelper.convertToCORBAException(alreadyConnected);
        
        if (!(corbaEx instanceof org.omg.CosEventChannelAdmin.AlreadyConnected)) {
            throw new AssertionError("Expected AlreadyConnected exception, but got " + corbaEx.getClass().getName());
        }
        
        System.out.println("Successfully converted AlreadyConnected exception");
        
        // Test null handling
        corbaEx = MigrationHelper.convertToCORBAException(null);
        
        if (corbaEx != null) {
            throw new AssertionError("Expected null result for null input in convertToCORBAException");
        }
        
        nonCorbaEx = MigrationHelper.convertFromCORBAException(null);
        
        if (nonCorbaEx != null) {
            throw new AssertionError("Expected null result for null input in convertFromCORBAException");
        }
        
        System.out.println("Successfully handled null inputs");
        
        System.out.println("Exception conversion test passed");
    }
}
