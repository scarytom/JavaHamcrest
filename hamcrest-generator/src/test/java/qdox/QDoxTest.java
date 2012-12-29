package qdox;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameterizedType;

public class QDoxTest extends junit.framework.TestCase {

    final JavaProjectBuilder builder = new JavaProjectBuilder();
    
    public void
    testReadsGenericsInGenericType() {
        final String sourceCode = "" +
                "package foo;\n" +
                "public static class DummyOne {\n" +
                "  public static java.util.list<java.util.Map<? extends java.util.Set<Long>, String>> crazyType() { return null; }\n" +
                "}\n";
        
        builder.addSource(new java.io.StringReader(sourceCode));
        JavaClass qDoxClass = builder.getClassByName("foo.DummyOne");
        JavaMethod qDoxMethod = qDoxClass.getMethods().get(0);
        
        String result = ((JavaParameterizedType)qDoxMethod.getReturnType()).getActualTypeArguments().get(0).getGenericFullyQualifiedName();
        assertEquals("java.util.Map<? extends java.util.Set<java.lang.Long>,java.lang.String>", result);
    }
    
    public void
    testReadsGenerifiedParameterTypes() {
        final String sourceCode = "" +
                "package foo;\n" +
                "public static class DummyOne {\n" +
                "  public static String withGenerifiedParam(java.util.Collection<? extends Comparable<String>> things) { return null; }\n" +
                "}\n";
        
        builder.addSource(new java.io.StringReader(sourceCode));
        JavaClass qDoxClass = builder.getClassByName("foo.DummyOne");
        JavaMethod qDoxMethod = qDoxClass.getMethods().get(0);
        
        String result = qDoxMethod.getParameterTypes(true).get(0).getGenericFullyQualifiedName();
        assertEquals("java.util.Collection<? extends java.lang.Comparable<java.lang.String>>", result);
    }
    
    public void
    testReadsGenericTypeParameters() {
        final String sourceCode = "" +
                "package foo;\n" +
                "public static class DummyOne {\n" +
                "  public static <T extends Number & Iterable<Integer>> T genericTypeParam(T x) { return null; }\n" +
                "}\n";
        
        builder.addSource(new java.io.StringReader(sourceCode));
        JavaClass qDoxClass = builder.getClassByName("foo.DummyOne");
        JavaMethod qDoxMethod = qDoxClass.getMethods().get(0);
        
        String result = qDoxMethod.getTypeParameters().get(0).getGenericFullyQualifiedName();
        assertEquals("<T extends java.lang.Number & java.lang.Iterable<java.lang.Integer>>", result);
    }
}
