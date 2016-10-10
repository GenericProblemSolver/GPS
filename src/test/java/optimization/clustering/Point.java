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
package optimization.clustering;

import java.util.Random;

/**
 * A point in a cluster
 * 
 * @author oesterca
 */
public class Point {

    /**
     * x-coordinate of the point
     */
    private final double x;

    /**
     * y-coordinate of the point
     */
    private final double y;

    /**
     * Creates a new point with the default coordinates x=0,y=0
     */
    public Point() {
        x = 0.0;
        y = 0.0;
    }

    /**
     * Creates a new point with coordinates in the given range.
     * 
     * @param maxCoordinate
     * 		the range
     */
    public Point(final int maxCoordinate) {
        final Random r = new Random();
        x = r.nextInt(maxCoordinate);
        y = r.nextInt(maxCoordinate);
    }

    /**
     * Creates a new point for the given coordinates 
     * 
     * @param pX    the x-coordinate
     * @param pY    the y-coordinate
     */
    public Point(final double pX, final double pY) {
        x = pX;
        y = pY;
    }

    @Override
    public String toString() {
        return "" + x + ", " + y;
    }

    /**
     * Returns the x-coordinate
     * @return  the x-coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y-coordinate
     * @return  the y-coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Calculates the euclidian distance for two points
     * 
     * @param p1    the first point
     * @param p2    the second point
     * @return      the euclidian distance
     */
    public static double euclideanDistance(final Point p1, final Point p2) {
        return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2)
                + Math.pow(p1.getY() - p2.getY(), 2));
    }
}
