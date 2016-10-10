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
package benchmarks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import butt.tool.benchmark.IBenchmarkRunner;
import butt.tool.benchmark.IRunnableBenchmark;
import butt.tool.benchmark.common.GameHelper;
import game.gempuzzle.GemPuzzle;
import gps.GPS;
import gps.games.GamesModule;
import gps.games.wrapper.Game;
import gps.util.Tuple;

/**
 * Benchmarks for the GemPuzzle
 * 
 * @author haker@uni-bremen.de
 *
 */
public class BenchmarkGemPuzzle implements IBenchmarkRunner<GemPuzzle> {

    /**
     * The seed for the gem puzzle
     */
    private final static long seed = 12345;

    @Override
    public List<IRunnableBenchmark<GemPuzzle>> getRunners() {
        ArrayList<IRunnableBenchmark<GemPuzzle>> list = new ArrayList<IRunnableBenchmark<GemPuzzle>>();
        streamHelper().forEach(tuple -> list.addAll(GameHelper.gameBenchmarks(
                new GamesModule<>(tuple.getY()), tuple.getX())));
        return list;

    }

    /**
     * Stream of tuples that represent tuples of problem description and game
     * instance.
     * 
     * @return The tuple.
     */
    private static Stream<Tuple<String, Game<GemPuzzle>>> streamHelper() {
        return Stream.of(2, 3, 4, 5, 6, 7)
                .map(radix -> new Tuple<String, Game<GemPuzzle>>(
                        "Gem(" + radix + ")",
                        new Game<>(GPS.wrap(new GemPuzzle(radix, seed)))));
    }

    @Override
    public Stream<Game<GemPuzzle>> getProblemInstancesStream() {
        return streamHelper().map(Tuple::getY);
    }
}
