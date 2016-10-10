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
package gps.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used for a public method or field. The method should
 * return all legal moves from the current game state. <br> If used
 * for a field, the field should include all legal moves from the
 * current game state.
 * <p>
 * Arrays, classes implementing {@link java.util.Collection} or the class
 * representing your move (naturally only applies, if there is just one
 * legal move in every state) are valid field types or return values.
 * <p>
 * If this annotation is used, you also need a method expecting one action as
 * parameter and annotated with {@link Move} to enable the solver to apply the
 * returned actions.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface Action {

}