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

import java.util.Optional;

import javax.lang.model.element.ExecutableElement;

import gps.preprocessing.AbstractAnnotationImplementer;

/**
 * Method Implementer for the {@link gps.annotations.Move} annotation.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class Move extends AbstractAnnotationImplementer {

    /**
     * Construct the Method Implementer
     */
    public Move() {
        super(gps.annotations.Move.class);
    }

    /**
     * The method that is implemented by this implementer.
     */
    private final static String METHOD_PROTOTYPE = "public void applyAction(gps.games.wrapper.Action pAction)";

    /**
     * The name of the has-method that this implementer implements.
     */
    private final static String HAS_METHOD_NAME = "hasApplyActionMethod";

    @Override
    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();

        Optional<ExecutableElement> method = asExecutable().stream()
                .filter(p -> {
                    return p.getParameters().size() == 1;
                }).findAny();

        if (method.isPresent()) {
            sb.append(implementProblemMethodInvokerMethod(METHOD_PROTOTYPE,
                    method.get(), "pAction.get()"));
        } else {
            sb.append(implementNonAnnotatedMethod(METHOD_PROTOTYPE));
        }

        sb.append(implementMethodsToRunableConverter("runnableMoves"));
        sb.append(implementBooleanMethod(HAS_METHOD_NAME, method.isPresent()));

        return sb.toString();
    }

}
