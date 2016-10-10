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
package gps.games.algorithm.analysis;

import java.util.Arrays;
import java.util.Optional;

import gps.games.util.GameTree;
import gps.util.Tuple;

/**
 * This class represents the results of the analysis for a given problem.
 * 
 * @author Fabian
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem
 */
public class GameAnalysisResult implements IGameAnalysisResult {

    /**
     * The average branching factor of the randomly generated tree object.
     */
    private final Double avgBranchingFactor;

    /**
     * The average depth a path has in the randomly generated tree object. Is
     * never < 0.
     */
    private final Double avgDepth;

    /**
     * The amount of time needed for the analysis, averaged over all available
     * threads. Will always be >= 0.
     */
    private final Double avgTime;

    /**
     * The amount of nodes completed by all threads combined. Will always be >=
     * 0.
     */
    private final Integer completedNodeCount;

    /**
     * The first value is the minimum depth found in the randomly generated tree
     * object. Does never contain values < 0. Also the minimum will always be
     * smaller (<=) than the maximum.
     */
    private final Integer depthRangeMin;

    /**
     * The value is the maximum depth found in the randomly generated tree
     * object. Does never contain values < 0. Also the minimum will always be
     * smaller (<=) than the maximum.
     */
    private final Integer depthRangeMax;

    /**
     * The number of different players (based on HashSet equality) that were
     * found in the analysis. Will ne Optional.empty() if it has not been set by
     * the analyser. A possible reason for this is a lack of the needed method.
     */
    private final Integer playerNumber;

    /**
     * The amount of paths that terminated divided by the amount of paths that
     * had a terminal test method (which should either be none or all of them).
     * Represents the percentage of paths that terminated during the analysis.
     */
    private final Double terminationRate;

    /**
     * The value is the minimum utility value found. Will be null if it has not
     * been set by the analyser. A possible reason for this is a lack of the
     * needed method.
     */
    private final Double utilityRangeMin;

    /**
     * The maximum utility value found. Will be null if it has not been set by
     * the analyser. A possible reason for this is a lack of the needed method.
     */
    private final Double utilityRangeMax;

    /**
     * The memory used per node in the given tree. Saved in bytes.
     */
    private final Double memoryPerNode;

    /**
     * The constructor for a GameAnalysisResult. The GameAnalysisResult is based
     * contains all information found by the Analyser about a given problem.
     * 
     * @param pAvgBranchingFactor
     *            The new average branching factor for this object. Can not be <
     *            0.
     * @param pAvgDepth
     *            The new average depth for this object. Can not be < 0
     * @param pAvgTime
     *            The time used by the workers, averaged over all available
     *            threads. Can not be < 0.
     * @param pCompletedNodeCount
     *            The amount of nodes visited by the CPU while the analysis was
     *            running. Can not be < 0.
     * @param pTerminationRate
     *            The new termination rate for this object. Represents a
     *            percentage, has to be between 0 and 1.
     * @param pDepthRange
     *            The new depth range for this object. Can not be {@code null},
     *            can not contain values < 0. Since the first value represents
     *            the minimum and the second value the maximum, there has to be
     *            a x <= y relation.
     * @param pTree
     *            The new tree saved in this object. Can not be {@code null}.
     * @param pPlayerNumber
     *            The new amount of players found in the analysis. Has to
     *            contain an integer value >= 1 if set, has to be
     *            Optional.empty() otherwise. Can not be {@code null}.
     * @param pUtilityRange
     *            The new utility range for this object. Has to be
     *            Optional.empty() when not set. Can not be {@code null}.
     *            Otherwise, since the first value represents the minimum and
     *            the second value the maximum, there has to be a value1 <=
     *            value2 relation.
     * @param pMemoryPerNode
     *            The memory that is used per node in the given tree. Will not
     *            be set if the analysis thread is interrupted
     */
    public GameAnalysisResult(final Double pAvgBranchingFactor,
            final Double pAvgDepth, final Double pAvgTime,
            final Integer pCompletedNodeCount, final Double pTerminationRate,
            final Tuple<Integer, Integer> pDepthRange, final GameTree<?> pTree,
            final Optional<Integer> pPlayerNumber,
            final Optional<Tuple<Number, Number>> pUtilityRange,
            final Optional<Double> pMemoryPerNode) {
        if (pAvgBranchingFactor < 0) {
            throw new IllegalArgumentException(
                    "Average branching factor can not be < 0");
        }
        avgBranchingFactor = pAvgBranchingFactor;
        if (pAvgDepth < 0) {
            throw new IllegalArgumentException("Average depth can not be < 0");
        }
        avgDepth = pAvgDepth;

        if (pAvgTime < 0) {
            throw new IllegalArgumentException("Average time can not be < 0");
        }
        avgTime = pAvgTime;
        if (pCompletedNodeCount < 0) {
            throw new IllegalArgumentException(
                    "Completed node count can not be < 0");
        }
        completedNodeCount = pCompletedNodeCount;

        if (pDepthRange == null) {
            throw new NullPointerException("Depth range tuple cant be null.");
        }
        if (pDepthRange.getX() < 0 || pDepthRange.getY() < 0) {
            throw new IllegalArgumentException(
                    "Values saved in the tuple cant be < 0.");
        }
        if (pDepthRange.getX() > pDepthRange.getY()) {
            throw new IllegalArgumentException(
                    "Minimum depth cant be greater than maximum depth.");
        }
        depthRangeMin = pDepthRange.getX();
        depthRangeMax = pDepthRange.getY();
        if (pPlayerNumber == null) {
            throw new NullPointerException(
                    "Optional playernumber should not be null.");
        }
        if (pPlayerNumber.isPresent() && pPlayerNumber.get() < 1) {
            throw new IllegalArgumentException("Player number can not be < 1.");
        }
        playerNumber = pPlayerNumber.orElse(null);

        if (pTerminationRate < 0 || pTerminationRate > 1) {
            throw new IllegalArgumentException(
                    "Percental values can only be between 0 and 1.");
        }
        terminationRate = pTerminationRate;
        if (pUtilityRange == null) {
            throw new NullPointerException(
                    "Optional utility range cant be null.");
        }
        if (pUtilityRange.isPresent() && (pUtilityRange.get().getX()
                .doubleValue() > pUtilityRange.get().getY().doubleValue())) {
            throw new IllegalArgumentException(
                    "Minimum utility cant be greater than maximum utility.");
        }
        if (pUtilityRange.isPresent()) {
            utilityRangeMin = pUtilityRange.get().getX().doubleValue();
            utilityRangeMax = pUtilityRange.get().getY().doubleValue();
        } else {
            utilityRangeMin = null;
            utilityRangeMax = null;
        }

        if (pMemoryPerNode == null) {
            throw new NullPointerException("Memory usage cannot be null.");
        }
        // Will be Optional.empty() if < 0
        memoryPerNode = pMemoryPerNode.orElse(null);
    }

