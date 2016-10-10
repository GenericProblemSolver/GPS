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
package gps.optimization.algorithm.simulatedAnnealing.schedule;

/**
 * Represents a multiplicative exponential cooling schedule.
 * 
 * @author mburri
 * 
 */
public class ExponentialSchedule implements ICoolingSchedule {

    /**
     * Factor by which the temperature is decreased after every step
     */
    private final double decreaseFactor;

    /**
     * Initializes a new multiplicative exponential cooling schedule.
     * 
     * @param pDecr
     * 			the decrease factor
     */
    public ExponentialSchedule(final double pDecr) {
        if (pDecr > 1 || pDecr <= 0) {
            throw new IllegalArgumentException(
                    "Decrease factor for Exponential Schedule"
                            + " must be a double between 0 and 1");
        }
        decreaseFactor = pDecr;
    }

    /**
     * Decreases the temperature by multiplying it with the 
     * {@link #decreaseFactor}. T(n) = T(0) * (decreaseFactor ^ n)
     * 
     * @param currentTemp
     * 			the current temperature
     * @return
     * 			the new temperature
     */
    @Override
    public double decreaseTemperature(double currentTemp) {
        return currentTemp * decreaseFactor;
    }

    @Override
    public String toString() {
        return "Exponential schedule (Factor: " + decreaseFactor + ")";
    }

}
