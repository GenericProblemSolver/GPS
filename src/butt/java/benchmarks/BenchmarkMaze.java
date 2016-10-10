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
import game.pathfinder.Maze;
import gps.GPS;
import gps.games.GamesModule;
import gps.games.wrapper.Game;
import gps.util.Tuple;

/**
 * Benchmarks for the Maze Puzzle
 * 
 * @author haker@uni-bremen.de
 *
 */
public class BenchmarkMaze implements IBenchmarkRunner<Maze> {

    /**
     * Path to the maze files
     */
    private static final String filepath = "pathfinding/";

    @Override
    public List<IRunnableBenchmark<Maze>> getRunners() {
        ArrayList<IRunnableBenchmark<Maze>> list = new ArrayList<IRunnableBenchmark<Maze>>();
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
    private static Stream<Tuple<String, Game<Maze>>> streamHelper() {
        return Stream.of("maze.bmp")
                .map(mazeFile -> new Tuple<String, Game<Maze>>(
                        "Maze(" + mazeFile + ")",
                        new Game<>(GPS.wrap(new Maze(filepath + mazeFile)))));
    }

    @Override
    public Stream<Game<Maze>> getProblemInstancesStream() {
        return streamHelper().map(Tuple::getY);
    }
}
