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
package gps;

import gps.games.wrapper.IGame;
import gps.optimization.wrapper.IOptimizable;

/**
 * This interface represents a wrapped Problem. After preprocessing problems get
 * wrapped in this Interface.
 * 
 * @author haker@uni-bremen.de
 *
 */
public interface IWrappedProblem<T> extends IGame, IOptimizable {

    /**
     * Retrieves the original unwrapped problem object.
     * 
     * @return the unwrapped problem object.
     */
    public T getSource();

    /**
     * Get the attributes of the problem class.
     * 
     * @return The attributes. Is never {@code null}. May be empty.
     */
    public Object[] getAttributes();

    /**
     * Set a attribute to a specific value.
     * 
     * @param index
     *            The Index of the attribute. Use {@link #getAttributes()} to
     *            retrieve the Array. The index of the element in this array
     *            goes here.
     * @param val
     *            The value to that the attribute is set to. The type must match
     *            the attribute type otherwise a
     *            {@link java.lang.ClassCastException} is thrown.
     */
    public void setAttribute(int index, Object val);

    /**
     * Checks whether an attribute is final.
     * 
     * @param index
     *            The index of the attribute in the array returned by
     *            {@link #getAttributes()}.
     * @return {@code true} if the attribute with the given index is final.
     */
    public boolean isAttributeFinal(int index);
}
