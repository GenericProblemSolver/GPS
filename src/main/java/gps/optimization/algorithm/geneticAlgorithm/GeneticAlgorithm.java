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
package gps.optimization.algorithm.geneticAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import gps.ResultEnum;
import gps.optimization.algorithm.AbstractOptimizer;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.wrapper.Optimizable;

/**
 * Implements the Genetic Algorithm. This algorithm slowly "evolves" a
 * population of solutions by selecting solutions to cross into new solutions
 * and mutating existing ones, according to different possible strategies, to
 * generate better solutions.
 *
 * @author Steffen
 *
 * @param <T>
 *            the type of the original problem class
 */
public class GeneticAlgorithm<T> extends AbstractOptimizer<T> {

    /**
     * Default mutation chance
     */
    private static final int DEFAULTMUTATIONCHANCE = 10;

    /**
     * Default mutationStep
     */
    private static final int DEFAULTMUTATIONSTEP = 1;

    /**
     * Default maximum size of the population
     */
    private static final int DEFAULTPOPULATIONSIZE = 20;

    /**
     * Default selection strategy
     */
    private static final SelectionStrategy DEFAULTSELECTIONSTRATEGY = SelectionStrategy.TRUNCATION;
    /**
     * Chance of new solutions mutating randomly. In percentage-points.
     */
    private final int mutationChance;

    /**
     * Maximum distance a solution can be mutated by
     */
    private int mutationStep;

    /**
     * The given population, each solution is represented by the its Object[]
     * and the fitness
     */
    private List<SolutionFitnessTuple> population;
    /**
     * Maximum size of the population
     */
    private final int populationSize;

    /**
     * Random instance for randomness
     */
    private Random random;

    /**
     * The selection strategy determining which solutions are used to build new
     * solutions
     */
    private final SelectionStrategy selectionStrategy;

    /**
     * Counts how many solutions have been generated
     */
    private int solutionCounter;

    /**
     * Initializes a new instance of the Genetic Algorithm for the given
     * {@link gps.optimization.wrapper.Optimizable} with default parameters.
     *
     * @param pToOpt
     *            the Optimizable the algorithm is to be applied on
     * @param pInitSolution
     *            the initial solution of the optimization process
     */
    public GeneticAlgorithm(final Optimizable<T> pToOpt,
            final FlatObjects pInitSolution) {
        this(pToOpt, pInitSolution, GeneticAlgorithm.DEFAULTPOPULATIONSIZE,
                GeneticAlgorithm.DEFAULTMUTATIONCHANCE,
                GeneticAlgorithm.DEFAULTSELECTIONSTRATEGY,
                GeneticAlgorithm.DEFAULTMUTATIONSTEP);
    }

    /**
     * Initializes a new instance of the Genetic Algorithm for the given
     * {@link gps.optimization.wrapper.Optimizable}.
     *
     * @param pToOpt
     *            the Optimizable the algorithm is to be applied on
     * @param pInitSolution
     *            the initial solution of the optimization process
     * @param pPopulationSize
     *            the maximum size of the population param pMutationChance the
     *            percentage chance of random mutations occuring
     * @param pMutationChance 
     *            chance in percent of mutation occuring
     * @param pSelectionStrategy
     *            the selection strategy to be used
     * @param pMutationStep
     *            how much a solution is changed when mutating
     */
    public GeneticAlgorithm(final Optimizable<T> pToOpt,
            final FlatObjects pInitSolution, final int pPopulationSize,
            final int pMutationChance,
            final SelectionStrategy pSelectionStrategy,
            final int pMutationStep) {
        super(pToOpt, pInitSolution);
        populationSize = pPopulationSize;
        mutationChance = pMutationChance;
        selectionStrategy = pSelectionStrategy;
        mutationStep = pMutationStep;
        solutionCounter = 0;
        random = new Random();
        population = new ArrayList<>();
    }

    /**
     * Adds the currently loaded Solution to the population
     */
    private void addToPopulation() {
        solutionCounter++;
        double res = eval();
        Object[] solution = solutionObject.getObjects();
        if (res > currentBestEval) {
            currentSolution = solution;
            currentBestEval = res;
        }
        population.add(new SolutionFitnessTuple(solution, res));
    }

    /**
     * Crosses the given two solutions (represented by Object-arrays) over into
     * a new solution. Practically this means randomly selecting the value of
     * the first or the second Solution.
     *
     * @param pSolution1
     *            Object-array holding the values of the first solution
     * @param pSolution2
     *            Object-array holding the values of the second solution
     * @return the Object-array created by crossover of the given two
     *         Object-arrays
     */
    private Object[] crossoverSolutions(final Object[] pSolution1,
            final Object[] pSolution2) {
        if (pSolution1.length != pSolution2.length) {
            throw new IllegalArgumentException(
                    "Both solution need to have the same number of FlatWrappers.");
        }
        Object[] result = new Object[pSolution1.length];
        for (int x = 0; x < result.length; x++) {
            result[x] = random.nextBoolean() ? pSolution1[x] : pSolution2[x];
        }
        return result;
    }

    @Override
    public String getName() {
        return "Genetic Algorithm";
    }

    @Override
    protected void init() {
        solutionObject.setValues(currentSolution);
        currentBestEval = eval();
        addToPopulation();
        for (int x = 1; x < populationSize; x++) {
            sop.randomVariation(solutionObject);
            addToPopulation();
        }
    }

    @Override
    public boolean isApplicable(final ResultEnum type) {
        return type.equals(ResultEnum.MAXIMIZED)
                || type.equals(ResultEnum.MINIMIZED);
    }

    /**
     * Performs one step according to the Genetic Algorithm. Selects solutions
     * to crossover and mutate, and creates a new population. Any new solution
     * better than the current best is stored. 
     */
    @Override
    protected void optimizeOneStep() {
        Collections.sort(population);
        List<Object[]> selectedSolutions = selectionStrategy.select(population,
                populationSize / 2);
        population = new ArrayList<>(
                population.subList(0, population.size() * 3 / 5));
        for (int x = 0; x < selectedSolutions.size() - 1; x += 2) {
            Object[] solution1 = population.get(x).getSolution();
            Object[] solution2 = population.get(x + 1).getSolution();
            Object[] crossover = crossoverSolutions(solution1, solution2);
            solutionObject.setValues(crossover);
            if (random.nextInt(100) < mutationChance) {
                sop.randomRandomVariation(solutionObject, mutationStep);
            }
            addToPopulation();
        }
        for (int x = 0; x < population.size()
                && population.size() < populationSize; x++) {
            solutionObject.setValues(population.get(x).getSolution());
            if (random.nextInt(100) < mutationChance) {
                sop.randomRandomVariation(solutionObject, mutationStep);
            }
            addToPopulation();
        }
    }

    @Override
    public String toString() {
        return "<html>Population size " + populationSize
                + "<br>Mutation chance: " + mutationChance + "</html>";
    }

}
