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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;

import gps.preprocessing.implementer.helper.ProblemWrapping;

/**
 * Represents an implementer that processes a specific annotation and implements
 * all methods / fields that depend on this annotation.
 * 
 * @author haker@uni-bremen.de
 *
 */
public abstract class AbstractAnnotationImplementer
        extends AbstractImplementer {

    /**
     * The ProcessingEnvironment
     */
    private ProcessingEnvironment env;

    /**
     * Class of the annotation for this implementer
     */
    private Class<?> annotation;

    /**
     * All elements that are annotated with the {@link #annotation}
     */
    private Set<Element> usedElements = new HashSet<>();

    /**
     * Construct the AbstractMethodImplementer.
     * 
     * @param pAnnotation
     *            The annotation this Implementer processes.
     */
    public AbstractAnnotationImplementer(Class<?> pAnnotation) {
        annotation = pAnnotation;
    }

    /**
     * Get all Elements that are annotated with this annotation.
     * 
     * @return a set of annotated elements.
     */
    final protected Set<Element> getUsedElements() {
        return usedElements;
    }

    /**
     * Gets the ProcessingEnvironment
     * 
     * @return the ProcessingEnvironment
     */
    final protected ProcessingEnvironment getProcessingEnv() {
        return env;
    }

    /**
     * Sets the context for this implementer. Calculates all elements that are
     * annotated with this element in the given class.
     * 
     * @param pProblemClass
     *            The problem class.
     * @param pUsedAnnotations
     *            The annotations that are used in the problem class.
     * @param pUsedElements
     *            The elements that are annotated in the problem class.
     * @param pRoundEnv
     *            The RoundEnvironment
     * @param pEnv
     *            The ProcessingEnvironment
     */
    @Override
    public void setContext(final Element pProblemClass,
            final Set<? extends TypeElement> pUsedAnnotations,
            final Set<Element> pUsedElements, final RoundEnvironment pRoundEnv,
            final ProcessingEnvironment pEnv) {
        super.setContext(pProblemClass, pUsedAnnotations, pUsedElements,
                pRoundEnv, pEnv);

        // set ProcessingEnvironment variable
        env = pEnv;

        // get annotation as TypeElement. Basically all annotations in
        // usedAnnotations should fulfill the filter.
        final Optional<? extends TypeElement> annoAsTypeElement = pUsedAnnotations
                .stream().filter((a) -> {
                    return a.toString().equals(annotation.getCanonicalName());
                }).findAny();

        // assign the usedElements not directly since the pUsedElements
        // parameter contains all
        // elements that are used in the class but not by the specific
        // annotation in this class.
        if (annoAsTypeElement.isPresent()) {
            usedElements = pUsedElements.stream().filter(m -> {
                return pRoundEnv
                        .getElementsAnnotatedWith(annoAsTypeElement.get())
                        .contains(m);
            }).collect(Collectors.toSet());
        } else {
            usedElements = new HashSet<>(); // we don't have any used Elements
        }

        // check whether all elements are public and throw an Exception if
        // necessary
        usedElements.forEach(e -> {
            if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                env.getMessager()
                        .printMessage(Diagnostic.Kind.ERROR,
                                "@" + annotation.getSimpleName()
                                        + " annotated Method must be public.",
                                e);
            }
        });
    }

    /**
     * 
     * get the class of the annotation for this implementer that has been passed
     * to the constructor {@link #AbstractMethodImplementer(Class)}.
     * 
     * @return the class
     */
    final protected Class<?> getAnnotation() {
        return annotation;
    }

    /**
     * Checks whether the given annotation is present in the current class.
     * 
     * @return {@code true} if there is at least 1 annotated element.
     *         {@code false} otherwise.
     */
    final protected boolean isAnnotationPresent() {
        return !usedElements.isEmpty();
    }

    /**
     * Get an executable element from an element or throws an error message if
     * the Element is not executable.
     * 
     * @param pAnnotatedElement
     *            the element
     * @return the ExecutableElement.
     * 
     */
    final protected ExecutableElement asExecutable(Element pAnnotatedElement) {
        if (!(pAnnotatedElement instanceof ExecutableElement)) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "@" + annotation.getSimpleName()
                            + " annotated Method must be an executable method.",
                    pAnnotatedElement);
        }
        return (ExecutableElement) pAnnotatedElement;
    }

    /**
     * Get all executable elements that are annotated with the current
     * annotation.
     * 
     * @return the ExecutableElement set. The set can be empty but not null.
     * 
     */
    final protected Set<ExecutableElement> asExecutable() {
        return usedElements.stream().filter(e -> {
            return e instanceof ExecutableElement;
        }).map(e -> {
            return (ExecutableElement) e;
        }).collect(Collectors.toSet());
    }

    /**
     * Implements a method that throws an {@link java.lang.RuntimeException}
     * width the reason that the current annotation has not been used.
     * 
     * @param pMethodSignature
     *            the signature of the method e.g.
     *            "public void example(Object arg0)"
     * @return the java source code for this method
     */
    final protected String implementNonAnnotatedMethod(
            String pMethodSignature) {
        return super.implementNonAnnotatedMethod(pMethodSignature, "No @"
                + annotation.getSimpleName() + " Method has been annotated.");
    }

    /**
     * Implements a getter method for the returned object of the annotation.
     * Only one Element with the current annotation is allowed. If more exist an
     * Exception is thrown. The annotated object should be a method or a field.
     * 
     * @param methodPrototype
     *            The prototype for the method. E.g.
     *            {@code "public Comparable<?> heuristic()"}
     * @param string
     * @return The String that implements the described method.
     */
    final protected String implementProblemGetterMethod(
            String methodPrototype) {
        return implementProblemGetterMethod(methodPrototype, null);
    }

    /**
     * Implements a getter method for the returned object of the annotation.
     * Only one Element with the current annotation is allowed. If more exist an
     * Exception is thrown. The annotated object should be a method or a field.
     * 
     * @param methodPrototype
     *            The prototype for the method. E.g.
     *            {@code "public Comparable<?> heuristic()"}
     * @param wrapperClass
     *            An optional wrapper class. This class must provide a
     *            constructor with an argument for the type of object the
     *            problem class provides. Can be {@code null} if a Object Type
     *            is sufficient and therefore no wrapping is necessary. E.g.:
     *            {@code "gps.games.wrapper.Player"}
     * @return The String that implements the described method.
     */
    final protected String implementProblemGetterMethod(String methodPrototype,
            String wrapperClass) {
        StringBuilder sb = new StringBuilder();

        // this handles methods and fields
        String invoke = ProblemWrapping.PROBLEM_ATTRIBUTE + "."
                + implementAccess(getSingleAnnotatedElement());
        if (wrapperClass != null) {
            invoke = "new " + wrapperClass + "(" + invoke + ")";
        }

        sb.append(implementGetterMethod(methodPrototype, invoke));

        return sb.toString();
    }

    /**
     * Get the only annotated element. Throws an error message if annotation is
     * present multiple times or if not present at all.
     * 
     * @return The only element with this annotation.
     */
    final protected Element getSingleAnnotatedElement() {
        if (!isAnnotationPresent()) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "No element has been annotated with the @"
                            + getAnnotation().getSimpleName() + " Annotation.");
        }

        // we cannot handle more than one element with the annotation
        if (getUsedElements().size() > 1) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "There must be no more than one @"
                            + getAnnotation().getSimpleName() + " Annotation.");
        }

        // get the only element in the set
        final Element annotatedElement = (Element) getUsedElements()
                .toArray()[0];

        return annotatedElement;
    }

    /**
     * Implements a simple invoker method that invokes a method. An optional
     * return value is returned. The method that is invoked is passed an
     * argument.
     * 
     * @param methodPrototype
     *            The prototype of the method to implement. E.g.
     *            {@code "public boolean setVal(Object val)"}
     * @param argName
     *            The argument to pass to the method to invoke. E.g. "val".
     * @return The String that implements the described method.
     */
    final protected String implementProblemMethodInvokerMethod(
            String methodPrototype, ExecutableElement method, String argName) {
        StringBuilder sb = new StringBuilder();

        // get the parameter list for the method
        final List<? extends VariableElement> parameters = method
                .getParameters();

        if (parameters.size() != 1) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "There must be exactly one parameter for the @"
                            + getAnnotation().getSimpleName() + " method.",
                    method);
        }

        final String ret = method.getReturnType().getKind()
                .equals(TypeKind.VOID) || methodPrototype.contains("void ") ? ""
                        : "return ";

        sb.append("    " + methodPrototype + " {\n        " + ret
                + ProblemWrapping.PROBLEM_ATTRIBUTE + ".");
        sb.append(method.getSimpleName());
        sb.append("((");
        sb.append(method.getParameters().get(0).asType().toString());
        sb.append(") " + argName + ");\n    }\n\n");
        return sb.toString();
    }

    /**
     * Implements a private method that returns an array of
     * {@link java.lang.Runnable}. The array contains all methods that take no
     * arguments and do not have a return value (void methods). The newly
     * generated method will have the following signature:
     * {@code private java.lang.Runnable[] methodName()}
     * 
     * @param methodName
     *            The name of the method to implement
     * @return The String which implements the method.
     */
    final protected String implementMethodsToRunableConverter(
            String methodName) {
        StringBuilder sb = new StringBuilder();
        sb.append("    private java.lang.Runnable[] " + methodName
                + "() { \n        return new java.lang.Runnable[] { \n");
        asExecutable().stream().filter(p -> {
            return getReturnTypeOfElement(p).getKind().equals(TypeKind.VOID)
                    && p.getParameters().isEmpty();
        }).forEach(e -> {
            sb.append(
                    "        new java.lang.Runnable() { public void run() { ");
            sb.append(ProblemWrapping.PROBLEM_ATTRIBUTE + ".");
            sb.append(implementAccess(e));
            sb.append("; } }, \n");
        });
        sb.append("        }; \n    } \n\n");
        return sb.toString();
    }
}
