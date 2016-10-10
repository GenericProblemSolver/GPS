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
package optimization.benchmarks;

/**
 * Represents a set of parameters the {@link gps.optimization.analysis.Analyzer} 
 * is expected to find. 
 * 
 * @author mburri
 *
 */
public class AnalyzerBenchmark {

    /**
     * the minimal objective function scale that is acceptable
     */
    private final int expectedStepWidthMin;

    /**
     * the maximal objective function scale that is acceptable
     */
    private final double expectedObjectiveFunctionScaleMin;

    /**
     * the minimal step width that is acceptable
     */
    private final int expectedStepWidthMax;

    /**
     * the maximal step width that is acceptable
     */
    private final double expectedObjectiveFunctionScaleMax;

    /**
     * Creates a new AnalyzerBenchmark for the given values.
     * 
     * @param pExpectedStepWidthMin
     * @param pExpectedStepWidthMax
     * @param pExpectedObjectiveFunctionScaleMin
     * @param pExpectedObjectiveFunctionScaleMax
     */
    public AnalyzerBenchmark(final int pExpectedStepWidthMin,
            final int pExpectedStepWidthMax,
            final double pExpectedObjectiveFunctionScaleMin,
            final double pExpectedObjectiveFunctionScaleMax) {
        expectedStepWidthMin = pExpectedStepWidthMin;
        expectedObjectiveFunctionScaleMin = pExpectedObjectiveFunctionScaleMin;

        expectedStepWidthMax = pExpectedStepWidthMax;
        expectedObjectiveFunctionScaleMax = pExpectedObjectiveFunctionScaleMax;
    }

    /**
     * Gets the minimal step width that is acceptable
     * 
     * @return
     * 		the minimal expected step width
     */
    public int getExpectedStepWidthMin() {
        return expectedStepWidthMin;
    }

    /**
     * Gets the maximal step width that is acceptable
     * 
     * @return
     * 		the maximal expected step width
     */
    public int getExpectedStepWidthMax() {
        return expectedStepWidthMax;
    }

    /**
     * Gets the minimal objective function scale that is acceptable
     * 
     * @return
     * 		the minimal obj. function scale
     */
    public double getExpectedObjectiveFunctionScaleMin() {
        return expectedObjectiveFunctionScaleMin;
    }

    /**
     * Gets the maximal objective function scale that is acceptable
     * 
     * @return
     * 		the maximal obj. function scale
     */
    public double getExpectedObjectiveFunctionScaleMax() {
        return expectedObjectiveFunctionScaleMax;
    }

}
