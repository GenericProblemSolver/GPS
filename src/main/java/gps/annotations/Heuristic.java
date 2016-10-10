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
 * compute a heuristic evaluation for a state and return it as number.<br> If
 * used for a field, the field should contain the respective heuristic
 * evaluation numerical value.
 * <p>
 * For multiplayer games a heuristic method with player parameter is expected.
 * The heuristic method should then compute the heuristic value for the given
 * player. A heuristic method for a singleplayer game can be without parameter
 * or you can annotate a field as described above.
 * <p>
 * The implementation of a heuristic method is optional, but some algorithms
 * need a heuristic to work in general and most algorithms are faster, if a
 * heuristic is provided.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface Heuristic {

}
