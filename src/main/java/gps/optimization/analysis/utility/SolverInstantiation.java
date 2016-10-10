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
package gps.optimization.analysis.utility;

import java.util.ArrayList;
import java.util.List;

import gps.optimization.algorithm.AbstractOptimizer;
import gps.optimization.algorithm.geneticAlgorithm.GeneticAlgorithm;
import gps.optimization.algorithm.geneticAlgorithm.SelectionStrategy;
import gps.optimization.algorithm.greatDeluge.GreatDeluge;
import gps.optimization.algorithm.hillClimbing.HillClimbing;
import gps.optimization.algorithm.particleSwarmOpt.ParticleSwarm;
import gps.optimization.algorithm.simulatedAnnealing.SimulatedAnnealing;
import gps.optimization.algorithm.simulatedAnnealing.schedule.ExponentialSchedule;
import gps.optimization.algorithm.simulatedAnnealing.schedule.LinearSchedule;
import gps.optimization.algorithm.simulatedAnnealing.schedule.QuadraticSchedule;
import gps.optimization.algorithm.thresholdAccepting.ThresholdAccepting;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.wrapper.Optimizable;

/**
 * Utility class that instantiates all eligible solvers with parameters fitting
 * to the objective function of the given
 * {@link gps.optimization.wrapper.Optimizable}.
 * 
 * @author mburri
 *
 * @param <T>
 *            the type of the original problem class
 */
public class SolverInstantiation<T> {

    /**
     * The {@link gps.optimization.wrapper.Optimizable} that is to be optimized
     */
    private final Optimizable<T> opt;

    /**
     * The start params that every solver is to use
     */
    private final FlatObjects startParams;

    /**
     * Creates a new instance of the SolverInstantiation for the given
     * Optimizable.
     * 
     * @param pOpt
     *            the Optimizable the solvers are to be retrieved for
     */
    public SolverInstantiation(final Optimizable<T> pOpt,
            final FlatObjects pStartParams) {
        opt = pOpt;
        startParams = pStartParams;
    }

    /**
     * Returns a list of eligible solvers (for the given problem) with set
     * parameters.
     * 
     * @param stepWidth
     *            the step width the solvers are to use
     * @param objScale
     *            the objective function scale that is to be considered when
     *            choosing the solver parameters
     * @param estimatedSteps
     *            the estimated amount of steps every solver is allowed to take
     *            during the whole optimization process
     * @param maximize
     *            indicates if the problem is to be maximized or minimized
     * 
     * @return a list of eligible solvers
     */
    public List<AbstractOptimizer<T>> getSolversWithParameters(
            final int stepWidth, final double objScale,
            final int estimatedSteps, final byte maximize) {
        List<AbstractOptimizer<T>> solvers = new ArrayList<AbstractOptimizer<T>>();

        // Threshold Accepting
        solvers.addAll(this.getSolversWithParametersTA(stepWidth, objScale,
                estimatedSteps, maximize));

        // Simulated Annealing
        solvers.addAll(this.getSolversWithParametersSA(stepWidth, objScale,
                estimatedSteps, maximize));

        // Great Deluge
        solvers.addAll(this.getSolversWithParametersGD(stepWidth, objScale,
                estimatedSteps, maximize));

        // Hill Climbing
        solvers.addAll(this.getSolversWithParametersHC(stepWidth, objScale,
                estimatedSteps, maximize));

        // Particle Swarm Optimization
        solvers.addAll(this.getSolversWithParametersPS(stepWidth, objScale,
                estimatedSteps, maximize));
        // Genetic Algorithm
        solvers.addAll(this.getSolversWithParametersGA(stepWidth, objScale,
                estimatedSteps, maximize));

        return solvers;
    }

    /**
     * Returns a few instances of the
     * {@link gps.optimization.algorithm.thresholdAccepting.ThresholdAccepting}
     * solver with parameters fitting the given arguments.
     * 
     * @param stepWidth
     *            the step width the solvers are to use
     * @param objScale
     *            the objective function scale that is to be considered when
     *            choosing the solver parameters
     * @param estimatedSteps
     *            the estimated amount of steps every solver is allowed to take
     *            during the whole optimization process
     * @param maximize
     *            indicates if the problem is to be maximized or minimized
     * 
     * @return a list of ThresholdAccepting instances
     */
    public List<ThresholdAccepting<T>> getSolversWithParametersTA(
            final int stepWidth, final double objScale,
            final int estimatedSteps, final byte maximize) {
        List<ThresholdAccepting<T>> solvers = new ArrayList<ThresholdAccepting<T>>();

        // TA with initial threshold = objScale and decrease factor = 0.9996
        ThresholdAccepting<T> tA = new ThresholdAccepting<T>(opt, startParams,
                objScale, 0.9996);
        tA.setStepWidth(stepWidth);
        solvers.add(tA);

        // TA with initial threshold = objScale * 2 and decrease factor =  0.9995
        ThresholdAccepting<T> tA1 = new ThresholdAccepting<T>(opt, startParams,
                objScale * 2, 0.9995);
        tA1.setStepWidth(stepWidth);
        solvers.add(tA1);

        // TA with initial threshold = objScale / 2 and decrease factor =  0.9997
        ThresholdAccepting<T> tA2 = new ThresholdAccepting<T>(opt, startParams,
                objScale / 2, 0.9997);
        tA2.setStepWidth(stepWidth);
        solvers.add(tA2);

        // TA with initial threshold = objScale and decrease factor = 0.995 -> fast decrease
        ThresholdAccepting<T> tA3 = new ThresholdAccepting<T>(opt, startParams,
                objScale, 0.995);
        tA3.setStepWidth(stepWidth);
        solvers.add(tA3);

        // TA with initial threshold = objScale * 2 and decrease factor = 0.995 -> fast decrease
        ThresholdAccepting<T> tA4 = new ThresholdAccepting<T>(opt, startParams,
                objScale * 2, 0.995);
        tA4.setStepWidth(stepWidth);
        solvers.add(tA4);

        return solvers;
    }

