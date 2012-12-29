package org.hamcrest.generator;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

public final class QDoxFactoryReaderBaseTest extends TestCase {

    public void testIteratesOverFactoryMethods() {
        final String sourceCode = "" +
                "package org;\n" +
                "public static class SimpleSetOfMatchers {\n" +
                "  @org.hamcrest.Factory\n" +
                "  public static org.hamcrest.Matcher<String> firstMethod() { return null; }\n" +
                "  @org.hamcrest.Factory\n" +
                "  public static org.hamcrest.Matcher<String> secondMethod() { return null; }\n" +
                "}\n";
        
        Iterable<FactoryMethod> reader = qDoxReaderFor("org.SimpleSetOfMatchers", sourceCode);
        Iterator<FactoryMethod> methods = reader.iterator();

        assertTrue("Expected first method", methods.hasNext());
        FactoryMethod firstMethod = methods.next();
        assertEquals("firstMethod", firstMethod.getName());
        assertEquals("org.SimpleSetOfMatchers", firstMethod.getMatcherClass());

        assertTrue("Expected second method", methods.hasNext());
        FactoryMethod secondMethod = methods.next();
        assertEquals("secondMethod", secondMethod.getName());
        assertEquals("org.SimpleSetOfMatchers", secondMethod.getMatcherClass());

        assertFalse("Expected no more methods", methods.hasNext());
    }

    public void testOnlyReadsPublicStaticAnnotatedMethodsThatReturnNonVoid() {
        final String sourceCode = "" +
                "package org;\n" +
                "public static class MatchersWithDodgySignatures {\n" +
                "  @org.hamcrest.Factory\n" +
                "  public org.hamcrest.Matcher<String> notStatic() { return null; }\n" +
                "  @org.hamcrest.Factory\n" +
                "  static org.hamcrest.Matcher<String> notPublic() { return null; }\n" +
                "  public static org.hamcrest.Matcher<String> noAnnotation() { return null; }\n" +
                "  @org.hamcrest.Factory\n" +
                "  public static org.hamcrest.Matcher<String> goodMethod() { return null; }\n" +
                "  @org.hamcrest.Factory\n" +
                "  public static String anotherGoodMethod() { return null; }\n" +
                "  @org.hamcrest.Factory\n" +
                "  public static void wrongReturnType() { }\n" +
                "}\n";
        
        Iterable<FactoryMethod> reader = qDoxReaderFor("org.MatchersWithDodgySignatures", sourceCode);
        Iterator<FactoryMethod> methods = reader.iterator();

        assertTrue("Expected first method", methods.hasNext());
        assertEquals("goodMethod", methods.next().getName());

        assertTrue("Expected second method", methods.hasNext());
        assertEquals("anotherGoodMethod", methods.next().getName());

        assertFalse("Expected no more methods", methods.hasNext());
    }

    private final String generifiedMatchersSrc = "" +
            "package org;\n" +
            "public static class GenerifiedMatchers {\n" +
            "  @org.hamcrest.Factory\n" +
            "  public static org.hamcrest.Matcher<Comparator<String>> generifiedType() { return null; }\n" +
            "  @org.hamcrest.Factory\n" +
            "  public static org.hamcrest.Matcher noGenerifiedType() { return null; }\n" +
            "  @org.hamcrest.Factory\n" +
            "  public static org.hamcrest.Matcher<java.util.Map<? extends java.util.Set<Long>, org.hamcrest.Factory>> crazyType() { return null; }\n" +
            "}\n";
    
    public void testReadsFullyQualifiedGenericType() {
        FactoryMethod method = readMethod("org.GenerifiedMatchers", generifiedMatchersSrc, "generifiedType");
        assertEquals("Comparator<java.lang.String>", method.getGenerifiedType());
    }

    public void testReadsNullGenerifiedTypeIfNotPresent() {
        FactoryMethod method = readMethod("org.GenerifiedMatchers", generifiedMatchersSrc, "noGenerifiedType");
        assertNull(method.getGenerifiedType());
    }

    public void testReadsGenericsInGenericType() {
        FactoryMethod method = readMethod("org.GenerifiedMatchers", generifiedMatchersSrc, "crazyType");
        assertEquals("java.util.Map<? extends java.util.Set<java.lang.Long>,org.hamcrest.Factory>",
                     method.getGenerifiedType());
    }

    private final String paramterizedMatchersSrc = "" +
            "package org;\n" +
            "public static class ParamterizedMatchers {\n" +
            "  @org.hamcrest.Factory\n" +
            "  public static org.hamcrest.Matcher<String> withParam(String someString, int[] numbers, java.util.Collection<Object> things) { return null; }\n" +
            "  @org.hamcrest.Factory\n" +
            "  public static org.hamcrest.Matcher<String> withArray(String[] array) { return null; }\n" +
            "  @org.hamcrest.Factory\n" +
            "  public static org.hamcrest.Matcher<String> withVarArgs(String... things) { return null; }\n" +
            "  @org.hamcrest.Factory\n" +
            "  public static org.hamcrest.Matcher<String> withGenerifiedParam(java.util.Collection<? extends Comparable<String>> things, java.util.Set<String[]>[] x) { return null; }\n" +
            "}\n";

