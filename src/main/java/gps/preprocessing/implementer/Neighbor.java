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
package gps.preprocessing.implementer;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import gps.preprocessing.AbstractAnnotationImplementer;
import gps.preprocessing.implementer.helper.ProblemWrapping;

/**
 * Method Implementer for the {@link gps.annotations.Neighbor} annotation.
 * 
 * @author mburri
 *
 */
public class Neighbor extends AbstractAnnotationImplementer {

    /**
     * Construct the Method Implementer
     */
    public Neighbor() {
        super(gps.annotations.Neighbor.class);
    }

    /**
     * The method that is implemented by this implementer.
     */
    private final static String METHOD_PROTOTYPE = "public java.util.List<Object[]> neighbor(final Object[] params)";

    /**
     * The name of the has-method that this implementer implements.
     */
    private final static String HAS_METHOD_NAME = "hasNeighborFunction";

    @Override
    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();
        if (isAnnotationPresent()) {
            // get the only element in the set as executable element (method)
            final ExecutableElement method = asExecutable(
                    getSingleAnnotatedElement());

            // get the parameter list for the method
            final List<? extends VariableElement> parameters = method
                    .getParameters();

            // check params of neighbor = params of optimize
            if (!paramsEqualToOptimizeParams(parameters)) {
                getProcessingEnv().getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Either you did not specify an @Optimize-annotated method "
                                + "or the parameters of the @Optimize-annotated and the "
                                + "@Neighbor-annotated method do not match.",
                        method);
            }

            final TypeMirror returnType = method.getReturnType();

            // every parameter has to be casted so the  
            // neighbor-method of the problem can be invoked
            StringJoiner sjParams = new StringJoiner(", ");
            for (int i = 0; i < parameters.size(); i++) {
                sjParams.add("(" + parameters.get(i).asType().toString() + ") "
                        + "params[" + i + "]");
            }

            StringBuilder toReturn = new StringBuilder();
            toReturn.append("        java.util.List<Object[]> ret = "
                    + "new java.util.ArrayList<Object[]>();\n        ");

            // only 1 parameter -> returntype == paramtype 
            //					or returntype == List<paramtype> allowed
            if (parameters.size() == 1) {
                VariableElement onlyParam = parameters.get(0);
                if (onlyParam.asType().toString()
                        .equals(returnType.toString())) {
                    // returntype == paramtype
                    toReturn.append("ret.add(new Object[]{ "
                            + ProblemWrapping.PROBLEM_ATTRIBUTE + "."
                            + method.getSimpleName() + "(" + sjParams.toString()
                            + ") });");
                    toReturn.append("\n");
                } else if (returnType.toString().startsWith("java.util.List")) {
                    if (returnType.toString()
                            .substring(returnType.toString().indexOf("<"),
                                    returnType.toString().indexOf(">"))
                            .toLowerCase().contains(parameters.get(0).asType()
                                    .toString().toLowerCase())) {
                        // returntype == List<paramtype>
                        toReturn.append("for (Object o : "
                                + ProblemWrapping.PROBLEM_ATTRIBUTE + "."
                                + method.getSimpleName() + "("
                                + sjParams.toString() + ")) {\n");
                        toReturn.append(
                                "            ret.add(new Object[]{ o });");
                        toReturn.append("\n        }\n");
                    } else {
                        // type of List not equal to paramtype
                        getProcessingEnv().getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                "Generic type of return type java.util.List "
                                        + "must be equal to the type of the parameter "
                                        + "(excluding wrappers)",
                                method);
                    }
                } else {
                    getProcessingEnv().getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "If your @Neighbor-annotated method has exactly "
                                    + "one parameter it is allowed to return "
                                    + "an object of the same type as the parameter or "
                                    + "a List of objects of the same type as the parameter.",
                            method);
                }
            } else {
                // multiple parameters -> only Object[] or List<Object[] allowed
                // returntype == Object[]
                if (returnType.toString().equals("java.lang.Object[]")) {
                    toReturn.append(
                            "ret.add(" + ProblemWrapping.PROBLEM_ATTRIBUTE + "."
                                    + method.getSimpleName() + "("
                                    + sjParams.toString() + "));");
                    toReturn.append("\n");
                } else if (method.getReturnType().toString()
                        .equals("java.util.List<java.lang.Object[]>")) {
                    // returntype == List<Object[]>
                    toReturn.append(
                            "ret.addAll(" + ProblemWrapping.PROBLEM_ATTRIBUTE
                                    + "." + method.getSimpleName() + "("
                                    + sjParams.toString() + "));");
                    toReturn.append("\n");
                } else {
                    getProcessingEnv().getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "If your @Neighbor-annotated method has multiple parameters "
                                    + " it is allowed to return either an Object-Array containing "
                                    + "objects of the same types as the parameters or a List of "
                                    + "Object-Arrays each containing objects of the same types as the parameters.",
                            method);
                }
            }

            sb.append("    " + METHOD_PROTOTYPE + " {\n" + toReturn.toString()
                    + "        return ret;\n    }\n\n");
        } else {
            sb.append(implementNonAnnotatedMethod(METHOD_PROTOTYPE));

        }
        sb.append(
                implementBooleanMethod(HAS_METHOD_NAME, isAnnotationPresent()));

        return sb.toString();
    }

    /**
     * Checks whether the given list of VariableElements is equal to 
     * the parameters of the {@link gps.annotations.Optimize}-annotated 
     * method.
     * 
     * @param parameters
     * 			the list of VariableElements that is to be checked
     * @return	{@code true} if the list is equal,
     * 			{@code false} otherwise
     */
    private boolean paramsEqualToOptimizeParams(
            List<? extends VariableElement> parameters) {
        // get @Optimize-annotated Element
        final Optional<? extends Element> optimizeElement = getProblemClass()
                .getEnclosedElements().stream().filter(e -> {
                    for (AnnotationMirror m : e.getAnnotationMirrors()) {
                        if (m.toString().equals("@gps.annotations.Optimize")) {
                            return true;
                        }
                    }
                    return false;
                }).findFirst();
        if (optimizeElement.isPresent()) {
            if (optimizeElement.get() instanceof ExecutableElement) {
                final ExecutableElement optimizeMethod = (ExecutableElement) optimizeElement
                        .get();
                List<? extends VariableElement> optimizeParameters = optimizeMethod
                        .getParameters();

                // compare parameters
                if (optimizeParameters.size() != parameters.size()) {
                    return false;
                }
                for (int i = 0; i < parameters.size(); i++) {
                    if (!getProcessingEnv().getTypeUtils().isSameType(
                            parameters.get(i).asType(),
                            optimizeParameters.get(i).asType())) {
                        return false;
                    }
                }
                return true;
            }
        }
        // annotated element not a method 
        // or no annotated element present
        return false;
    }

}
