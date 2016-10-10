/*
 * Copyright 2016  Generic Problem Solver Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gps.preprocessing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import gps.IWrappedProblem;
import gps.util.reflections.ReflectionsHelper;
import javassist.Modifier;

/**
 * The Annotation processor. Builds wrapper classes for all available problem
 * classes.
 * 
 * @author haker@uni-bremen.de
 *
 */

@SupportedAnnotationTypes("gps.annotations.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor {
    /**
     * The processing environment
     */
    private ProcessingEnvironment env;

    /**
     * All method implementers. Gets initialized by
     * {@link #init(ProcessingEnvironment)}.
     */
    private final List<Class<? extends AbstractImplementer>> methodImplementers = new ArrayList<>();

    @Override
    public void init(final ProcessingEnvironment arg0) {
        env = arg0;

        // initialize method implementers list
        methodImplementers.clear();
        methodImplementers
                .addAll(ReflectionsHelper
                        .getSubTypesOfCached("gps.preprocessing.*",
                                AbstractImplementer.class)
                        .stream().filter(p -> {
                            return (!Modifier.isAbstract(p.getModifiers())
                                    && !p.isInterface()
                                    && Modifier.isPublic(p.getModifiers()));
                        }).collect(Collectors.toSet()));
    }

    @Override
    public boolean process(final Set<? extends TypeElement> gpsAnnotations,
            final RoundEnvironment pRoundEnv) {

        // Set of all problem classes (classes that contain gps.annotation.*
        // annotations)
        final Set<Element> problemClasses = new HashSet<>();

        // Set of all annotations (annotations from gps.annotation.*)
        final Set<? extends TypeElement> problemAnnotations = gpsAnnotations;

        // Set of all elements (in all classes) that are annotated with
        // annotations from gps.annotation.*
        final Set<Element> problemElements = new HashSet<>();

        // Fill problemElements and problemElements
        for (final TypeElement typeElement : gpsAnnotations) {
            final Set<? extends Element> elements = pRoundEnv
                    .getElementsAnnotatedWith(typeElement);
            for (final Element e : elements) {
                problemClasses.add(e.getEnclosingElement());
                problemElements.add(e);
            }
        }

        // Create a wrapper class for each problem class
        problemClasses.forEach(problemClass -> {
            // Calculate all annotations that are used in the problem class
            final Set<? extends TypeElement> usedAnnotations = problemAnnotations
                    .stream().filter(e -> {
                        return pRoundEnv.getElementsAnnotatedWith(e).stream()
                                .map(m -> {
                                    return m.getEnclosingElement();
                                }).anyMatch(s -> {
                                    return s.equals(problemClass);
                                });
                    }).collect(Collectors.toSet());

            // Calculate all Elements that are annotated in the problem class
            final Set<Element> usedElemets = problemElements.stream()
                    .filter(m -> {
                        return m.getEnclosingElement().equals(problemClass);
                    }).collect(Collectors.toSet());

            // Generate a wrapper class for the given problem class
            try {
                generateWrapper(problemClass, usedAnnotations, usedElemets,
                        pRoundEnv);
            } catch (final IOException e) {
                env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Failed to generate wrapper class for "
                                + problemClass.toString());
                env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Caused by: " + e.getMessage());
            }

        });
        return false;
    }

    /**
     * Creates a class name for the wrapper class of the given problem class.
     * 
     * @param problemClass
     *            The problem class.
     * @return The name of the wrapper class.
     */
    public static String getSafeClassName(Element problemClass) {
        return "C" + problemClass.toString().replaceAll("\\.", "P") + "Wrapped";
    }

    /**
     * Generate java source for wrapper class.
     * 
     * @param problemClass
     *            the problem class to wrap
     * @param usedAnnotations
     *            the annotations that are used in the class
     * @param usedElemets
     *            The methods, fields and other elements that are annotated in
     *            the class.
     * @param pRoundEnv
     *            The RoundEnvironment
     * @throws IOException
     *             if generation failed.
     */
    private void generateWrapper(Element problemClass,
            Set<? extends TypeElement> usedAnnotations,
            Set<Element> usedElemets, RoundEnvironment pRoundEnv)
            throws IOException {
        JavaFileObject jfo;
        PackageElement packageElem = env.getElementUtils()
                .getPackageOf(problemClass);

        String jfoFileName = "";
        if (!packageElem.getQualifiedName().toString().isEmpty()) {
            jfoFileName = packageElem.getQualifiedName() + ".";
        }

        jfoFileName += getSafeClassName(problemClass);
        jfo = env.getFiler().createSourceFile(jfoFileName, packageElem);

        final BufferedWriter bw = new BufferedWriter(jfo.openWriter());
        try {
            bw.write(generateSource(problemClass, usedAnnotations, usedElemets,
                    pRoundEnv));
        } finally {
            bw.close();
        }
    }

    /**
     * Generates source code string for a wrapper class to a specific problem
     * class
     * 
     * @param problemClass
     *            the problem class to wrap
     * @param usedAnnotations
     *            the annotations that are used in the class
     * @param usedElements
     *            The methods, fields and other elements that are annotated in
     *            the class.
     * @param pRoundEnv
     *            The RoundEnvironment
     * @return The java source code for the wrapper class as a String.
     */
    private String generateSource(final Element problemClass,
            Set<? extends TypeElement> usedAnnotations,
            Set<Element> usedElements, RoundEnvironment pRoundEnv) {
        final StringBuilder sb = new StringBuilder();

        PackageElement packageElement = env.getElementUtils()
                .getPackageOf(problemClass);

        if (!packageElement.getQualifiedName().toString().isEmpty()) {
            sb.append("package ");
            sb.append(env.getElementUtils().getPackageOf(problemClass)
                    .getQualifiedName());
            sb.append(";\n\n");
        }

        // We actually want exceptions if the user failed to make the types
        // match
        sb.append("@SuppressWarnings(\"all\")\n");

        // public class xxx implements IWrappedProblem {
        sb.append("public class ");
        sb.append(getSafeClassName(problemClass));
        sb.append(" implements ");
        sb.append(IWrappedProblem.class.getCanonicalName());
        sb.append("<" + problemClass + "> {\n\n");

        // Invoke all implementers and add their source code to the file
        methodImplementers.forEach(o -> {
            try {
                AbstractImplementer implementer = o.newInstance();
                implementer.setContext(problemClass, usedAnnotations,
                        usedElements, pRoundEnv, env);
                sb.append(implementer.toSourceCode());
            } catch (IllegalAccessException | InstantiationException
                    | ExceptionInInitializerError | SecurityException e) {
                env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Bad implementation of Implementer. Cannot instantiate class.");
            }
        });

        // End Class
        sb.append("}\n");

        return sb.toString();

    }

}