    /**
     * Returns a few instances of the
     * {@link gps.optimization.algorithm.simulatedAnnealing.SimulatedAnnealing}
     * solver with parameters fitting the given arguments.
     * 
     * @param stepWidth
     *            the step width the solvers are to use
     * @param objScale
     *            the objective function scale that is to be considered when
     *            choosing the solver parameters
     * @param estimatedSteps
     *            the estimated amount of steps every solver is allowed to take
     *            during the whole optimization process
     * @param maximize
     *            indicates if the problem is to be maximized or minimized
     * 
     * @return a list of SimulatedAnnealing instances
     */
    public List<SimulatedAnnealing<T>> getSolversWithParametersSA(
            final int stepWidth, final double objScale,
            final int estimatedSteps, final byte maximize) {
        List<SimulatedAnnealing<T>> solvers = new ArrayList<SimulatedAnnealing<T>>();

        // with linear schedule
        // initial temperature = estimatedSteps, decrease value = 1 -> slow
        // decrease
        LinearSchedule ls = new LinearSchedule(1);
        SimulatedAnnealing<T> sA1 = new SimulatedAnnealing<T>(opt, startParams,
                estimatedSteps, ls);
        sA1.setStepWidth(stepWidth);
        solvers.add(sA1);

        // with exponential schedule
        // initial temperature = estimatedSteps, decrease value = 0.9996 -> slow decrease
        ExponentialSchedule es = new ExponentialSchedule(0.9996);
        SimulatedAnnealing<T> sA2 = new SimulatedAnnealing<T>(opt, startParams,
                estimatedSteps, es);
        sA2.setStepWidth(stepWidth);
        solvers.add(sA2);

        // with exponential schedule
        // initial temperature = estimatedSteps, decrease value = 0.995 -> fast decrease
        ExponentialSchedule es1 = new ExponentialSchedule(0.999);
        SimulatedAnnealing<T> sA3 = new SimulatedAnnealing<T>(opt, startParams,
                objScale, es1);
        sA3.setStepWidth(stepWidth);
        solvers.add(sA3);

        // with quadratic schedule
        // temperature = estimatedSteps, decrease factor = 0.0000002 -> first slow, then fast, then slow again
        QuadraticSchedule qs = new QuadraticSchedule(estimatedSteps, 0.0000002);
        SimulatedAnnealing<T> sA4 = new SimulatedAnnealing<T>(opt, startParams,
                estimatedSteps, qs);
        sA4.setStepWidth(stepWidth);
        solvers.add(sA4);

        return solvers;
    }

    /**
     * Returns a few instances of the
     * {@link gps.optimization.algorithm.greatDeluge.GreatDeluge} solver with
     * parameters fitting the given arguments.
     * 
     * @param stepWidth
     *            the step width the solvers are to use
     * @param objScale
     *            the objective function scale that is to be considered when
     *            choosing the solver parameters
     * @param estimatedSteps
     *            the estimated amount of steps every solver is allowed to take
     *            during the whole optimization process
     * @param maximize
     *            indicates if the problem is to be maximized or minimized
     * 
     * @return a list of GreatDeluge instances
     */
    public List<GreatDeluge<T>> getSolversWithParametersGD(final int stepWidth,
            final double objScale, final int estimatedSteps,
            final byte maximize) {
        List<GreatDeluge<T>> solvers = new ArrayList<GreatDeluge<T>>();

        // rain = objScale * 2 / estimatedSteps -> faster increase of water level
        GreatDeluge<T> gD;
        if (maximize == 1) {
            gD = new GreatDeluge<T>(opt, startParams, 0,
                    (objScale * 2) / estimatedSteps);
        } else {
            gD = new GreatDeluge<T>(opt, startParams, -objScale * 2,
                    (objScale * 2) / estimatedSteps);
        }
        gD.setStepWidth(stepWidth);
        solvers.add(gD);

        // rain = objScale * 1.5 / estimatedSteps -> slow increase of water level
        GreatDeluge<T> gD1;
        if (maximize == 1) {
            gD1 = new GreatDeluge<T>(opt, startParams, 0,
                    (objScale * 1.5) / estimatedSteps);
        } else {
            gD1 = new GreatDeluge<T>(opt, startParams, -objScale * 2,
                    (objScale * 1.5) / estimatedSteps);
        }
        gD1.setStepWidth(stepWidth);
        solvers.add(gD1);

        // rain = objScale * 4 / estimatedSteps -> very fast increase of water level
        GreatDeluge<T> gD2;
        if (maximize == 1) {
            gD2 = new GreatDeluge<T>(opt, startParams, 0,
                    (objScale * 4) / estimatedSteps);
        } else {
            gD2 = new GreatDeluge<T>(opt, startParams, -objScale * 2,
                    (objScale * 4) / estimatedSteps);
        }
        gD2.setStepWidth(stepWidth);
        solvers.add(gD2);

        return solvers;
    }

