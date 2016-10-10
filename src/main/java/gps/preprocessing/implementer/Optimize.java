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
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import gps.preprocessing.AbstractAnnotationImplementer;
import gps.preprocessing.implementer.helper.ProblemWrapping;

/**
 * Method Implementer for the {@link gps.annotations.Optimize} annotation.
 * 
 * @author mburri
 *
 */
public class Optimize extends AbstractAnnotationImplementer {

    /**
     * Construct the Method Implementer
     */
    public Optimize() {
        super(gps.annotations.Optimize.class);
    }

    /**
     * The method that is implemented by this implementer.
     * This is the method that is to be optimized.
     */
    private final static String METHOD_PROTOTYPE_OBJ = "public double objectiveFunction(final Object[] params)";

    /**
     * The name of the has-method that this implementer implements.
     */
    private final static String HAS_METHOD_NAME = "hasObjectiveFunction";

    /**
     * The other method that is implemented by this implementer.
     * This method provides the solvers with an initial "solution".
     */
    private final static String METHOD_PROTOTYPE_DEFAULT_PARAMS = "public Object[] getDefaultParams()";

    /**
     * The other other method that is implemented by this implementer.
     * This method provides the function that is used for the 
     * {@link gps.optimization.algorithm.incrementalSat.IncrementalSat}. 
     */
    private final static String METHOD_PROTOTYPE_CBGT = "public boolean canBeGreaterThan()";

    /**
     * The method that sets the maximize field of the wrapper. The maximize field is 
     * needed to handle both maximize and minimize cases with iterative SAT, since 
     * we only have one constraint method, namely 
     * {@link gps.optimization.wrapper.Optimizable#canBeGreaterThanConstraint()}.
     */
    private final static String METHOD_PROTOTYPE_SET_MAXIMIZE = "public void setMaximize(final byte pMax)";

    /**
     * The method that sets the threshold field. The threshold field is needed for the 
     * {@link gps.optimization.algorithm.incrementalSat.IncrementalSat}.
     */
    private final static String METHOD_PROTOTYPE_SET_THRESHOLD = "public void setThresholdForObjectiveFunction(final double pThresh)";

    @Override
    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();
        if (isAnnotationPresent()) {
            // get the only element in the set as executable element (method)
            final ExecutableElement method = asExecutable(
                    getSingleAnnotatedElement());

            // supported return types are int,double,short,long,float,byte
            // and their wrappers
            if (!returnTypeValid(method.getReturnType())) {
                getProcessingEnv().getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Methods that are annotated with the @"
                                + getAnnotation().getSimpleName()
                                + " annotation are only allowed to return "
                                + "int, double, float, byte, long or short "
                                + "and their wrappers.",
                        method);
            }

            // objectiveFunction-method
            // create method that calls the method of the wrapped class
            // with the casted parameters
            //
            // get the parameter list for the method
            final List<? extends VariableElement> parameters = method
                    .getParameters();
            // every parameter has to be casted
            StringJoiner sjParams = new StringJoiner(", ");
            for (int i = 0; i < parameters.size(); i++) {
                sjParams.add("(" + parameters.get(i).asType().toString() + ") "
                        + "params[" + i + "]");
            }

            // return value is casted to double for consistency
            sb.append("    " + METHOD_PROTOTYPE_OBJ + " {\n        "
                    + "return (double) (maximize * "
                    + ProblemWrapping.PROBLEM_ATTRIBUTE + ".");
            sb.append(method.getSimpleName());
            sb.append("(" + sjParams.toString() + "));\n    }\n\n");

            // create attribute that determines if the problem is to be maximized 
            // or minimized (needed for incremental sat)
            sb.append(setMaximizeSourceCode());

            // getDefaultParams-method
            // returns all fields that are to be optimized and therefore annotated with the 
            // @Variable annotation, wrapped in an Object array
            //
            // get all fields annotated with the @Variable annotation in the class
            List<Element> variableElements = getVariableAnnotatedFields();
            // check annotated fields for completeness and validity
            // and return them wrapped in an Object array
            String defaultPars = defaultParamsAttributesString(parameters,
                    variableElements);
            sb.append(defaultParamsSourceCode(defaultPars));

