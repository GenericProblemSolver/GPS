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

/**
 * This annotation is used for a public method. The method should return a 
 * fitness value for a given set of parameters that is to be optimized.<br>
 * Allowed return types are int, double, byte, float, long, short and their wrappers.<br>
 * If this annotation is used, you also need a {@link gps.annotations.Variable} annotated 
 * field for every parameter (same type or subtype) of the function annotated with this 
 * annotation. These fields are the initial values of the optimization process and must 
 * neither be private nor final. Additionally, they must not be null at runtime.
 * To avoid unpredictable behavior, the annotated method should return a positive value. 
 * 
 * At the end of the optimization process, the problem is slightly modified and passed 
 * to a BytecodeOptimizationInterface to check whether the found solution is optimal. 
 * In order for us to be able to do that, the use of the {@link gps.annotations.Variable} 
 * annotation has to be in line with the requirements specified by the BytecodeGroup.
 */
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Optimize {

}