    /**
     * Returns a few instances of the
     * {@link gps.optimization.algorithm.hillClimbing.HillClimbing} solver with
     * parameters fitting the given arguments.
     * 
     * @param stepWidth
     *            the step width the solvers are to use
     * @param objScale
     *            the objective function scale that is to be considered when
     *            choosing the solver parameters
     * @param estimatedSteps
     *            the estimated amount of steps every solver is allowed to take
     *            during the whole optimization process
     * @param maximize
     *            indicates if the problem is to be maximized or minimized
     * 
     * @return a list of HillClimbing instances
     */
    public List<HillClimbing<T>> getSolversWithParametersHC(final int stepWidth,
            final double objScale, final int estimatedSteps,
            final byte maximize) {
        List<HillClimbing<T>> solvers = new ArrayList<HillClimbing<T>>();

        // step width = calculated optimal step width
        HillClimbing<T> hC = new HillClimbing<T>(opt, startParams);
        hC.setStepWidth(stepWidth);
        solvers.add(hC);

        return solvers;
    }

    /**
     * Returns a few instances of the
     * {@link gps.optimization.algorithm.particleSwarm.ParticleSwarm} solver
     * with parameters fitting the given arguments.
     * 
     * @param stepWidth
     *            the step width the solvers are to use
     * @param objScale
     *            the objective function scale that is to be considered when
     *            choosing the solver parameters
     * @param estimatedSteps
     *            the estimated amount of steps every solver is allowed to take
     *            during the whole optimization process
     * @param maximize
     *            indicates if the problem is to be maximized or minimized
     * 
     * @return a list of ParticleSwarm instances
     */
    public List<ParticleSwarm<T>> getSolversWithParametersPS(
            final int stepWidth, final double objScale,
            final int estimatedSteps, final byte maximize) {
        List<ParticleSwarm<T>> solvers = new ArrayList<ParticleSwarm<T>>();

        if (ParticleSwarm.onlyNumbers(startParams)) {
            // 10 particles
            ParticleSwarm<T> ps = new ParticleSwarm<T>(opt, startParams, 10);
            solvers.add(ps);

            // 500 particles
            ParticleSwarm<T> ps1 = new ParticleSwarm<T>(opt, startParams, 300);
            solvers.add(ps1);
        }

        return solvers;
    }

    /**
     * Returns a few instances of the
     * {@link gps.optimization.algorithm.geneticAlgorithm.GeneticAlgorithm}
     * solver with parameters fitting the given arguments.
     * 
     * @param stepWidth
     *            the step width the solvers are to use
     * @param objScale
     *            the objective function scale that is to be considered when
     *            choosing the solver parameters
     * @param estimatedSteps
     *            the estimated amount of steps every solver is allowed to take
     *            during the whole optimization process
     * @param maximize
     *            indicates if the problem is to be maximized or minimized
     * 
     * @return a list of GeneticAlgorithm instances
     */
    public List<GeneticAlgorithm<T>> getSolversWithParametersGA(
            final int stepWidth, final double objScale,
            final int estimatedSteps, final byte maximize) {
        List<GeneticAlgorithm<T>> solvers = new ArrayList<>();
        GeneticAlgorithm<T> ga1 = new GeneticAlgorithm<T>(opt, startParams, 5,
                10, SelectionStrategy.TRUNCATION, stepWidth);
        solvers.add(ga1);
        GeneticAlgorithm<T> ga2 = new GeneticAlgorithm<T>(opt, startParams, 10,
                10, SelectionStrategy.TRUNCATION, stepWidth);
        solvers.add(ga2);

        GeneticAlgorithm<T> ga4 = new GeneticAlgorithm<T>(opt, startParams, 5,
                50, SelectionStrategy.TRUNCATION, stepWidth);
        solvers.add(ga4);

        return solvers;
    }

}
