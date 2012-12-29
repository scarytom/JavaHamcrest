package org.hamcrest.generator;

import java.io.File;
import java.io.Reader;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * Wraps QDox library. This is because to ease distribution, QDox is bundled into
 * hamcrest-generator.jar and has its package renamed to ensure there is no conflict
 * (using Jar Jar Links). This wrapper class removes all traces of QDox from Hamcrest's
 * public API, so there aren't any compatibility problems.
 *
 * @author Joe Walnes
 */
public class QDox {

    private final JavaProjectBuilder JavaProjectBuilder = new JavaProjectBuilder();

    public void addSourceTree(File sourceDir) {
        JavaProjectBuilder.addSourceTree(sourceDir);
    }

    public void addSource(Reader reader) {
        JavaProjectBuilder.addSource(reader);
    }

    JavaClass getClassByName(String className) {
        return JavaProjectBuilder.getClassByName(className);
    }
}
