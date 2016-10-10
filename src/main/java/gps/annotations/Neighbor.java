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
 * solution or a list of solutions created by modifying a given solution. 
 * Using this annotation without using the {@link gps.annotations.Optimize} 
 * annotation does not yield any results. <br>
 * The parameters of the annotated method are to be equal to the parameters 
 * of the {@link gps.annotations.Optimize}-annotated method. <br>
 * If the method has exactly one parameter, the method is allowed to return an 
 * object of the same type as the parameter or a {@link java.util.List} of 
 * objects of the same type as the parameter. <br>
 * If the method has multiple parameters, the method is allowed to return an 
 * {@code java.lang.Object[]} containing an object for every parameter, arranged 
 * in the same order, or a {@link java.util.List} of {@code java.lang.Object[]} 
 * each containing an object for every parameter, arranged in the same order. <br>
 * Generally speaking, you have to be able to invoke the {@link gps.annotations.Optimize}-
 * annotated method with everything this method returns (if it returns a List, every 
 * element in the List) as arguments. 
 */
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Neighbor {

}
