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
 * Represents an additive linear cooling schedule.
 * 
 * @author mburri
 * 
 */
public class LinearSchedule implements ICoolingSchedule {

    /**
     * The constant value by which the temperature is 
     * decreased after every step
     */
    private final double decreaseValue;

    /**
     * Initializes a new additive linear cooling schedule.
     * 
     * @param pDecr
     * 			the value by which the temperature is to be 
     * 			decreased after every step
     */
    public LinearSchedule(final double pDecr) {
        decreaseValue = pDecr;
    }

    /**
     * Decreases the temperature by subtracting the {@link #decreaseValue}.
     * If the new temperature is < 0, 0 is returned. T(n) = T(0) - decreaseValue * n
     *
     * @param currentTemp
     * 			the current temperature 
     * @return 
     * 			the new temperature
     */
    @Override
    public double decreaseTemperature(double currentTemp) {
        return currentTemp - decreaseValue > 0 ? currentTemp - decreaseValue
                : 0;
    }

    @Override
    public String toString() {
        return "Linear schedule (decrease value: " + decreaseValue + ")";
    }

}
