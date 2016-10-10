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

import static org.junit.Assert.assertFalse;

import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import butt.tool.benchmark.IRunnableBenchmark;
import butt.tool.benchmark.common.Benchmarks;
import butt.tool.benchmark.common.GameHelper;
import game.connect4.ConnectGame;
import game.hanoi.Hanoi;
import gps.GPS;
import gps.games.GamesModule;
import gps.games.wrapper.Game;

/**
 *
 * Tests on {@link GameHelper} and benchmarks
 *
 * @author jschloet@tzi.de
 *
 */
public class GameHelperTest {

    //TODO:: Ignore this now as it fails due to a "The memory per node should never be a negative value" exception. This problem gets fixed in another MR so we can remove the @Ignore tag when that is done.
    @Ignore
    @Test
    public void gameHelperBenchmarkHanoi3() {
        Hanoi hanoi = new Hanoi(3);
        Game<Hanoi> game = new Game<>(GPS.wrap(hanoi));
        GamesModule<Hanoi> module = new GamesModule<Hanoi>(game);
        // Filter IterativeNMCS because they are dummies right now:
        IRunnableBenchmark<Hanoi> bench = GameHelper
                .gameBenchmarks(module, "Hanoi").stream()
                .filter(a -> !a.getAlgorithm().getName()
                        .equals("Iterative NMCS"))
                .collect(Collectors.toList()).get(0);
        bench.run(Optional.empty());
    }

    @Ignore
    @Test
    public void Benchmarks() {
        java.util.List<IRunnableBenchmark<?>> benchmarks = Benchmarks
                .constructAllBenchmarks();
        assertFalse(
                benchmarks.stream().filter(a -> a.getProblem() instanceof Hanoi)
                        .collect(Collectors.toList()).isEmpty());
        assertFalse(benchmarks.stream()
                .filter(a -> a.getProblem() instanceof ConnectGame)
                .collect(Collectors.toList()).isEmpty());
    }

}
