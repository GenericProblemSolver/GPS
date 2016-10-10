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
package gps.games.algorithm.recycling;

import gps.games.wrapper.successor.INode;
import java.util.ArrayList;
import java.util.List;

/**
 * Data structure for elements of the population in {@link AbstractINodeRecycler}.
 * Contains a solution for a problem of a given type, i.e. a list of {@link INode}s and
 * an id for this element.
 *
 * @author jschloet@tzi.de
 * @param <T> Type of the problem to be solved
 */
class RecyclingElement<T> implements Cloneable {

    /**
     * static counter used to create the ids
     */
    private static int counter = 0;

    /**
     * Id of this element
     */
    private int id;

    /**
     * Solution of the problem to be solved
     */
    List<INode<T>> elem;

    /**
     * Constuctor sets the {@link #elem} with the given list.
     * Increases the {@link #counter} and sets the {@link #id}.
     *
     * @param pElem The new value of {@link #elem}
     */
    RecyclingElement(List<INode<T>> pElem) {
        counter++;
        id = counter;
        elem = pElem;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object other) {
        return !(other == null || !(other instanceof RecyclingElement))
                && id == ((RecyclingElement) other).id;
    }

    /**
     * Overrides the clone method. Creates a new {@link RecyclingElement} with the same id
     * and the same elements in a different list.
     *
     * @return A new {@link RecyclingElement}
     */
    @Override
    public RecyclingElement<T> clone() {
        RecyclingElement<T> clone = new RecyclingElement<>(
                new ArrayList<>(elem));
        clone.id = id;
        return clone;
    }
}