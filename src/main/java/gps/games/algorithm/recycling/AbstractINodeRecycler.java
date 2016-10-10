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

import gps.games.algorithm.nestedMonteCarloSearch.NMCS;
import gps.games.wrapper.successor.INode;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link AbstractRecycler} is a {@link Runnable} that can be
 * feed on with solutions for a problem of the given type via {@link #insert(List)}.
 *
 * The meaning of an {@link AbstractRecycler} is to optimize this solutions and find
 * better ones that can be accessed via {@link #getBestElem()}.
 *
 * This class is an extension of {@link AbstractRecycler} that works on {@link RecyclingElement}s, i.e.
 * lists of {@link INode}s. Handles the save input of new elements as well as the determination
 * of the best element.
 *
 * Also manages the population, meaning all known {@link RecyclingElement}s. Takes care of the population
 * to not exceed a fixed size. Executes a selection before inserting new elements to do so.
 *
 * Can be overridden to implement the {@link #recycle()} method. An extension of this class
 * needs to handle the process of optimizing the known elements.
 *
 * @author jschloet@tzi.de
 * @param <T> Type of the Problem to be solved
 */
abstract class AbstractINodeRecycler<T> extends AbstractRecycler<T> {

    /**
     * The population contains the {@link RecyclingElement} that are currently
     * optimized. Meaning that members of the population can be changed during the
     * {@link #recycle()} method.
     *
     * This is devided from the {@link #inputQueue} to prevent concurrency problems.
     */
    List<RecyclingElement<T>> population;

    /**
     * {@link ReentrantLock} used to prevent conflict between {@link #getBestElem()} and {@link #saveBestElem()}.
     */
    private ReentrantLock bestElemLock;

    /**
     * The maximum amount of elements in the {@link #population}. This is used to limit the memory usage.
     */
    private int populationCapacity = 25;

    /**
     * During the {@link #reloading()} method, the worst elements in the {@link #population} are removed.
     * This value decides how many elements are removed (if the {@link #population} is full).
     */
    private int removeRate;

    /**
     * Stores a copy of the best known element. The element is copied and stored outside if {@link #population} to
     * make it available for other threads.
     */
    private RecyclingElement<T> bestElem;

    /**
     * Calls the super constructor an initializes the data structures.
     */
    AbstractINodeRecycler() {
        super();
        population = new ArrayList<>();
        bestElemLock = new ReentrantLock();
        removeRate = 5;
    }

    /**
     * Extracts the best element out of the {@link #population} and
     * stores a copy of it in {@link #bestElem}.
     *
     */
    private void saveBestElem() {
        try {
            bestElemLock.lock();
            if (!population.isEmpty()) {
                sortPopulation();
                bestElem = population.get(0).clone();
            }
        } finally {
            bestElemLock.unlock();
        }
    }

    /**
     * Returns the best element.
     *
     * @return The best known element.
     */
    @Override
    public List<INode<T>> getBestElem() {
        try {
            bestElemLock.lock();
            List<INode<T>> toReturn = (bestElem != null)
                    ? new ArrayList<>(bestElem.elem) : null;
            return toReturn;
        } finally {
            bestElemLock.unlock();
        }
    }

    /**
     * Controls the course of the Recycling process. Calls the {@link #optimize()} method
     * and handles the storage of the {@link #bestElem} as well as the {@link #selection()} and
     * {@link #reloading()} of the {@link #population}.
     */
    @Override
    protected void recycle() {
        //optimize population:
        optimize();
        //Save the best element:
        saveBestElem();
        //Remove the worst elements of population:
        selection();
        // Insert new elements into the population:
        reloading();
    }

    /**
     * Can be overridden to implement the manipulation of already known elements in order
     * to optimize them.
     */
    abstract protected void optimize();

    /**
     * If the the {@link #population} is smaller then the populationCapacity,
     * elements of the {@link #inputQueue} are inserted into the {@link #population}
     * until the {@link #populationCapacity} is reached or the {@link #inputQueue} is
     * empty.
     */
    private void reloading() {
        int space = populationCapacity - population.size();
        while (space > 0 && !inputQueue.isEmpty()) {
            try {
                inputLock.lock();
                population.add(new RecyclingElement<>(inputQueue.get(0)));
                inputQueue.remove(0);
                space--;
            } finally {
                inputLock.unlock();
            }
        }
        sortPopulation();
    }

    /**
     * Removes elements from the {@link #population} until the difference between
     * the size of {@link #population} and {@link #populationCapacity} is at least
     * {@link #removeRate}.
     */
    private void selection() {
        int diff;
        if (population.size() > (diff = populationCapacity - removeRate)) {
            for (int i = 0; i < population.size() - diff; i++) {
                population.remove(population.size() - 1);
            }
        }
    }

    /**
     * Sorts the {@link #population} according to {@link NMCS#utilityAbstraction(int, INode)} using
     * the elements size as depth and the last INode in the sequence as terminal.
     */
    private void sortPopulation() {
        population
                .sort((o1,
                        o2) -> Double
                                .compare(
                                        (-1) * NMCS.utilityAbstraction(
                                                o1.elem.size(), o1.elem.get(
                                                        o1.elem.size() - 1)),
                                        (-1) * NMCS.utilityAbstraction(
                                                o2.elem.size(), o2.elem.get(
                                                        o2.elem.size() - 1))));
    }

}