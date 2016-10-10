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
package gps.games.util.transpositionTable;

import gps.games.wrapper.Game;
import gps.util.Tuple;

import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a transposition table, which maps game states (more
 * accurately: hash codes of game states) to the evaluation value of the game
 * state. The table has a fixed size and uses buckets of a fixed size for the
 * case of index collisions.
 *
 * @author alueck@uni-bremen.de
 */
public class TranspositionTable<T> {

    /**
     * An array of length {@link #maxSize} for {@link LinkedList}s, which act as
     * buckets.
     */
    private List<AbstractTTEntry>[] table;

    /**
     * Maximum number of buckets.
     */
    private final int maxSize;

    /**
     * Maximum number of elements one bucket can contain.
     */
    private final static int BUCKET_SIZE = 5;

    /**
     * True, if the table entries should contain exact minimax values, false if
     * the should contain bounds.
     */
    private final boolean useExactValue;

    /**
     * Creates a new {@link TranspositionTable} with {@code pMaxSize} number of
     * buckets. So the maximum possible number of items stored by this table is
     * the product of {@code pMaxSize} and {@link #BUCKET_SIZE}.
     *
     * @param pMaxSize
     *         maximum number of bucket
     * @param pUseExactValue
     *         {@code true}, if the table entries should contain exact minimax
     *         values, {@code false} if they should contain minimax bounds
     */
    @SuppressWarnings("unchecked")
    public TranspositionTable(final int pMaxSize,
            final boolean pUseExactValue) {
        if (pMaxSize <= 0) {
            throw new IllegalArgumentException();
        }
        maxSize = pMaxSize;
        useExactValue = pUseExactValue;
        table = (List<AbstractTTEntry>[]) new LinkedList<?>[maxSize];
    }