            // add threshold field and setter
            sb.append(thresholdSourceCode(method, defaultPars));

        } else {
            sb.append(implementNonAnnotatedMethod(METHOD_PROTOTYPE_OBJ));
            sb.append(implementNonAnnotatedMethod(
                    METHOD_PROTOTYPE_DEFAULT_PARAMS));
            sb.append(implementNonAnnotatedMethod(METHOD_PROTOTYPE_CBGT));
            sb.append(
                    implementNonAnnotatedMethod(METHOD_PROTOTYPE_SET_MAXIMIZE));
            sb.append(implementNonAnnotatedMethod(
                    METHOD_PROTOTYPE_SET_THRESHOLD));
        }
        sb.append(
                implementBooleanMethod(HAS_METHOD_NAME, isAnnotationPresent()));

        return sb.toString();
    }

    /**
     * Checks whether the given type is a supported return type for 
     * the objective function
     * 
     * @param type
     * 			the type that is to be evaluated
     * @return	{@code true} if the type is supported,
     * 			{@code false} otherwise
     */
    public boolean returnTypeValid(final TypeMirror type) {
        String returnTypeToString = type.toString();
        if ("int".equals(returnTypeToString)
                || "java.lang.Integer".equals(returnTypeToString)
                || "double".equals(returnTypeToString)
                || "java.lang.Double".equals(returnTypeToString)
                || "float".equals(returnTypeToString)
                || "java.lang.Float".equals(returnTypeToString)
                || "short".equals(returnTypeToString)
                || "java.lang.Short".equals(returnTypeToString)
                || "long".equals(returnTypeToString)
                || "java.lang.Short".equals(returnTypeToString)
                || "byte".equals(returnTypeToString)
                || "java.lang.Byte".equals(returnTypeToString)) {
            return true;
        }
        return false;
    }

    /**
     * Returns all fields that are annotated with the {@link gps.annotations.Variable}
     * annotation. Throws an error message, if the annotated element is 
     * not a field, if the field is private or if the field is final.  
     * 
     * @return
     * 		all fields annotated with the {@link gps.annotations.Variable} annotation
     */
    public List<Element> getVariableAnnotatedFields() {
        return getProblemClass().getEnclosedElements().stream().filter(e -> {
            for (AnnotationMirror m : e.getAnnotationMirrors()) {
                if (m.toString().contains("@gps.annotations.Variable")) {
                    if (m instanceof ExecutableElement) {
                        getProcessingEnv().getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                "@Variable annotated element must be a field.",
                                e);
                    }
                    e.getModifiers().forEach(mod -> {
                        // annotated field must not be private or final
                        // so it can be 1. accessed by us
                        // 2. modified by us
                        if (mod.toString().equals("final")
                                || mod.toString().equals("private")) {
                            getProcessingEnv().getMessager().printMessage(
                                    Diagnostic.Kind.ERROR,
                                    "@Variable annotated fields must"
                                            + " neither be private nor final "
                                            + "if they are to be optimized.",
                                    e);
                        }
                    });
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    /**
     * Implements a maximize field of type byte and a setter-method.
     * Creates an attribute that determines if the problem is to be maximized 
     * or minimized (needed for incremental sat).
     * 
     * @return
     * 		the String that "implements" the maximize field and setter-method
     */
    public String setMaximizeSourceCode() {
        return "    private byte maximize;\n\n    "
                + METHOD_PROTOTYPE_SET_MAXIMIZE + " {\n"
                + "        maximize = pMax;\n    }\n";

    }

    /**
     * Implements the threshold field of type double and a setter-method.
     * 
     * @return
     * 		the String that "implements" the threshold field and setter-method
     */
    public String thresholdSourceCode(
            final ExecutableElement methodToBeOptimized,
            final String defaultParsString) {
        StringBuilder sb = new StringBuilder();
        sb.append("    private double thresholdForObjectiveFunction;\n\n");
        sb.append("    " + METHOD_PROTOTYPE_SET_THRESHOLD + " {\n"
                + "        thresholdForObjectiveFunction = pThresh;\n    }\n\n");
        sb.append("    " + METHOD_PROTOTYPE_CBGT + " {\n"
                + "        return thresholdForObjectiveFunction < "
                + ProblemWrapping.PROBLEM_ATTRIBUTE + ".");
        sb.append(methodToBeOptimized.getSimpleName() + "("
                + defaultParsString.toString());
        sb.append(") * maximize;\n    }\n\n");
        return sb.toString();
    }

    /**
     * Implements the getDefaultParams-method.
     * "Returns" all fields that are to be optimized and therefore annotated with the 
     * Variable-annotation, wrapped in an Object array.
     * 
     * @param defaultParsString
     * 			the string containing all Variable-annotated fields
     * @return
     * 			the getDefaultParams-method
     */
    public String defaultParamsSourceCode(final String defaultParsString) {
        return "    " + METHOD_PROTOTYPE_DEFAULT_PARAMS + " {\n"
                + "        Object[] ret = new Object[]{ " + defaultParsString
                + " };\n        " + "for (Object o : ret) {\n            "
                + "if (o == null) {\n                "
                + "throw new RuntimeException(\"@Variable annotated fields must not be null at runtime"
                + " if they are to be optimized.\");\n            }\n"
                + "        }\n" + "        return ret;\n" + "    }\n\n";

    }

    /**
     * Returns calls to the {@link gps.annotations.Variable} annotated 
     * fields in the correct order formatted as a string.
     * Throws an error message, if not every parameter has matching field.
     * 
     * @param parameters
     * 			the parameters of the function that is to be optimized
     * @param variableElements
     * 			the {@link gps.annotations.Variable} annotated fields in the class
     * 
     * @return
     * 			a string containing the calls of the annotated fields
     */
    public String defaultParamsAttributesString(
            final List<? extends VariableElement> parameters,
            final List<Element> variableElements) {

        StringJoiner sjDefaultParams = new StringJoiner(", ");

        // check for Variable-annotated fields for every parameter 
        // of the method that is to be optimized
        for (int i = 0; i < parameters.size(); i++) {
            VariableElement currentParam = parameters.get(i);
            String paramTypeName = currentParam.asType().toString();
            // indicates whether there is a Variable-annotated field 
            // matching the type of the parameter
            boolean elementExisting = false;
            // check every Variable-annotated field
            for (int z = 0; z < variableElements.size(); z++) {
                Element currentField = variableElements.get(z);
                String fieldTypeName = currentField.asType().toString();
                // types equal / parameter type = object
                if (paramTypeName.equals(fieldTypeName)
                        || paramTypeName.equals("java.lang.Object")) {
                    sjDefaultParams.add(ProblemWrapping.PROBLEM_ATTRIBUTE + "."
                            + currentField.getSimpleName());
                    elementExisting = true;
                    variableElements.remove(z);
                    break;
                } else if (currentField.asType() instanceof DeclaredType) {
                    // Inheritance
                    DeclaredType d = (DeclaredType) currentField.asType();
                    if (d.asElement() instanceof TypeElement) {
                        // Check implemented interfaces and superclass(es)
                        TypeElement t = (TypeElement) d.asElement();
                        if (checkAllInterfaces(paramTypeName, fieldTypeName, t)
                                || checkAllSuperclasses(paramTypeName,
                                        fieldTypeName, t)) {
                            sjDefaultParams
                                    .add(ProblemWrapping.PROBLEM_ATTRIBUTE + "."
                                            + currentField.getSimpleName());
                            elementExisting = true;
                            variableElements.remove(z);
                            break;
                        }
                    }
                }
            }
            // no matching field found -> throw error message
            if (!elementExisting) {
                getProcessingEnv().getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "There is no field of type '"
                                + currentParam.asType().toString()
                                + "' or subtype annotated with the @Variable annotation. "
                                + "For every parameter of the @Optimize-annotated method, there has to be "
                                + "a matching @Variable-annotated field in your problem class. "
                                + "In order for us to optimize your problem, we need you to declare "
                                + "an @Variable-annotated field of said type. Additionaly, "
                                + "the field must not be private, final or null (at runtime).",
                        currentParam);
            }
        }
        return sjDefaultParams.toString();
    }

    /**
     * Checks the superclass of the given TypeElement for equality with the given parameter type.
     * Also checks the superclass of the superclass and the implemented interfaces.
     * 
     * @param pName
     * 			the name of the type of the parameter that the equal type is searched for
     * @param fName
     * 			the name of the type of the field that is to be examined 
     * 			(needed for types with generic types)
     * @param t
     * 			the class which superclass is to be examined
     *  
     * @return
     * 			{@code true} if a superclass/an implemented interface is equal to the given parameter type,
     * 			{@code false} otherwise
     */
    public boolean checkAllSuperclasses(final String pName, final String fName,
            final TypeElement t) {
        if (t.getSuperclass().getKind().equals(TypeKind.NONE)) {
            return false;
        } else {
            // check superclass
            String sclassName = t.getSuperclass().toString();
            if (sclassName.equals(pName)) {
                return true;
            } else if (pName.contains("<") && sclassName.contains("<")) {
                // Generics
                if (compareTypesWithGenericTypes(pName, fName, sclassName)) {
                    return true;
                }
            } else if (t.getSuperclass() instanceof DeclaredType) {
                // check superclass and implemented interfaces of superclass
                DeclaredType d = (DeclaredType) t.getSuperclass();
                if (d.asElement() instanceof TypeElement) {
                    return checkAllSuperclasses(pName, fName,
                            (TypeElement) d.asElement())
                            || checkAllInterfaces(pName, fName,
                                    (TypeElement) d.asElement());
                }
            }
        }
        return false;
    }

    /**
     * Checks the implemented interfaces of the given TypeElement for equality with the given parameter type.
     * Also checks the implemented interfaces of the implemented interfaces.
     * 
     * @param pName
     * 			the name of the type of the parameter that the equal type is searched for
     * @param fName
     * 			the name of the type of the field that is to be examined 
     * 			(needed for types with generic types)
     * @param t
     * 			the class which implemented interfaces are to be examined
     *  
     * @return
     * 			{@code true} if a an implemented interface is equal to the given parameter type,
     * 			{@code false} otherwise
     */
    public boolean checkAllInterfaces(final String pName, final String fName,
            final TypeElement t) {
        // check all implemented interfaces
        for (TypeMirror iType : t.getInterfaces()) {
            String interfaceTypeName = iType.toString();
            if (interfaceTypeName.equals(pName)) {
                return true;
            } else if (pName.contains("<") && interfaceTypeName.contains("<")) {
                // Generics
                if (compareTypesWithGenericTypes(pName, fName,
                        interfaceTypeName)) {
                    return true;
                }
            } else {
                // check implemented interfaces of implemented interfaces
                if (iType instanceof DeclaredType) {
                    DeclaredType d = (DeclaredType) iType;
                    if (d.asElement() instanceof TypeElement) {
                        return checkAllInterfaces(pName, fName,
                                (TypeElement) d.asElement());
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the two given types with generic types are equal. 
     * Also checks if the generic types of given the original field and the 
     * given parameter are equal.
     * 
     * @param pName
     * 			the name of the type of the parameter which is to be compared
     * @param fName
     * 			the name of the type of the original field which generic types 
     * 			are to be compared
     * @param sName
     * 			the name of the type of the class which type is to be 
     * 			compared with the type of the given parameter
     *  
     * @return
     * 			{@code true} if the two given types are equal,
     * 			{@code false} otherwise
     */
    public boolean compareTypesWithGenericTypes(final String pName,
            final String fName, final String sName) {
        return pName.substring(0, pName.indexOf("<"))
                .equals(sName.substring(0, sName.indexOf("<")))
                && pName.substring(pName.indexOf("<"), pName.length() - 1)
                        .equals(fName.substring(fName.indexOf("<"),
                                fName.length() - 1));
    }

}
