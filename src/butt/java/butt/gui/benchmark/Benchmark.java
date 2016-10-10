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
package butt.gui.benchmark;

/**
 * Class that represents a benchmark for the GUI. This is displayed by the tree
 * view in the algorithm/problem/result type selector view.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class Benchmark {
    /**
     * The string describing the problem
     */
    private final String problem;

    /**
     * The string describing the result type
     */
    private final String resultType;

    /**
     * The string describing the algorithm
     */
    private final String algorithm;

    /**
     * The index of the runner that was used to construct this Benchmark
     */
    private final int runnerId;

    /**
     * @return The string describing the problem
     */
    public String getProblem() {
        return problem == null ? "all problems" : problem;
    }

    /**
     * @return The string describing the result type
     */
    public String getResultType() {
        return resultType == null ? "all results" : resultType;
    }

    /**
     * @return The string describing the algorithm
     */
    public String getAlgorithm() {
        return algorithm == null ? "all algorithms" : algorithm;
    }

    /**
     * @return {@code true} if problem is wildcard. (Meaning that {@code null}
     *         has been passed to the constructor.)
     */
    public boolean isProblem() {
        return problem != null;
    }

    /**
     * @return {@code true} if result type is wildcard. (Meaning that
     *         {@code null} has been passed to the constructor.)
     */
    public boolean isResultType() {
        return resultType != null;
    }

    /**
     * @return {@code true} if algorithm is wildcard. (Meaning that {@code null}
     *         has been passed to the constructor.)
     */
    public boolean isAlgorithm() {
        return algorithm != null;
    }

    /**
     * @return The index of the runner that was used to construct this Benchmark
     */
    public int getRunnerId() {
        return runnerId;
    }

    /**
     * Construct a Benchmark object.
     * 
     * @param pRunnerId
     *            The id of the runner. This can be an arbitary integer. It is
     *            the index of the algorithm in the algorithms list which is
     *            obtained by reflections.
     * @param name
     *            The presented name of the problem. If {@code null}
     *            "all problems" is returned by {@link #getProblem()}.
     * @param result
     *            The presented name of the algorithm. If {@code null}
     *            "all problems" is returned by {@link #getResultType()}.
     * @param algorithm
     *            The presented name of the result type. If {@code null}
     *            "all problems" is returned by {@link #getAlgorithm()}.
     */
    Benchmark(int pRunnerId, String name, String result, String algorithm) {
        this.problem = name;
        this.algorithm = algorithm;
        this.resultType = result;
        runnerId = pRunnerId;
    }
}
