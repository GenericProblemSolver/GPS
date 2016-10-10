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

import java.util.Arrays;
import java.util.Random;

import gps.optimization.flattening.FlatObjects;

/**
 * Implements a representation of a particle used by the
 * particle swarm optimization algorithm.<br>
 * MaxVelocity is set to globalBest by default. 
 * 
 * See {@link gps.optimization.algorithm.particleSwarmOpt.ParticleSwarm}
 * 
 * @author mburri
 *
 * @param <T>  
 * 			the type of the original problem class
 */
public class Particle {

    /**
     * Random number generator used to generate the random numbers
     * that are needed for calculating the velocity of the particle
     */
    private final Random rand = new Random();

    /**
     * The current position/solution of this particle
     */
    private Object[] currentPosition;

    /**
     * The personal best solution found by this particle
     */
    private Object[] personalBest;

    /**
     * Value of the best params found by this particle
     */
    private double valueOfPersonalBest;

    /**
     * Regulates how much the personal best influences the velocity
     */
    private double learnFactor1;

    /**
     * Regulates how much the global best influences the velocity
     */
    private double learnFactor2;

    /**
     * Regulates by how much the position is changed in every step
     */
    private Object[] velocity;

    /**
     * Creates a new Particle with the given parameters
     * 
     * @param pOpt  problem object that is to be optimized
     * @param initPosition   initial solution of the particle
     * @param lF1   the learn factor that regulates how much the personal best is regarded
     * @param lF2   the learn factor that regulates how much the global best is regarded
     */
    public Particle(final Object[] initPosition, final double lF1,
            final double lF2) {
        currentPosition = Arrays.copyOf(initPosition, initPosition.length);
        this.setPersonalBest(currentPosition);
        learnFactor1 = lF1;
        learnFactor2 = lF2;

        velocity = new Object[currentPosition.length];
        initializeVelocity();
    }

    /**
     * Initializes the velocity with 0
     */
    public void initializeVelocity() {
        for (int i = 0; i < currentPosition.length; i++) {
            Object currentPositionValue = currentPosition[i];
            if (currentPositionValue instanceof Integer) {
                velocity[i] = (int) 0;
            } else if (currentPositionValue instanceof Double) {
                velocity[i] = (double) 0;
            } else if (currentPositionValue instanceof Byte) {
                velocity[i] = (byte) 0;
            } else if (currentPositionValue instanceof Float) {
                velocity[i] = (float) 0;
            } else if (currentPositionValue instanceof Long) {
                velocity[i] = (long) 0;
            } else if (currentPositionValue instanceof Short) {
                velocity[i] = (short) 0;
            }
        }
    }

    /**
     * Returns the value of the best solution that was found 
     * by this particle (up until this point) 
     * 
     * @return  the value of the personal best solution
     */
    public double getValueOfPersonalBest() {
        return valueOfPersonalBest;
    }

    /**
     * Sets the personal best according to the given Objecŧ[].
     * 
     * @param pNewBest	the new personal best
     */
    public void setPersonalBest(final Object[] pNewBest) {
        personalBest = pNewBest;
    }

    /**
     * Sets the value of the best solution that was found 
     * by this particle (up until this point) 
     * 
     * @param pValue    the new value of the personal best solution
     */
    public void setValueOfPersonalBest(final double pValue) {
        valueOfPersonalBest = pValue;
    }

    /**
     * Gets the FlatObjects representing the current position of 
     * this particle.
     * 
     * @return
     * 			the current position of this particle
     */
    public Object[] getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Calculates the velocity for the particle and 
     * updates its position accordingly 
     * 
     * @param globalBest    best solution found by the whole swarm (up until this point)
     */
    protected void updatePosition(final FlatObjects globalBest) {
        for (int i = 0; i < velocity.length; i++) {
            updatePositionForIndex(globalBest, i);
        }
    }