    @Override
    public Double getAvgBranchingFactor() {
        return avgBranchingFactor;
    }

    @Override
    public Double getAvgDepth() {
        return avgDepth;
    }

    @Override
    public Double getAvgTime() {
        return avgTime;
    }

    @Override
    public Integer getCompletedNodeCount() {
        return completedNodeCount;
    }

    @Override
    public Tuple<Integer, Integer> getDepthRange() {
        return new Tuple<Integer, Integer>(depthRangeMin, depthRangeMax);
    }

    @Override
    public Optional<Integer> getPlayerNumber() {
        return Optional.ofNullable(playerNumber);
    }

    @Override
    public Double getTerminationRate() {
        return terminationRate;
    }

    @Override
    public Optional<Tuple<Double, Double>> getUtilityRange() {
        if (utilityRangeMin != null && utilityRangeMax != null) {
            return Optional.of(new Tuple<Double, Double>(utilityRangeMin,
                    utilityRangeMax));
        }
        return Optional.empty();
    }

    @Deprecated
    @Override
    public GameTree<?> getTree() {
        return new GameTree<>();
    }

    @Override
    public Optional<Double> getMemoryPerNode() {
        return Optional.ofNullable(memoryPerNode);
    }

    @Override
    public Double[] getClassificationVector() {
        Double[] vector = new Double[] {

                getAvgBranchingFactor(),

                getAvgDepth(),

                getAvgTime(),

                getCompletedNodeCount().doubleValue(),

                getTerminationRate(),

                getMemoryPerNode().isPresent()
                        ? getMemoryPerNode().get().doubleValue() : 0d,

                getDepthRange().getX().doubleValue(),

                getDepthRange().getY().doubleValue(),

                getPlayerNumber().isPresent()
                        ? getPlayerNumber().get().doubleValue() : 0,

                getUtilityRange().isPresent()
                        ? getUtilityRange().get().getX().doubleValue() : 0d,

                getUtilityRange().isPresent()
                        ? getUtilityRange().get().getY().doubleValue() : 0d

        };
        return vector;
    }

    @Override
    public String toString() {
        return "(" + Arrays.stream(getClassificationVector())
                .map(m -> m.toString()).reduce("", (a, b) -> a + "," + b) + ")";
    }
}
