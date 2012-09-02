package qdox;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

public class QDoxTest extends junit.framework.TestCase {

    final JavaDocBuilder builder = new JavaDocBuilder();
    
    public void
    testReadsGenericsInGenericType() {
        final String sourceCode = "" +
                "package foo;\n" +
                "public static class DummyOne {\n" +
                "  public static java.util.list<java.util.Map<? extends java.util.Set<Long>, String>> crazyType() { return null; }\n" +
                "}\n";
        
        builder.addSource(new java.io.StringReader(sourceCode));
        JavaClass qDoxClass = builder.getClassByName("foo.DummyOne");
        JavaMethod qDoxMethod = qDoxClass.getMethods()[0];
        
        String result = qDoxMethod.getGenericReturnType().getActualTypeArguments()[0].toGenericString();
        assertEquals("java.util.Map<? extends java.util.Set<java.lang.Long>, java.lang.String>", result);
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
        JavaMethod qDoxMethod = qDoxClass.getMethods()[0];
        
        String result = qDoxMethod.getParameterTypes(true)[0].getGenericValue();
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
        JavaMethod qDoxMethod = qDoxClass.getMethods()[0];
        
        String result = qDoxMethod.getTypeParameters()[0].toGenericString();
        assertEquals("<T extends java.lang.Number & java.lang.Iterable<java.lang.Integer>>", result);
    }
}
