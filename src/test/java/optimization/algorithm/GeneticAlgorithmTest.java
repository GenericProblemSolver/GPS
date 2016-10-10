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
package optimization.algorithm;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gps.GPS;
import gps.optimization.OptimizationReturn;
import gps.optimization.algorithm.geneticAlgorithm.GeneticAlgorithm;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.Flattener;
import gps.optimization.wrapper.Optimizable;
import optimization.knapsack.KnapsackProblem;

/**
 * Tests the
 * {@link gps.optimization.algorithm.geneticAlgorithm.GeneticAlgorithm}
 * algorithm.
 *
 * @author Sotisi
 *
 */
public class GeneticAlgorithmTest {
    /**
     * the instance of the solver
     */
    private GeneticAlgorithm<KnapsackProblem> gD;

    /**
     * Instance of the KnapsackProblem to be solved
     */
    private KnapsackProblem kP;

    /**
     * the wrapped problem that will be used for the test
     */
    private Optimizable<KnapsackProblem> opt;

    /**
     * Initializes the Genetic Algorithm Solver and the ClusteringProblem that
     * is to be optimized and used for the test
     */
    @Before
    public void init() {
        kP = new KnapsackProblem();

        Flattener f = new Flattener();

        opt = new Optimizable<>(GPS.wrap(kP));
        FlatObjects fO = f.flattenAndWrap(opt.getDefaultParams());
        gD = new GeneticAlgorithm<KnapsackProblem>(opt, fO);
    }

    /**
     * Tests the
     * {@link gps.optimization.algorithm.geneticAlgorithm.GeneticAlgorithm#opimizeOneStep}
     * -function if the problem is to be maximized.
     */
    @Test
    public void maximizeOneStep() {
        gD.setMaxSteps(5000);
        Optional<OptimizationReturn> result = gD.maximize();
        Assert.assertTrue(result.get().getBestSolutionEval() * 2.5 > kP
                .referenceSolution());
    }

    /**
     * Tests the
     * {@link gps.optimizoation.algorithm.geneticAlgorithm.GeneticAlgorithm#opimizeOneStep}
     * -function if the problem is to be minimized.
     */
    @Test
    public void minimizeOneStep() {
        gD.setMaxSteps(1);
        Optional<OptimizationReturn> result = gD.minimize();
        Assert.assertTrue(result.get().getBestSolutionEval() == 0);
    }
}
