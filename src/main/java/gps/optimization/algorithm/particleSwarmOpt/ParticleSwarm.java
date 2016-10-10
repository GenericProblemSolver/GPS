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
package gps.optimization.algorithm.particleSwarmOpt;

import java.util.ArrayList;
import java.util.List;

import gps.ResultEnum;
import gps.optimization.algorithm.AbstractOptimizer;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.FlatWrapper;
import gps.optimization.wrapper.Optimizable;

/**
 * Implements the particle swarm optimization algorithm.
 * 
 * @see <a href="http://www.swarmintelligence.org/tutorials.php">
 * 		http://www.swarmintelligence.org/tutorials.php</a>
 * 
 * @author mburri
 *
 * @param <T>  
 * 			the type of the original problem class
 */
public class ParticleSwarm<T> extends AbstractOptimizer<T> {

    /**
     * the learn factor that regulates how much the 
     * personal best is taken into account
     */
    private final static double DEFAULT_LEARNING_FACTOR1 = 1;

    /**
     * the learn factor that regulates how much the 
     * global best is taken into account
     */
    private final static double DEFAULT_LEARNING_FACTOR2 = 1;

    /**
     * the default number of particles in the swarm
     */
    private final static int DEFAULT_NUMBER_OF_PARTICLES = 10;

    /**
     * used to regulate the velocity
     */
    private double learnFactor1;

    /**
     * used to regulate the velocity
     */
    private double learnFactor2;

    /**
     * the number of particles in the swarm
     */
    private final int numberOfParticles;

    /**
     * the particles in the swarm
     */
    private List<Particle> particles;

    /**
     * Initializes a new particle swarm for the given {@link Optimizable}
     * 
     * @param pToOpt	the Optimizable the algorithm is to be applied on
     * @param pInitSolution	the initial solution for this algorithm
     * @param noOfParticles	the number of particles in the swarm
     * @param lf1	first learn factor
     * @param lf2	second learn factor
     */
    public ParticleSwarm(final Optimizable<T> pToOpt,
            final FlatObjects pInitSolution, final int noOfParticles,
            final double lf1, final double lf2) {
        super(pToOpt, pInitSolution);
        numberOfParticles = noOfParticles;
        learnFactor1 = lf1;
        learnFactor2 = lf2;
    }

    /**
     * Initializes a new particle swarm for the given {@link Optimizable}
     * 
     * @param pToOpt	the Optimizable the algorithm is to be applied on
     * @param pInitSolution	the initial solution for this algorithm
     * @param noOfParticles	the number of particles in the swarm
     */
    public ParticleSwarm(final Optimizable<T> pToOpt,
            final FlatObjects pInitSolution, final int noOfParticles) {
        super(pToOpt, pInitSolution);
        numberOfParticles = noOfParticles;
        learnFactor1 = DEFAULT_LEARNING_FACTOR1;
        learnFactor2 = DEFAULT_LEARNING_FACTOR2;
    }

    /**
     * Initializes a new particle swarm for the given {@link Optimizable}
     * 
     * @param pToOpt	the Optimizable the algorithm is to be applied on
     * @param pInitSolution	the initial solution for this algorithm
     */
    public ParticleSwarm(final Optimizable<T> pToOpt,
            final FlatObjects pInitSolution) {
        super(pToOpt, pInitSolution);
        numberOfParticles = DEFAULT_NUMBER_OF_PARTICLES;
        learnFactor1 = DEFAULT_LEARNING_FACTOR1;
        learnFactor2 = DEFAULT_LEARNING_FACTOR2;
    }

    @Override
    protected void optimizeOneStep() {
        for (Particle p : particles) {
            solutionObject.setValues(p.getCurrentPosition());
            double valueOfCurrentPosition = eval();

            // 	if the current position is better than the personal best,
            //	set personal best accordingly
            if (valueOfCurrentPosition > p.getValueOfPersonalBest()) {
                p.setValueOfPersonalBest(valueOfCurrentPosition);
                p.setPersonalBest(p.getCurrentPosition());

                //  if the new personal best is better than the global best,
                // 	set global best accordingly
                if (valueOfCurrentPosition > currentBestEval) {
                    currentBestEval = valueOfCurrentPosition;
                    currentSolution = solutionObject.getObjects();
                }
            }
        }

        //	update the position of every particle
        solutionObject.setValues(currentSolution);
        for (Particle p : particles) {
            p.updatePosition(solutionObject);
        }

    }

    @Override
    protected void init() {
        // initializes the swarm:
        // particles are initialized (with random initial positions)
        // also initializes the global best
        if (particles != null) {
            // if the list is not empty, 
            // there is no need to initialize the swarm
            return;
        }
        particles = new ArrayList<Particle>();

        Object[] initialValues = solutionObject.getObjects();
        currentBestEval = -Double.MAX_VALUE;
        for (int i = 0; i < numberOfParticles; i++) {
            solutionObject.setValues(initialValues);
            sop.randomVariation(solutionObject, 1);
            Particle p = new Particle(initialValues, learnFactor1,
                    learnFactor2);
            double value = eval();
            p.setValueOfPersonalBest(value);
            particles.add(p);
            if (value > currentBestEval) {
                // set new best 'global' solution
                currentSolution = solutionObject.getObjects();
                currentBestEval = value;
            }
        }
    }

    /**
     * To calculate the velocity of the particles, we need to calculate 
     * the difference between solution vectors. Generically, this is 
     * only possible if we restrict ourselves to vectors that solely include 
     * numbers.
     * 
     * @return
     * 			{@code true} if the solution vector solely consists of numbers, 
     * 			{@code false} otherwise
     */
    public static boolean onlyNumbers(final FlatObjects fO) {
        for (FlatWrapper fW : fO.getFlatWrappers()) {
            Object o = fW.get();
            if (!(o instanceof Integer || o instanceof Byte
                    || o instanceof Short || o instanceof Long
                    || o instanceof Float || o instanceof Double)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "Particle Swarm Optimization";
    }

    @Override
    public boolean isApplicable(ResultEnum type) {
        return type.equals(ResultEnum.MAXIMIZED)
                || type.equals(ResultEnum.MINIMIZED);
    }

    @Override
    public String toString() {
        return "Number of particles: " + numberOfParticles;
    }
}
