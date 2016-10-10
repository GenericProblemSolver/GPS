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
 * Method Implementer for the {@link gps.annotations.Utility} annotation.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class Utility extends AbstractAnnotationImplementer {

    /**
     * Construct the Method Implementer
     */
    public Utility() {
        super(gps.annotations.Utility.class);
    }

    /**
     * Method that is implemented by this implementer.
     */
    private final static String METHOD_PROTOTYPE_WITH_PLAYER = "public java.lang.Number getUtility(gps.games.wrapper.Player pPlayer)";

    /**
     * Method that is implemented by this implementer.
     */
    private final static String METHOD_PROTOTYPE = "public java.lang.Number getUtility()";

    /**
     * The name of the has-method that this implementer implements.
     */
    private final static String HAS_METHOD_NAME = "hasUtilityMethod";

    /**
     * The name of the has-method that this implementer implements.
     */
    private final static String HAS_METHOD_WITH_PLAYER_NAME = "hasUtilityPlayerMethod";

    @Override
    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();

        // with player
        Optional<ExecutableElement> methodWithPlayer = asExecutable().stream()
                .filter(p -> {
                    return p.getParameters().size() == 1;
                }).findAny();
        if (methodWithPlayer.isPresent()) {
            sb.append(implementProblemMethodInvokerMethod(
                    METHOD_PROTOTYPE_WITH_PLAYER, methodWithPlayer.get(),
                    "pPlayer.get()"));
        } else {
            sb.append(
                    implementNonAnnotatedMethod(METHOD_PROTOTYPE_WITH_PLAYER));
        }
        sb.append(implementBooleanMethod(HAS_METHOD_WITH_PLAYER_NAME,
                methodWithPlayer.isPresent()));

        // without player
        Optional<ExecutableElement> methodWithoutPlayer = asExecutable()
                .stream().filter(p -> {
                    return p.getParameters().size() == 0;
                }).findAny();
        if (methodWithoutPlayer.isPresent()) {
            sb.append(implementProblemGetterMethod(METHOD_PROTOTYPE));
        } else {
            sb.append(implementNonAnnotatedMethod(METHOD_PROTOTYPE));
        }
        sb.append(implementBooleanMethod(HAS_METHOD_NAME,
                methodWithoutPlayer.isPresent()));

        return sb.toString();
    }

}
