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

import java.util.stream.Collectors;

import javax.lang.model.type.TypeKind;

import gps.preprocessing.AbstractAnnotationImplementer;

/**
 * Method Implementer for the {@link gps.annotations.Action} annotation.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class Action extends AbstractAnnotationImplementer {

    /**
     * Construct the Method Implementer
     */
    public Action() {
        super(gps.annotations.Action.class);
    }

    /**
     * The method that is implemented by this implementer.
     */
    private final static String METHOD_PROTOTYPE = "public java.util.List<gps.games.wrapper.Action> getActions()";

    /**
     * The name of the has-method that this implementer implements.
     */
    private final static String HAS_METHOD_NAME = "hasActionMethod";

    @Override
    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();
        if (isAnnotationPresent()) {
            sb.append(implementCollectionProblemGetterMethod(METHOD_PROTOTYPE,
                    getUsedElements().stream().filter(p -> {
                        return !(getReturnTypeOfElement(p).getKind()
                                .equals(TypeKind.VOID));
                    }).collect(Collectors.toSet()),
                    "gps.games.wrapper.Action"));

        } else {
            sb.append(implementNonAnnotatedMethod(METHOD_PROTOTYPE));
        }

        sb.append(implementMethodsToRunableConverter("runnableActions"));
        sb.append(
                implementBooleanMethod(HAS_METHOD_NAME, isAnnotationPresent()));

        return sb.toString();
    }
}
