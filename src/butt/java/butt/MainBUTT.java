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
package butt;

import butt.tool.Arguments;
import butt.tool.BenchmarkTool;
import butt.tool.EvaluationTool;
import butt.tool.NeuronalNetworkTool;

/**
 * Tool for creating benchmarks for algorithms and providing training data for
 * the game classifier.
 * 
 * This class implements the headless mode.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class MainBUTT {

    /**
     * Run the GPS Benchmark and Training Tool.
     * 
     * @param args
     *            program arguments.
     */
    public static void main(final String[] args) {
        Arguments.parse(args);
        if (!Arguments.isNoBenchmark()) {
            BenchmarkTool.run();
        }
        if (Arguments.getEvaluationFilename().isPresent()
                && Arguments.getTargetFilename().isPresent()) {
            if (!Arguments.isNoEvaluation()) {
                EvaluationTool.run();
            }
            if (!Arguments.isNoLearning()) {
                NeuronalNetworkTool.run();
            }
        }
    }
}
