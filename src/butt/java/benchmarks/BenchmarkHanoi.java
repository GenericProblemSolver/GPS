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
import game.hanoi.Hanoi;
import gps.GPS;
import gps.games.GamesModule;
import gps.games.wrapper.Game;
import gps.util.Tuple;

/**
 * Benchmarks for the Hanoi game
 * 
 * @author haker@uni-bremen.de
 *
 */
public class BenchmarkHanoi implements IBenchmarkRunner<Hanoi> {

    @Override
    public List<IRunnableBenchmark<Hanoi>> getRunners() {
        ArrayList<IRunnableBenchmark<Hanoi>> list = new ArrayList<IRunnableBenchmark<Hanoi>>();
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
    private static Stream<Tuple<String, Game<Hanoi>>> streamHelper() {
        return Stream.of(3, 4, 5, 6, 7, 9, 12, 15, 20, 25)
                .map(hanoiDisks -> new Tuple<String, Game<Hanoi>>(
                        "Hanoi(" + hanoiDisks + ")",
                        new Game<>(GPS.wrap(new Hanoi(hanoiDisks)))));
    }

    @Override
    public Stream<Game<Hanoi>> getProblemInstancesStream() {
        return streamHelper().map(Tuple::getY);
    }
}
