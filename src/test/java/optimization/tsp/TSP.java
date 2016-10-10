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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import gps.annotations.Neighbor;
import gps.annotations.Optimize;
import gps.annotations.Variable;

/**
 * Implementation of the traveling salesman problem.
 * 
 * @author mburri
 *
 */
public class TSP {

    /**
     * Random generator used to determine the two cities 
     * to be swapped by the {@link #randomTwoOpt(List)}-method
     */
    private final Random rand = new Random();

    /**
     * The current tour implemented as a list of cities
     */
    @Variable
    public List<City> cities;

    /**
     * Creates a new random instance of the TSP 
     * with the given number of cities.
     * 
     * @param numberOfNodes
     * 			the number of cities
     */
    public TSP(final int numberOfNodes) {
        cities = new ArrayList<City>();
        for (int i = 0; i < numberOfNodes; i++) {
            cities.add(new City());
        }
        Collections.shuffle(cities);
    }

    /**
     * Creates a new instance of the TSP for the given 
     * list of cities.
     * 
     * @param pCities
     * 			the list of cities
     */
    public TSP(final List<City> pCities) {
        if (pCities == null) {
            throw new IllegalArgumentException(
                    "Given list of cities must not be null");
        }
        cities = pCities;
    }

    /**
     * Cost-function for a list of nodes which should be minimized
     * 
     * @param tour
     * 			the tour/list of nodes that the costs are calculated for
     * @return 
     * 			the costs of the tour
     */
    @Optimize
    public double getCosts(final List<City> tour) {
        double costs = 0.0;
        for (int i = 0; i < tour.size(); i++) {
            final City c1 = tour.get(i);
            final City c2 = tour.get((i + 1) % tour.size());
            costs += c1.distanceTo(c2);
        }
        return costs;
    }

    /**
     * Randomly selects two cities in the given tour and 
     * exchanges their positions. 
     * 
     * @param tour
     * 			the tour that is to be changed
     * @return
     * 			the changed tour
     */
    @Neighbor
    public List<City> randomTwoOpt(final List<City> tour) {
        List<City> neighborTour = new ArrayList<City>();
        neighborTour.addAll(tour);
        if (tour.size() == 0) {
            return neighborTour;
        }

        int firstSwap = rand.nextInt(tour.size());
        int sndSwap = rand.nextInt(tour.size());
        while (sndSwap == firstSwap) {
            sndSwap = rand.nextInt(tour.size());
        }
        City c1 = neighborTour.get(firstSwap);
        City c2 = neighborTour.get(sndSwap);

        neighborTour.remove(firstSwap);
        neighborTour.add(firstSwap, c2);
        neighborTour.remove(sndSwap);
        neighborTour.add(sndSwap, c1);

        return neighborTour;
    }

    /**
     * Returns the tour formatted as a String
     * 
     * @return	the current tour
     */
    public String getTourString() {
        StringBuilder sj = new StringBuilder();
        for (int i = 0; i < cities.size(); i++) {
            sj.append((i + 1) + ". " + cities.get(i).toString() + "\n");
        }
        return sj.toString();
    }

    @Override
    public String toString() {
        return String.valueOf(cities.size()) + " cities";
    }

}