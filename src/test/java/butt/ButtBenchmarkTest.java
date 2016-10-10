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

import benchmarks.BenchmarkConnect;
import gps.ResultEnum;
import gps.games.algorithm.monteCarloTreeSearch.UCTMCTS;
import gps.games.algorithm.mtdf.MTDf;
import org.junit.Test;

import java.util.Optional;

/**
 * Some tests for benchmarks in butt
 *
 * @author haker@uni-bremen.de
 *
 */
public class ButtBenchmarkTest {

    @Test
    public void mtdfInterruptionTest() {
        new BenchmarkConnect().getRunners().stream()
                .filter(m -> (m.getAlgorithm() instanceof MTDf)
                        && (m.getResultType().equals(ResultEnum.BEST_MOVE)))
                .findFirst().get().run(Optional.empty());
    }

    @Test
    public void nmcsInterruptionTest() {
        new BenchmarkConnect().getRunners().stream()
                .filter(m -> (m.getAlgorithm() instanceof UCTMCTS)
                        && (m.getResultType().equals(ResultEnum.BEST_MOVE)))
                .findFirst().get().run(Optional.empty());
    }
}
