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
package gps.preprocessing.implementer.helper;

import gps.preprocessing.AbstractImplementer;
import gps.preprocessing.AnnotationProcessor;

/**
 * Method Implementer for the {@link gps.IWrappedProblem#getSource()} method.
 * Also implements the Constructor and the Attribute where the wrapped problem
 * object is stored.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class ProblemWrapping extends AbstractImplementer {

    /**
     * Construct the Method Implementer
     */
    public ProblemWrapping() {
        super();
    }

    /**
     * The name of the problem attribute
     */
    public static final String PROBLEM_ATTRIBUTE = "problem";

    @Override
    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();

        // Problem Object as Attribute: "private final xxx problem;"
        sb.append("    private final ");
        sb.append(getProblemClass().toString());
        sb.append(" " + PROBLEM_ATTRIBUTE + ";\n\n");

        // Constructor with Problem Object assigns Problem attribute
        sb.append("    public "
                + AnnotationProcessor.getSafeClassName(getProblemClass()) + "("
                + getProblemClass() + " pProblem) { " + PROBLEM_ATTRIBUTE
                + " = pProblem; };\n\n");

        // Getter for original problem object (defined in IWrappedProblem)
        sb.append("    public " + getProblemClass() + " getSource() { return "
                + PROBLEM_ATTRIBUTE + "; }\n\n");

        return sb.toString();
    }

}
