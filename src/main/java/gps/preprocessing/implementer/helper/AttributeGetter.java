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

import javax.lang.model.element.Modifier;
import java.util.List;

import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import gps.preprocessing.AbstractImplementer;

/**
 * Method Implementer for the {@link gps.IWrappedProblem#getAttributes()}
 * method.
 * 
 * Also implements the {@link gps.IWrappedProblem#setAttribute(int, Object)} and
 * {@link gps.IWrappedProblem#isAttributeFinal(int)} methods.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class AttributeGetter extends AbstractImplementer {

    /**
     * Construct the Method Implementer
     */
    public AttributeGetter() {
        super();
    }

    /**
     * The name of the problem attribute
     */
    public static final String PROBLEM_ATTRIBUTE = "problem";

    @Override
    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();

        List<VariableElement> fields = ElementFilter
                .fieldsIn(getProblemClass().getEnclosedElements());
        VariableElement[] validFields = fields.stream()
                .filter(p -> p.getModifiers().contains(Modifier.PUBLIC))
                .toArray(i -> new VariableElement[i]);

        { // implement getAttributes
            sb.append("    public Object[] getAttributes() {\n");

            sb.append("        return new Object[]{\n");
            for (VariableElement e : validFields) {
                sb.append("            ");
                sb.append(ProblemWrapping.PROBLEM_ATTRIBUTE);
                sb.append('.');
                sb.append(e.getSimpleName());
                sb.append(",\n");
            }
            sb.append("        };\n");
            sb.append("    }\n\n");
        }

        { // implement setAttribute
            sb.append("    public void setAttribute(int i, Object o) {\n");
            sb.append("        switch(i) {\n");

            int i = 0;
            for (VariableElement e : validFields) {
                sb.append("        case ");
                sb.append(i++);
                sb.append(":\n");
                if (!e.getModifiers().contains(Modifier.FINAL)) {
                    sb.append("            ");
                    sb.append(ProblemWrapping.PROBLEM_ATTRIBUTE);
                    sb.append('.');
                    sb.append(e.getSimpleName());
                    sb.append(" = (");
                    sb.append(e.asType().toString());
                    sb.append(") o;\n");
                }
                sb.append("            break;\n");
            }
            sb.append("        };\n");
            sb.append("    }\n\n");
        }

        { // implement isAttributeFinal
            sb.append("    public boolean isAttributeFinal(int i) {\n");

            sb.append("        return ");

            // will stay true until at least one final has been found
            boolean first = true;

            for (int i = 0; i < validFields.length; i++) {
                if (validFields[i].getModifiers().contains(Modifier.FINAL)) {
                    if (!first) {
                        sb.append(" || ");
                    }
                    sb.append("i == ");
                    sb.append(i);
                    first = false;
                }
            }

            // no final has been found, so we return false always.
            if (first) {
                sb.append("false");
            }

            sb.append(";\n    }\n\n");
        }

        return sb.toString();
    }

}
