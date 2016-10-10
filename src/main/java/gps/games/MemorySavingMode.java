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
package gps.games;

/**
 * Enum with the different memory saving modes.
 *
 * @author haker@uni-bremen.de
 */
public enum MemorySavingMode {

    /**
     * No memory saving means will be used.
     */
    NONE,

    /**
     * {@link gps.attribute.AttributeGraph}s will be used to determine if the
     * field`s values of an origin game object and the field`s values of a deep
     * copy of this object, after the application of an action, are equal.
     * <p>
     * If the value of a field of the origin and the deep copied game object are
     * equal, the deep copy's field value will be replaced by a reference to the
     * origin game object's field value.
     * <p>
     * This memory saving mode can handle all fields regardless of their access
     * modifier or final keyword. Since it uses the Java Reflection API, it is
     * slightly slower than {@link #PREPROCESSING}.
     * <p>
     * This approach needs a properly overridden {@link Object#equals(Object)}
     * method for every class, which acts as a field type of the game class.
     * <p>
     * <b>Important:</b> This mode and also {@link #PREPROCESSING} do not work
     * recursively, they only consider fields of the problem class and compare
     * their equality. This means, that if you want to really capitalize one of
     * the memory saving modes, objects that are not changed by the application
     * of an action, but are very memory hungry, must be located in your problem
     * class.
     */
    ATTRIBUTE_GRAPH,

    /**
     * This memory saving mode works similarly to {@link #ATTRIBUTE_GRAPH}, but
     * uses methods implemented by the preprocessor to get all field values,
     * whose equality should be checked, and to replace field values.
     * <p>
     * This mode only works for {@code public}, {@code non-final} fields ({@code
     * final} fields are ignored). So it is possible to control the fields that
     * are considered for this memory saving mode by setting their access
     * modifier accordingly.
     * <p>
     * This approach also needs a properly overridden {@link
     * Object#equals(Object)} method for every class, which acts as a field type
     * of the game class.
     * <p>
     * <b>Important:</b> This mode and also {@link #ATTRIBUTE_GRAPH} do not work
     * recursively, they only consider fields of the problem class and compare
     * their equality. This means, that if you want to really capitalize one of
     * the memory saving modes, objects that are not changed by the application
     * of an action, but are very memory hungry, must be located in your problem
     * class.
     */
    PREPROCESSING
}
