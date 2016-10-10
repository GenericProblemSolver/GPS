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

import gps.preprocessing.AbstractAnnotationImplementer;

/**
 * Method Implementer for the {@link gps.annotations.Player} annotation.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class Player extends AbstractAnnotationImplementer {

    /**
     * Construct the Method Implementer
     */
    public Player() {
        super(gps.annotations.Player.class);
    }

    /**
     * The method that is implemented by this implementer.
     */
    private final static String METHOD_PROTOTYPE = "public gps.games.wrapper.Player getPlayer()";

    /**
     * The name of the has-method that this implementer implements.
     */
    private final static String HAS_METHOD_NAME = "hasPlayerMethod";

    @Override
    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();
        if (isAnnotationPresent()) {
            sb.append(implementProblemGetterMethod(METHOD_PROTOTYPE,
                    "gps.games.wrapper.Player"));
        } else {
            sb.append(implementNonAnnotatedMethod(METHOD_PROTOTYPE));
        }

        sb.append(
                implementBooleanMethod(HAS_METHOD_NAME, isAnnotationPresent()));

        return sb.toString();
    }

}
