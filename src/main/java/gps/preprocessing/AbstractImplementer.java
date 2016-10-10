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

import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import gps.preprocessing.implementer.helper.CollectionHelper;
import gps.preprocessing.implementer.helper.ProblemWrapping;

/**
 * Represents an implementer that implements methods or attributes to the
 * wrapper class
 * 
 * @author haker@uni-bremen.de
 *
 */
public abstract class AbstractImplementer {

    /**
     * The problem class. Gets set in the
     * {@link #setContext(Element, Set, Set, RoundEnvironment)} method.
     */
    private Element problemClass;

    /**
     * Get the problem class that this implementer implements methods for.
     * 
     * @return The problem class as {@link javax.lang.model.element.Element}
     */
    public Element getProblemClass() {
        return problemClass;
    }

    /**
     * Construct the AbstractMethodImplementer.
     * 
     * @param pAnnotation
     *            The annotation this Implementer processes.
     */
    public AbstractImplementer() {
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
    public void setContext(final Element pProblemClass,
            final Set<? extends TypeElement> pUsedAnnotations,
            final Set<Element> pUsedElements, final RoundEnvironment pRoundEnv,
            final ProcessingEnvironment pEnv) {
        problemClass = pProblemClass;
    }

    /**
     * Retrieve the java source code of the methods that this implementer
     * implements.
     * 
     * @return java source code as a string.
     */
    public abstract String toSourceCode();

    /**
     * Implements a method that returns a constant boolean variable. The method
     * accepts no parameters.
     * 
     * @param pMethodName
     *            The name of the method eg. "hasSuccessorMethod"
     * @param retVal
     *            the value this method should return
     * @return the java source code for this method
     */
    final protected static String implementBooleanMethod(String pMethodName,
            boolean retVal) {
        return "    public boolean " + pMethodName + "() {\n        return "
                + (retVal ? "true" : "false") + ";\n    }\n\n";
    }

    /**
     * Implements a method that throws an {@link java.lang.RuntimeException}.
     * 
     * @param pMethodSignature
     *            the signature of the method eg.
     *            "public void example(Object arg0)"
     * @param pMessage
     *            The message that is used to instantiate the exception.
     * @return the java source code for this method
     */
    final protected String implementNonAnnotatedMethod(String pMethodSignature,
            String pMessage) {
        return "    " + pMethodSignature
                + " {\n        throw new RuntimeException(\"" + pMessage
                + "\");\n    }\n\n";
    }

    /**
     * Gets the return type for an element. The return type of a method is
     * simply its return type. The return type of a field is the type of the
     * field.
     * 
     * @param element
     *            The element whose return type is to be retrieved.
     * @return The return type.
     */
    final protected TypeMirror getReturnTypeOfElement(Element element) {
        TypeMirror type;
        if (element instanceof ExecutableElement) {
            ExecutableElement e = (ExecutableElement) element;
            type = e.getReturnType();
        } else {
            type = element.asType();
        }
        return type;
    }

    /**
     * Implements a getter method that retrieves a list. Allows multiple
     * annotated elements. Element types can be a single element, collection or
     * array. Also tries to handle primitive arrays. Furthermore a wrapper class
     * can be specified.
     * 
     * @param methodPrototype
     *            The prototype of the method. Should return a
     *            {@link java.util.List}. Eg.
     *            {@code "public java.util.List<gps.games.wrapper.Action> getElements()"}
     * @param it
     *            An iterable for the elements that are considered to be in the
     *            list. Use {@link #getUsedElements()} to process all annotated
     *            elements.
     * @param wrapperClass
     *            An optional wrapper class. This class must provide a
     *            constructor with an argument for the type of object the
     *            problem class provides. Can be {@code null} if a Object Type
     *            is sufficient and therefore no wrapping is necessary. Eg.:
     *            {@code "gps.games.wrapper.Player"}
     * @return The String that implements the described method.
     */
    final protected String implementCollectionProblemGetterMethod(
            String methodPrototype, Iterable<Element> it, String wrapperClass) {
        StringBuilder sb = new StringBuilder();
        sb.append("    " + methodPrototype);

        final String wrapper;

        sb.append(" {\n        final java.util.ArrayList<");

        if (wrapperClass == null || wrapperClass.isEmpty()) {
            wrapper = "";
            sb.append("?"); // better: the actual type
        } else {
            wrapper = ".map(m->{return new " + wrapperClass + "(m);})";
            sb.append(wrapperClass);
        }

        sb.append("> list = new java.util.ArrayList<>();\n");
        for (Element method : it) {

            final String invoke = ProblemWrapping.PROBLEM_ATTRIBUTE + "."
                    + implementAccess(method); // this handles methods and fields

            // array invoke (can do some primitive arrays too)
            final String arrayInvoke = "java.util.Arrays.asList( " + invoke
                    + " )";

            sb.append("        list.addAll( ");

            if (getReturnTypeOfElement(method).getKind()
                    .equals(TypeKind.ARRAY)) { // allow arrays
                sb.append(arrayInvoke);
            } else { // java.util.Collection
                sb.append(CollectionHelper.TO_COLLECTION + "(" + invoke + ")");
            }

            sb.append(".stream()" + wrapper
                    + ".collect(java.util.stream.Collectors.toList()));\n");
        }
        sb.append("        return list;\n    }\n\n");
        return sb.toString();
    }

    /**
     * Implements a simple getter method. Only one Element with the current
     * annotation is allowed. If more exist an Exception is thrown.
     * 
     * @param methodPrototype
     *            The prototype for the method. Eg.
     *            {@code "public Comparable<?> heuristic()"}
     * @param string
     * @return The String that implements the described method.
     */
    final protected String implementGetterMethod(String methodPrototype) {
        return implementGetterMethod(methodPrototype, null);
    }

    /**
     * Implements a getter method.
     * 
     * @param methodPrototype
     *            The prototype for the method. Eg.
     *            {@code "public Comparable<?> heuristic()"}
     * @param pReturnCode
     *            Code that is executed after the return statement.
     * @return The String that implements the described method.
     */
    final protected String implementGetterMethod(String methodPrototype,
            String pReturnCode) {
        StringBuilder sb = new StringBuilder();

        sb.append("    " + methodPrototype + " {\n");
        sb.append("        return ");
        sb.append(pReturnCode); // this handles methods and fields
        sb.append(";\n    }\n\n");

        return sb.toString();
    }

    /**
     * Implements a method or a field access. If a method is passed as argument
     * then the method must have 0 arguments.
     * 
     * @param element
     *            The element to retrieve
     * @return A String that implements the call to the specified element.
     */
    final protected String implementAccess(Element element) {
        String name = element.getSimpleName().toString();
        switch (element.getKind()) {
        case METHOD:
            return name + "()";
        case FIELD:
            return name;
        default:
            return element.toString();
        }
    }
}
