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
package gps.games.analysis.util;

/**
 * This class offers two simple methods to compare the change in memory usage between the calls.
 * 
 * @author Fabian
 */
public class MemoryAnalysisUtility {

    /**
     * The runtime object currently used to communicate with the environment
     */
    private static final Runtime runtime = Runtime.getRuntime();

    /**
     * The long's used to save the memory usage in bytes
     */
    private static Long before, after;

    /**
     * Returns the amount of memory currently used by the JVM.
     * 
     * @return The amount of memory used in bytes. Calculated by totalMemory-freeMemory
     */
    private static long getMemoryUsage() {
        hardClean();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Used to start monitoring changes in memory usage. Must be called at least once.
     */
    public static void startMeasurement() {
        hardClean();
        before = after = getMemoryUsage();
    }

    /**
     * Will calculate the increase in memory compared to the moment of the most recent {@code startMeasurement()} call.
     * Will throw an {@code IllegalStateException()} if {@code startMeasurement()} was not called until now. 
     * 
     * @return A long value representing the increase in memory usage, comparing between the time {@code startMeasurement()} and
     *  {@code getMemoryIncrease()} were called. Since the return represents an increase in memory, a negative return value 
     *  represents a decrease in memory usage compared to the moment {@code startMeasurement()} was called.
     */
    public static long getMemoryIncrease() {
        if (before == null || after == null) {
            throw new IllegalStateException(
                    "You must call startMeasurement() at least once before calling this method");
        }
        hardClean();
        after = getMemoryUsage();
        long diff = after - before;
        return (diff);
    }

    /**
     * Will clear as much memory as possible, resulting in a more accurate comparision
     */
    private static void hardClean() {
        // Running it once is actually not enough
        // Running it multiple times results in a way more accurate memory reading
        for (int i = 0; i < 5; ++i) {
            runtime.runFinalization();
            Thread.yield();
            runtime.gc();
        }
    }
}
