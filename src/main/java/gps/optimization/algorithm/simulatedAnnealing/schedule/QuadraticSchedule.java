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
 * Represents a multiplicative quadratic cooling schedule.
 * 
 * @author mburri
 * 
 */
public class QuadraticSchedule implements ICoolingSchedule {

    /**
     * Factor used to decrease the temperature after every step
     */
    private final double decreaseFactor;

    /**
     * The initial temperature (needed for the calculation of the decreased 
     * temperature).
     */
    private final double initialTemperature;

    /**
     * The number of the current cycle of the cooling process.
     */
    int currentStep;

    /**
     * Initializes a new quadratic exponential cooling schedule.
     * 
     * @param pDecr
     * 			the factor used to decrease the temperature
     * @param pInitTemp
     * 			the initial temperature of the cooling process
     */
    public QuadraticSchedule(final double pDecr, final double pInitTemp) {
        if (pDecr < 0) {
            throw new IllegalArgumentException(
                    "Decrease factor for Quadratic Schedule "
                            + "must be greater than 0");
        }
        decreaseFactor = pDecr;
        initialTemperature = pInitTemp;
        currentStep = 0;
    }

    /**
     * Calculates the current temperature.
     * T(n) = T(0) / 1 + decreaseFactor * n^2
     * 
     * @param currentTemp
     * 			the current temperature 
     * @return 
     * 			the new temperature
     */
    @Override
    public double decreaseTemperature(double currentTemp) {
        currentStep++;
        return initialTemperature
                / (1 + decreaseFactor * Math.pow(currentStep, 2));
    }

    @Override
    public String toString() {
        return "Quadratic schedule (Decrease factor: " + decreaseFactor + ")";
    }

}