    /**
     * <b>If you chose to store exact minimax values, you must use {@link
     * #put(Game, Number, int)} instead.</b>
     * <p>
     * Puts a mapping of the given key (game state) to the given eval and depth
     * values into the transposition table, if there is not already an entry for
     * the given key or if there is already an entry for the given key, which
     * has a shallower depth than the given depth.<br> If the entry for the
     * given key, would belong to a already full bucket, it replaces the
     * bucket's entry with the shallowest depth (if it is shallower than the
     * given depth).  Otherwise no new entry for the given key is created.
     *
     * @param key
     *         the key (game state) for which the mapping of eval and depth
     *         value should be created, must not be null
     * @param lowerBound
     *         lower bound minimax value for the given game state, must not be
     *         null and must be of the same class as upperBound
     * @param upperBound
     *         upper bound minimax value for the given game state, must not be
     *         null and must be of the same class as lowerBound
     * @param depth
     *         the depth at which the transposition has been found, must be >=
     *         0
     */
    public void put(final Game<T> key, final Number lowerBound,
            final Number upperBound, final int depth) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        if (useExactValue) {
            throw new IllegalArgumentException(
                    "Use put(Game, Number, int) instead.");
        }
        int hash = key.hashCode();
        BoundsTTEntry entry = new BoundsTTEntry(hash, depth, lowerBound,
                upperBound);
        put(entry);
    }

    /**
     * <b>If you chose to store minimax bounds, you must use {@link #put(Game,
     * Number, Number, int)} instead.</b>
     * <p>
     * Works like {@link #put(Game, Number, Number, int)}, but uses {@link
     * ExactTTEntry} instead of {@link BoundsTTEntry};
     *
     * @param key
     *         the key (game state) for which the mapping of eval and depth
     *         value should be created, must not be null
     * @param exactValue
     *         the exact minimax value, must not be null
     * @param depth
     *         the depth at which the transposition has been found, must be >=
     *         0
     */
    public void put(final Game<T> key, final Number exactValue,
            final int depth) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        if (!useExactValue) {
            throw new IllegalArgumentException(
                    "Use put(Game, Number, Number, int) instead.");
        }
        int hash = key.hashCode();
        ExactTTEntry entry = new ExactTTEntry(hash, depth, exactValue);
        put(entry);
    }

    /**
     * Searches in the transposition table for a {@link AbstractTTEntry}, where
     * {@link AbstractTTEntry#hashCode} equals key.hashCode(). If a
     * corresponding entry is found, a {@link Tuple} of the lower bound and
     * upper bound value is returned, if {@link #useExactValue} is set to {@code
     * false}.
     * <p>
     * Otherwise both {@link Tuple} entries will contain the exact minimax
     * value.
     * <p>
     * If no {@link AbstractTTEntry} is found, {@code null} is returned.
     *
     * @param key
     *         game state to get the evaluation value for
     *
     * @return given that a {@link AbstractTTEntry} for the given key exists: a
     * {@link Tuple} of lower bound and upper bound value for the given key, if
     * {@link #useExactValue} is set to {@code false}, otherwise a {@link Tuple}
     * where both entries contain the exact minimax value or {@code null} if
     * there is no {@link AbstractTTEntry} for the given key
     */
    public Tuple<Number, Number> get(final Game<T> key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        int hash = key.hashCode();
        int index = hash % maxSize;
        if (index < 0) {
            index += maxSize;
        }
        List<AbstractTTEntry> list = table[index];
        if (list != null) {
            for (AbstractTTEntry entry : list) {
                if (entry.hashCode == hash) {
                    if (entry instanceof ExactTTEntry) {
                        return new Tuple<>(((ExactTTEntry) entry).exactValue,
                                ((ExactTTEntry) entry).exactValue);
                    } else if (entry instanceof BoundsTTEntry) {
                        return new Tuple<>(((BoundsTTEntry) entry).lowerBound,
                                ((BoundsTTEntry) entry).upperBound);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Deletes all entries from the transposition table.
     */
    @SuppressWarnings("unchecked")
    public void clear() {
        table = (LinkedList<AbstractTTEntry>[]) new LinkedList<?>[maxSize];
    }

    /**
     * See javadoc of {@link #put(Game, Number, Number, int)}.
     *
     * @param entry
     *         class extending {@link AbstractTTEntry} to be added
     */
    private void put(final AbstractTTEntry entry) {
        if (useExactValue && !(entry instanceof ExactTTEntry)
                || !useExactValue && !(entry instanceof BoundsTTEntry)) {
            throw new IllegalArgumentException();
        }
        int index = entry.hashCode % maxSize;
        if (index < 0) {
            index += maxSize;
        }
        List<AbstractTTEntry> list = table[index];
        if (list == null) {
            list = new LinkedList<>();
            list.add(entry);
            table[index] = list;
        } else if (list.size() < BUCKET_SIZE) {
            // check if there is already an entry with the same hashcode in
            // the bucket
            boolean entryExists = false;
            for (AbstractTTEntry listEntry : list) {
                entryExists = listEntry.hashCode == entry.hashCode;
                if (entryExists && (listEntry.depth < entry.depth
                        || !listEntry.equals(entry)
                                && listEntry.depth < entry.depth)) {
                    listEntry.update(entry);
                    return;
                } else if (entryExists) {
                    break;
                }
            }
            if (!entryExists) {
                list.add(entry);
            }
        } else {
            AbstractTTEntry minDepthEntry = null;
            int minDepth = Integer.MAX_VALUE;
            // check if there is already an entry with the same hashcode in
            // the bucket and simultaneously fetch the entry with the lowest
            // depth, so it could be removed if there is no entry with the
            // same hashcode
            boolean entryExists;
            for (AbstractTTEntry listEntry : list) {
                entryExists = listEntry.hashCode == entry.hashCode;
                if (entryExists && (listEntry.depth < entry.depth
                        || !listEntry.equals(entry)
                                && listEntry.depth < entry.depth)) {
                    listEntry.update(entry);
                    return;
                }
                if (listEntry.depth < minDepth) {
                    minDepth = listEntry.depth;
                    minDepthEntry = listEntry;
                }
            }
            // replace entry with low depth with one with higher depth
            if (minDepth < entry.depth) {
                list.remove(minDepthEntry);
                list.add(entry);
            }
        }
    }
}