    public void testReadsParameterTypes() {
        FactoryMethod method = readMethod("org.ParamterizedMatchers", paramterizedMatchersSrc, "withParam");
        List<FactoryMethod.Parameter> params = method.getParameters();
        assertEquals(3, params.size());

        assertEquals("java.lang.String", params.get(0).getType());
        assertEquals("int[]", params.get(1).getType());
        assertEquals("java.util.Collection<java.lang.Object>", params.get(2).getType());
    }

    public void testReadsArrayAndVarArgParameterTypes() {
        FactoryMethod arrayMethod = readMethod("org.ParamterizedMatchers", paramterizedMatchersSrc, "withArray");
        assertEquals("java.lang.String[]", arrayMethod.getParameters().get(0).getType());

        FactoryMethod varArgsMethod = readMethod("org.ParamterizedMatchers", paramterizedMatchersSrc, "withVarArgs");
        assertEquals("java.lang.String...", varArgsMethod.getParameters().get(0).getType());
    }

    public void testReadsGenerifiedParameterTypes() {
        FactoryMethod method = readMethod("org.ParamterizedMatchers", paramterizedMatchersSrc, "withGenerifiedParam");
        assertEquals("java.util.Collection<? extends java.lang.Comparable<java.lang.String>>",
                method.getParameters().get(0).getType());
        assertEquals("java.util.Set<java.lang.String[]>[]",
                method.getParameters().get(1).getType());
    }

    public void testCannotReadParameterNamesSoMakesThemUpInstead() {
        FactoryMethod method = readMethod("org.ParamterizedMatchers", paramterizedMatchersSrc, "withParam");
        List<FactoryMethod.Parameter> params = method.getParameters();

        assertEquals("param1", params.get(0).getName());
        assertEquals("param2", params.get(1).getName());
        assertEquals("param3", params.get(2).getName());
    }

    public void testReadsExceptions() {
        final String input = "" +
                "package org;\n" +
                "public static class ExceptionalMatchers {\n" +
                "  @org.hamcrest.Factory\n" +
                "  public static org.hamcrest.Matcher<String> withExceptions() throws Error, java.io.IOException, RuntimeException { return null; }\n" +
                "}\n";
        
        FactoryMethod method = readMethod("org.ExceptionalMatchers", input, "withExceptions");
        
        List<String> exceptions = method.getExceptions();
        assertEquals(3, exceptions.size());

        assertEquals("java.lang.Error", exceptions.get(0));
        assertEquals("java.io.IOException", exceptions.get(1));
        assertEquals("java.lang.RuntimeException", exceptions.get(2));
    }

    public void testReadsGenericTypeParameters() {
        final String input = "" +
                "package org;\n" +
                "public static class G {\n" +
                "  @org.hamcrest.Factory\n" +
                "  public static <T, V extends java.util.List<String> & Comparable<String>> org.hamcrest.Matcher<java.util.Map<T, V[]>> x(java.util.Set<T> t, V v) { return null; }\n" +
                "}\n";
        
        FactoryMethod method = readMethod("org.G", input, "x");
        
        assertEquals("T", method.getGenericTypeParameters().get(0));
        assertEquals("V extends java.util.List<java.lang.String> & java.lang.Comparable<java.lang.String>",
                method.getGenericTypeParameters().get(1));
        assertEquals("java.util.Map<T,V[]>", method.getGenerifiedType());
        assertEquals("java.util.Set<T>", method.getParameters().get(0).getType());
        assertEquals("V", method.getParameters().get(1).getType());
    }

    public void testCatchesSubclasses() {
        final String input = "" +
                "package org;\n" +
                "public static class SubclassOfMatcher {\n" +
                "  @org.hamcrest.Factory\n" +
                "  public static org.hamcrest.BaseMatcher<?> subclassMethod() { return null; } \n" +
                "}\n";
        
        assertNotNull(readMethod("org.SubclassOfMatcher", input, "subclassMethod"));
    }

    private static FactoryMethod readMethod(String className, String soruceCode, String methodName) {
        for (FactoryMethod method : qDoxReaderFor(className, soruceCode)) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private static QDoxFactoryReaderBase qDoxReaderFor(String className, String sourceCode) {
        final QDox qdox = new QDox();
        qdox.addSource(new StringReader(sourceCode));
        return new QDoxFactoryReaderBase(qdox, className);
    }
}