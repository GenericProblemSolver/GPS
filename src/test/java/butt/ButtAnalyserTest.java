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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import butt.tool.benchmark.common.Benchmarks;
import game.pathfinder.Maze;
import game.vectorracer.VectorRacer;
import gps.games.GamesModule;
import gps.util.AnalysisCache;

/**
 * Some tests for the analyser of butt.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class ButtAnalyserTest {
    @Test
    public void analyserTestVectorRacer() {
        for (int i = 0; i < 100; i++) {
            Benchmarks.benchmarksGamesStream()
                    .filter(p -> p.getProblem() instanceof VectorRacer)
                    .map(m -> new GamesModule<>(m)).findAny().get()
                    .gameAnalysis();
        }
    }

    @Test
    public void analyserTestMaze() {
        Benchmarks.benchmarksGamesStream()
                .filter(p -> p.getProblem() instanceof Maze)
                .map(m -> new GamesModule<>(m)).findAny().get().gameAnalysis();
    }

    @Test
    public void analyserNonCachedGamesTest() {
        // must find 0 non cached games
        assertEquals(0,
                Benchmarks.benchmarksGamesStream()
                        .map(m -> AnalysisCache.getCachedResult(m))
                        .filter(p -> !p.isPresent()).count());
    }

}
