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
package gps.optimization.algorithm.simulatedAnnealing;

import gps.ResultEnum;
import gps.optimization.algorithm.AbstractOptimizer;
import gps.optimization.algorithm.simulatedAnnealing.schedule.ICoolingSchedule;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.wrapper.Optimizable;

/**
 * Implements the simulated annealing algorithm. With a slowly decreasing 
 * probability, this heuristic optimization algorithm also accepts worse solutions 
 * as the new current solution in order to escape local optima. 
 * 
 * @author bargen, mburri
 *
 * @param <T>
 *            the type of the original problem class
 *            
 * @see <a href="http://www.hs-augsburg.de/informatik/projekte/mebib/emiel/entw_inf/or_verf/2d_vis.html">
 *      Link</a>          
 */
public class SimulatedAnnealing<T> extends AbstractOptimizer<T> {

    /**
     * The initial temperature if no initial temperature was given
     */
    private final static double DEFAULT_INITIAL_TEMPERATURE = Double.MAX_VALUE;

    /**
     * The current temperature.
     */
    private double temperature;

    /**
     * The cooling schedule that regulates the temperature
     */
    private ICoolingSchedule coolingSchedule;

    /**
     * The initial temperature. Stored only for output purposes
     */
    private double initialTemperature;

    /**
     * Initializes a new instance of the SimulatedAnnealing algorithm 
     * for the given {@link gps.optimization.wrapper.Optimizable}.
     * 
     * @param pToOpt    
     * 			the Optimizable the algorithm is to be applied on
     * @param pInitSolution	
     * 			the initial solution for the algorithm
     * @param pInitTemp  
     * 			the initial temperature
     * @param pCoolingSchedule
     * 			the cooling schedule that regulates the temperature
     */
    public SimulatedAnnealing(final Optimizable<T> pToOpt,
            final FlatObjects pInitSolution, final double pInitTemp,
            final ICoolingSchedule pCoolingSchedule) {
        super(pToOpt, pInitSolution);
        initialTemperature = pInitTemp;
        temperature = pInitTemp;
        coolingSchedule = pCoolingSchedule;
    }

    /**
     * Initializes a new instance of the SimulatedAnnealing algorithm 
     * for the given {@link gps.optimization.wrapper.Optimizable}. 
     * Does not need an initial temperature. 
     * 
     * @param pToOpt    
     * 			the Optimizable the algorithm is to be applied on
     * @param pInitSolution	
     * 			the initial solution for the algorithm
     * @param pCoolingSchedule
     * 			the cooling schedule that regulates the temperature
     */
    public SimulatedAnnealing(final Optimizable<T> pToOpt,
            final FlatObjects pInitSolution,
            final ICoolingSchedule pCoolingSchedule) {
        super(pToOpt, pInitSolution);
        temperature = DEFAULT_INITIAL_TEMPERATURE;
        coolingSchedule = pCoolingSchedule;
    }

    /**
     * Gets the current temperature
     * 
     * @return
     * 		the current temperature
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Sets the temperature
     * 
     * @param temperature
     * 		the new temperature
     */
    public void setTemperature(double pTemp) {
        temperature = pTemp;
    }

    /**
     * Gets the CoolingSchedule
     * 
     * @return
     * 		the current CoolingSchedule
     */
    public ICoolingSchedule getCoolingSchedule() {
        return coolingSchedule;
    }

    /**
     * Sets the CoolingSchedule
     * 
     * @param pSched
     * 		the new CoolingSchedule
     */
    public void setCoolingSchedule(ICoolingSchedule pSched) {
        coolingSchedule = pSched;
    }

    /**
     * Performs one step according to the SimulatedAnnealing algorithm. Creates
     * a random variation of the current solution and accepts it as the new current
     * solution if it is better. With a sinking probability worse solutions 
     * are also accepted. The probability is directly connected to the temperature, 
     * which is decreased after every step. The decrease is regulated by a chosen 
     * {@link gps.optimization.algorithm.simulatedAnnealing.schedule.ICoolingSchedule}. 
     */
    @Override
    protected void optimizeOneStep() {
        solutionObject.setValues(currentSolution);
        sop.randomVariation(solutionObject, 1);
        double newEval = eval();
        double diff = newEval - currentBestEval;
        if (diff > 0 || Math.random() < Math.exp((diff / temperature))) {
            currentSolution = solutionObject.getObjects();
            currentBestEval = newEval;
        }
        temperature = coolingSchedule.decreaseTemperature(temperature);

    }

    @Override
    protected void init() {
        solutionObject.setValues(currentSolution);
        currentBestEval = eval();
    }

    @Override
    public String getName() {
        return "Simulated Annealing";
    }

    @Override
    public boolean isApplicable(ResultEnum type) {
        return type.equals(ResultEnum.MAXIMIZED)
                || type.equals(ResultEnum.MINIMIZED);
    }

    @Override
    public String toString() {
        return "<html>Initial temperature: " + initialTemperature + "<br>"
                + coolingSchedule.toString() + "</html>";
    }

}
