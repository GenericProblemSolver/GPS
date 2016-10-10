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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract class for recycler. A {@link AbstractRecycler} is a {@link Runnable} that can be
 * feed on with solutions for a problem of the given type via {@link #insert(List)}.
 *
 * The meaning of an {@link AbstractRecycler} is to optimize this solutions and find
 * better ones that can be accessed via {@link #getBestElem()}.
 *
 * @author jschloet@tzi.de
 * @param <T> Type of the Problem to be solved
 */
public abstract class AbstractRecycler<T> implements Runnable {

    /**
     * Data structure to collect all new inserted elements.
     **/
    List<List<INode<T>>> inputQueue;

    /**
     * {@link ReentrantLock} to prevent conflict between the insertion of new elements ({@link #insert(List)})
     * into the {@link #inputQueue} and the usage of elements of the {@link #inputQueue}
     *
     * So this lock is used to get safe access to the {@link #inputQueue}.
     */
    ReentrantLock inputLock;

    /**
     * The maximum amount of elements in the {@link #inputQueue}. This is used to limit the memory usage.
     */
    private int inputCapacity;

    AbstractRecycler() {
        super();
        inputCapacity = 26;
        inputLock = new ReentrantLock();
        inputQueue = new ArrayList<>();
    }

    /**
     * Inserts a new element into the {@link #inputQueue}. If the {@link #inputQueue} exceeds
     * the {@link #inputCapacity} after the insertion, the worst element is removed.
     *
     * @param newElem The new inserted element
     */
    public void insert(List<INode<T>> newElem) {
        if (!newElem.isEmpty()) {
            try {
                inputLock.lock();
                inputQueue.add(newElem);
                sortInput();
                if (inputQueue.size() > inputCapacity) {
                    inputQueue.remove(inputQueue.size() - 1);
                }
            } finally {
                inputLock.unlock();
            }
        }
    }

    /**
     * Sorts the {@link #inputQueue} according to {@link NMCS#utilityAbstraction(int, INode)} using
     * the elements size as depth and the last INode in the sequence as terminal.
     */
    private void sortInput() {
        inputQueue.sort((o1, o2) -> Double.compare(
                (-1) * NMCS.utilityAbstraction(o1.size(),
                        o1.get(o1.size() - 1)),
                (-1) * NMCS.utilityAbstraction(o2.size(),
                        o2.get(o2.size() - 1))));
    }

    /**
     * Returns the best element the {@link AbstractRecycler} knows.
     *
     * @return The best known element.
     */
    abstract public List<INode<T>> getBestElem();

    /**
     * Called when {@link Thread#start()} is called. Handles the course
     * of the algorithm. Terminates on interrupts.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            //recycle:
            recycle();
        }
        clean();
    }

    /**
     * This method is called when the {@link AbstractINodeRecycler} is interrupted and
     * going to terminate.
     *
     * Can be overridden to clean things up before that happens.
     */
    protected void clean() {
        //Do nothing
    }

    /**
     * This method it called during the {@link #run()} method.
     * It can be overridden to implement the optimization strategy.
     */
    protected abstract void recycle();

}
