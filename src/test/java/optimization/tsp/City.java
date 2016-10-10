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
package optimization.tsp;

import java.util.Random;

/**
 * Implementation of a city used by the {@link optimization.tsp.TSP}.
 * 
 * @author mburri
 *
 */
public class City {

    /**
     * The x-coordinate of the city
     */
    private final int xCoordinate;

    /**
     * The y-coordinate of the city
     */
    private final int yCoordinate;

    /**
     * Creates a new city with random coordinates
     */
    public City() {
        final Random rand = new Random();
        xCoordinate = rand.nextInt(1000);
        yCoordinate = rand.nextInt(1000);
    }

    /**
     * Creates a new city for the given coordinates
     * 
     * @param pX
     * 			the x-coordinate
     * @param pY
     * 			the y-coordinate
     */
    public City(final int pX, final int pY) {
        xCoordinate = pX;
        yCoordinate = pY;
    }

    /**
     * Gets the x-coordinate of the city
     * 
     * @return
     * 			the x-coordinate
     */
    public int getX() {
        return xCoordinate;
    }

    /**
     * Gets the y-coordinate of the city
     * 
     * @return
     * 			the y-coordinate
     */
    public int getY() {
        return yCoordinate;
    }

    /**
     * Gets the distance between this city and the given city.
     * 
     * @param pCity
     * 			the city that the distance is to be calculated for
     * @return
     * 			the distance
     */
    public double distanceTo(final City pCity) {
        int xDistance = Math.abs(xCoordinate - pCity.getX());
        int yDistance = Math.abs(yCoordinate - pCity.getY());
        double distance = Math
                .sqrt((xDistance * xDistance) + (yDistance * yDistance));
        return distance;
    }

    @Override
    public String toString() {
        return getX() + ", " + getY();
    }

}
