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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface Variable {
    VariableDepth value() default VariableDepth.Flat;

    public static enum VariableDepth {
        /**
         * The Field should not be considered a Variable
         */
        None,

        /**
         * The Field is a Variable of the Problem. GPS should try to find a value for it.
         * If the field is an (multidimensional-) Array, all of its Elements have VariableDepth.Flat
         * If the field is a Reference to an Object, the Reference itself is a Variable, i.e. the Object it points to is not
         */
        Flat,

        /**
         * The Field is a Variable of the Problem. GPS should try to find a value for it.
         * If the field is an (multidimensional-) Array, all of its Elements have VariableDepth.Deep
         * If the field is a Reference to an Object, all fields of the Object have VariableDepth.Deep
         */
        Deep
    }
}
