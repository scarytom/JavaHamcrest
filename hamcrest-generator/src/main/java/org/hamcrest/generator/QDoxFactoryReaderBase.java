package org.hamcrest.generator;

import java.util.Iterator;

import com.thoughtworks.qdox.model.Annotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.Type;
import com.thoughtworks.qdox.model.TypeVariable;

/**
 * Reads a list of Hamcrest factory methods from a class, using QDox source reflection.
 * <h3>Usage</h3>
 * <pre>
 * for (FactoryMethod method : new ReflectiveFactoryReader(MyMatchers.class)) {
 *   ...
 * }
 * </pre>
 * <p>All methods matching signature '@Factory public static Matcher<blah> blah(blah)' will be
 * treated as factory methods. To change this behaviour, override {@link #isFactoryMethod(JavaMethod)}.
 * <p>Caveat: Reflection is hassle-free, but unfortunately cannot expose method parameter names or JavaDoc
 * comments, making the sugar slightly more obscure.
 *
 * @author Tom Denley
 * @see FactoryMethod
 */
public class QDoxFactoryReaderBase implements Iterable<FactoryMethod> {

    private final JavaClass classSource;

    public QDoxFactoryReaderBase(QDox qdox, String className) {
        this.classSource = qdox.getClassByName(className);
    }

    @Override
    public Iterator<FactoryMethod> iterator() {
        return new Iterator<FactoryMethod>() {

            private int currentMethod = -1;
            private JavaMethod[] allMethods = classSource.getMethods();

            @Override
            public boolean hasNext() {
                while (true) {
                    currentMethod++;
                    if (currentMethod >= allMethods.length) {
                        return false;
                    } else if (isFactoryMethod(allMethods[currentMethod])) {
                        return true;
                    } // else carry on looping and try the next one.
                }
            }

            @Override
            public FactoryMethod next() {
                if (outsideArrayBounds()) {
                  throw new IllegalStateException("next() called without hasNext() check.");
                }
                return buildFactoryMethod(classSource, allMethods[currentMethod]);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            private boolean outsideArrayBounds() {
              return currentMethod < 0 || allMethods.length <= currentMethod;
            }
        };
    }

    /**
     * Determine whether a particular method is classified as a matcher factory method.
     * <p/>
     * <p>The rules for determining this are:
     * 1. The method must be public static.
     * 2. It must have a return type of org.hamcrest.Matcher (or something that extends this).
     * 3. It must be marked with the org.hamcrest.Factory annotation.
     * <p/>
     * <p>To use another set of rules, override this method.
     */
    protected boolean isFactoryMethod(JavaMethod method) {
        return method.isStatic()
                && method.isPublic()
                && hasFactoryAnnotation(method)
                && !Void.TYPE.getName().equals(method.getReturnType().getFullyQualifiedName());
    }

    private boolean hasFactoryAnnotation(JavaMethod method) {
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if ("org.hamcrest.Factory".equals(annotation.getType().getFullyQualifiedName())) {
                return true;
            }
        }
        return false;
    }
    
    private static FactoryMethod buildFactoryMethod(JavaClass clazz, JavaMethod method) {
        FactoryMethod result = new FactoryMethod(
                clazz.getFullyQualifiedName(),
                method.getName(), 
                method.getReturnType().getFullyQualifiedName());

        for (TypeVariable typeVariable : method.getTypeParameters()) {
            boolean hasBound = false;
            StringBuilder s = new StringBuilder(typeVariable.getName());
            final Type[] actualTypeArguments = typeVariable.getActualTypeArguments();
            if (actualTypeArguments != null) {
                for (Type bound : actualTypeArguments) {
                    if (!"java.lang.Object".equals(bound.getGenericValue())) {
                        if (hasBound) {
                            s.append(" & ");
                        } else {
                            s.append(" extends ");
                            hasBound = true;
                        }
                        s.append(bound.getGenericValue());
                    }
                }
            }
            result.addGenericTypeParameter(s.toString());
        }
        
        Type returnType = method.getGenericReturnType();
        if (returnType.getActualTypeArguments() != null) {
            Type generifiedType = returnType.getActualTypeArguments()[0];
            result.setGenerifiedType(generifiedType.toGenericString());
        }

        int paramNumber = 0;
        for (Type paramType : method.getParameterTypes(true)) {
            String type = paramType.getGenericValue();
            // Special case for var args methods.... String[] -> String...
            if (method.isVarArgs() && paramNumber == method.getParameterTypes().length - 1) {
                type = type.concat("...");
            }
            result.addParameter(type, "param" + (++paramNumber));
        }

        for (Type exception : method.getExceptions()) {
            result.addException(exception.getFullyQualifiedName());
        }

        return result;
    }
}