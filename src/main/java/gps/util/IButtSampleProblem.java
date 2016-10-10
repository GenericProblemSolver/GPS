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
package gps.util;

/**
 * Interface that is implemented in sample problems. All classes that implement
 * this interface can provide a faster method for generating an identifier. If
 * this interface is not present a serialized version of the problem object is
 * used as identifier.
 * 
 * @author haker@uni-bremen.de
 *
 */
public interface IButtSampleProblem {

    /**
     * The identifier. Each defined problem state must be represented by a
     * single unique identifier. The identifier must provide a
     * {@link #hashCode()} and a proper {@link #equals(Object)} method.
     * 
     * @return The identifier.
     */
    public Object getIdentifier();
}
