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
package butt.tool.benchmark.common;

/**
 * Class that allows to messure a time interval.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class TimeInterval {

    /**
     * Start Time.
     */
    private long startTimeNanos = -1;

    /**
     * End Time.
     */
    private long endTimeNanos = -1;

    /**
     * Start to messure the time from now on.
     */
    public void start() {
        startTimeNanos = System.nanoTime();
    }

    /**
     * Set the end time to now. The difference between start and end time is the
     * time interval.
     */
    public void end() {
        endTimeNanos = System.nanoTime();
    }

    /**
     * Get the time interval in nanoseconds.
     * 
     * @return Nanoseconds.
     */
    public long deltaNanos() {
        return endTimeNanos - startTimeNanos;
    }

    /**
     * Get the time interval in milliseconds
     * 
     * @return Milliseconds.
     */
    public long deltaMillis() {
        return deltaNanos() / 1000000;
    }

    /**
     * Get the time interval in seconds
     * 
     * @return Seconds.
     */
    public long deltaSeconds() {
        return deltaMillis() / 1000;
    }

    /**
     * Get a well formatted time string that displays a duration in nanoseconds
     * in a human readable format eq. "1w3d3h7m7s777ms1337ns"
     * 
     * @param nanos
     *            The nanoseconds.
     * @return A well formatted String.
     */
    public static String string(long nanos) {
        if (nanos == Long.MAX_VALUE) {
            return "∞";
        }
        int units = 2;
        StringBuilder sb = new StringBuilder();
        long week = nanos / 1000000 / 1000 / 60 / 60 / 24 / 7;
        nanos -= week * 7 * 24 * 60 * 60 * 1000 * 1000000;
        if (week != 0 && units > 0) {
            sb.append(week);
            sb.append('w');
            units--;

        }
        long day = nanos / 1000000 / 1000 / 60 / 60 / 24;
        nanos -= day * 24 * 60 * 60 * 1000 * 1000000;
        if (day != 0 && units > 0) {
            sb.append(day);
            sb.append('d');
            units--;
        }
        long hour = nanos / 1000000 / 1000 / 60 / 60;
        nanos -= hour * 60 * 60 * 1000 * 1000000;
        if (hour != 0 && units > 0) {
            sb.append(hour);
            sb.append('h');
            units--;
        }
        long min = nanos / 1000000 / 1000 / 60;
        nanos -= min * 60 * 1000 * 1000000;
        if (min != 0 && units > 0) {
            sb.append(min);
            sb.append('m');
            units--;
        }
        long seconds = nanos / 1000000 / 1000;
        nanos -= seconds * 1000 * 1000000;
        if (seconds != 0 && units > 0) {
            sb.append(seconds);
            sb.append('s');
            units--;
        }
        long millis = nanos / 1000000;
        nanos -= millis * 1000000;
        if (millis != 0 && units > 0) {
            sb.append(millis);
            sb.append("ms");
            units--;
        }
        if (nanos != 0 && units > 0) {
            sb.append(nanos);
            sb.append("ns");
            units--;
        }

        return sb.toString();
    }

    /**
     * Calculate how much nanoseconds are necessary.
     * 
     * @param done
     *            The items that are already processed
     * @param total
     *            The total amount of items to process
     * @return The estimated time that is necessary to finish all items in
     *         nanoseconds.
     */
    public long calcEta(long done, long total) {
        end();
        if (done < 0) {
            return Long.MAX_VALUE;
        }
        if (done == total) {
            return 0;
        }
        final double timePerItem = ((double) deltaNanos()) / ((double) done);
        final double openItems = total - done;
        final long timeToGo = (long) (openItems * timePerItem);
        return timeToGo;

    }
}