    /**
     * Calculates the velocity for the element of the given index of the current
     * position and updates the element accordingly 
     * 
     * velocity = velocity + learnFactor1 * {0 || 1} * (personalBest - currentPosition) 
     * 				+ learnFactor2 * {0 || 1} * (globalBest - currentPosition)
     * 
     * if velocity exceeds maxVelocity/maxNegativeVelocity, set velocity =
     * maxVelocity/maxNegativeVelocity maxVelocity = globalBest (if globalBest
     * positive) maxNegativeVelocity = globalBest (if negative)
     * 
     * position = position + velocity
     *
     * 
     * @param globalBest
     *            best solution found by the whole swarm (up until this point)
     * @param ind
     *            index of the element of the current position that is to be
     *            updated
     */
    public void updatePositionForIndex(final FlatObjects globalBest,
            final int ind) {
        double firstFactor = rand.nextInt(2) * learnFactor1;
        double secondFactor = rand.nextInt(2) * learnFactor2;

        // get value of current position
        Object currentPositionValue = currentPosition[ind];
        // get value of personal best
        Object personalBestValue = personalBest[ind];
        // get value of global best
        Object globalBestValue = globalBest.getFlatWrappers().get(ind).get();

        if (currentPositionValue instanceof Integer) {
            int currentPositionInt = (int) currentPositionValue;
            int personalBestInt = (int) personalBestValue;
            int globalBestInt = (int) globalBestValue;
            /* Update velocity */
            velocity[ind] = (int) ((int) velocity[ind]
                    + firstFactor * (personalBestInt - currentPositionInt)
                    + secondFactor * (globalBestInt - currentPositionInt));
            /* Check if velocity exceeds maxVelocity/maxNegativeVelociy */
            if ((globalBestInt > 0 && (int) velocity[ind] > globalBestInt)
                    || (globalBestInt < 0
                            && (int) velocity[ind] < globalBestInt)) {
                velocity[ind] = (int) globalBestInt;
            }
            /* Set new position */
            currentPosition[ind] = currentPositionInt + (int) velocity[ind];
        } else if (currentPositionValue instanceof Double) {
            double currentPositionDouble = (double) currentPositionValue;
            double personalBestDouble = (double) personalBestValue;
            double globalBestDouble = (double) globalBestValue;
            /* Update velocity */
            velocity[ind] = (double) ((double) velocity[ind]
                    + firstFactor * (personalBestDouble - currentPositionDouble)
                    + secondFactor
                            * (globalBestDouble - currentPositionDouble));
            /* Check if velocity exceeds maxVelocity/maxNegativeVelociy */
            if ((globalBestDouble > 0
                    && (double) velocity[ind] > globalBestDouble)
                    || (globalBestDouble < 0
                            && (double) velocity[ind] < globalBestDouble)) {
                velocity[ind] = globalBestDouble;
            }
            /* Set new position */
            currentPosition[ind] = currentPositionDouble
                    + (double) velocity[ind];
        } else if (currentPositionValue instanceof Byte) {
            byte currentPositionByte = (byte) currentPositionValue;
            byte personalBestByte = (byte) personalBestValue;
            byte globalBestByte = (byte) globalBestValue;
            /* Update velocity */
            velocity[ind] = (byte) ((byte) velocity[ind]
                    + firstFactor * (personalBestByte - currentPositionByte)
                    + secondFactor * (globalBestByte - currentPositionByte));
            /* Check if velocity exceeds maxVelocity/maxNegativeVelociy */
            if ((globalBestByte > 0 && (byte) velocity[ind] > globalBestByte)
                    || (globalBestByte < 0
                            && (byte) velocity[ind] < globalBestByte)) {
                velocity[ind] = globalBestByte;
            }
            /* Set new position */
            currentPosition[ind] = currentPositionByte + (byte) velocity[ind];
        } else if (currentPositionValue instanceof Float) {
            float currentPositionFloat = (float) currentPositionValue;
            float personalBestFloat = (float) personalBestValue;
            float globalBestFloat = (float) globalBestValue;
            /* Update velocity */
            velocity[ind] = (float) ((float) velocity[ind]
                    + firstFactor * (personalBestFloat - currentPositionFloat)
                    + secondFactor * (globalBestFloat - currentPositionFloat));
            /* Check if velocity exceeds maxVelocity/maxNegativeVelociy */
            if ((globalBestFloat > 0 && (float) velocity[ind] > globalBestFloat)
                    || (globalBestFloat < 0
                            && (float) velocity[ind] < globalBestFloat)) {
                velocity[ind] = globalBestFloat;
            }
            /* Set new position */
            currentPosition[ind] = currentPositionFloat + (float) velocity[ind];
        } else if (currentPositionValue instanceof Long) {
            long currentPositionLong = (long) currentPositionValue;
            long personalBestLong = (long) personalBestValue;
            long globalBestLong = (long) globalBestValue;
            /* Update velocity */
            velocity[ind] = (long) ((long) velocity[ind]
                    + firstFactor * (personalBestLong - currentPositionLong)
                    + secondFactor * (globalBestLong - currentPositionLong));
            /* Check if velocity exceeds maxVelocity/maxNegativeVelociy */
            if ((globalBestLong > 0 && (long) velocity[ind] > globalBestLong)
                    || (globalBestLong < 0
                            && (long) velocity[ind] < globalBestLong)) {
                velocity[ind] = globalBestLong;
            }
            /* Set new position */
            currentPosition[ind] = currentPositionLong + (long) velocity[ind];
        } else if (currentPositionValue instanceof Short) {
            short currentPositionShort = (short) currentPositionValue;
            short personalBestShort = (short) personalBestValue;
            short globalBestShort = (short) globalBestValue;
            /* Update velocity */
            velocity[ind] = (short) ((short) velocity[ind]
                    + firstFactor * (personalBestShort - currentPositionShort)
                    + secondFactor * (globalBestShort - currentPositionShort));
            /* Check if velocity exceeds maxVelocity/maxNegativeVelociy */
            if ((globalBestShort > 0 && (short) velocity[ind] > globalBestShort)
                    || (globalBestShort < 0
                            && (short) velocity[ind] < globalBestShort)) {
                velocity[ind] = globalBestShort;
            }
            /* Set new position */
            currentPosition[ind] = currentPositionShort + (short) velocity[ind];
        }
    }

}