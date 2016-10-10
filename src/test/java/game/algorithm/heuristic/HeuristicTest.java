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
package game.algorithm.heuristic;

import game.connect4.ConnectGame;
import game.hanoi.Hanoi;
import gps.GPS;
import gps.games.GamesModule;
import gps.games.MemorySavingMode;
import gps.games.algorithm.heuristic.DeltaHeuristic;
import gps.games.algorithm.heuristic.HeuristicUtility;
import gps.games.algorithm.nestedMonteCarloSearch.HeuristicNMCS;
import gps.games.algorithm.nestedMonteCarloSearch.HeuristicUsage;
import gps.games.algorithm.nestedMonteCarloSearch.IterativeNMCS;
import gps.games.algorithm.nestedMonteCarloSearch.NMCSGameTree;
import gps.games.algorithm.singleplayer.AStar;
import gps.games.algorithm.singleplayer.common.datastruct.ToEvalSortedList;
import gps.games.wrapper.Game;
import gps.games.wrapper.ISingleplayerHeuristic;
import gps.util.Tuple;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests all {@link ISingleplayerHeuristic}s returned by {@link HeuristicUtility#getAllSingleplayerHeuristics(Game)}
 * using {@link Hanoi} and {@link HeuristicNMCS} respectively {@link AStar}.
 *
 * @author jschloet@tzi.de
 */
public class HeuristicTest {

    // This seems to make no sense, cause Connect4 is not a singleplayer game. But
    // it should work nevertheless and we do not have singleplayer games with
    // getPlayer right now.
    @Test
    public void iterativeNMCSConnect4() {
        ConnectGame cg = ConnectGame.createConnect4();
        Game<ConnectGame> game = new Game<>(GPS.wrap(cg));
        IterativeNMCS<ConnectGame> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.CONSTANT),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 3, null);
        inmcs.useTimeLimit(100);
        inmcs.startAlgorithm();
    }

    // This seems to make no sense, cause Connect4 is not a singleplayer game. But
    // it should work nevertheless and we do not have singleplayer games with
    // getPlayer right now.
    @Test
    public void iterativeNMCSConnect4Delta() {
        ConnectGame cg = ConnectGame.createConnect4();
        Game<ConnectGame> game = new Game<>(GPS.wrap(cg));
        IterativeNMCS<ConnectGame> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(DeltaHeuristic.createUsingDepthFirstSearch(game),
                        HeuristicUsage.CONSTANT),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 3, null);
        inmcs.useTimeLimit(100);
        inmcs.startAlgorithm();
    }

    @Test
    public void testHeuristicsNMCS() {
        Hanoi hanoi = new Hanoi(4);
        Game<Hanoi> game = new Game<>(GPS.wrap(hanoi));
        for (ISingleplayerHeuristic heuristic : HeuristicUtility
                .getAllSingleplayerHeuristics(game.copy())) {
            System.out.println(heuristic + ":");
            HeuristicNMCS<Hanoi> nmcs = new HeuristicNMCS<>(game.copy(),
                    new NMCSGameTree<>(game.copy().asRoot()), heuristic,
                    MemorySavingMode.NONE);
            nmcs.setStartingLevel(0);
            nmcs.setShortestPath(true);
            nmcs.setCircleAvoidance(true);
            nmcs.start();
            Game<Hanoi> testGame = game.copy();
            for (gps.games.wrapper.Action a : nmcs.getSequence()) {
                testGame.applyAction(a);
            }
            assertTrue(testGame.isTerminal());
            System.out.println("Size: " + nmcs.getSequence().size());
        }
    }

    @Test
    public void testHeuristicsAstar() {
        Hanoi hanoi = new Hanoi(4);
        Game<Hanoi> game = new Game<>(GPS.wrap(hanoi));
        for (ISingleplayerHeuristic heuristic : HeuristicUtility
                .getAllSingleplayerHeuristics(game.copy())) {
            System.out.println(heuristic + ":");
            try {
                AStar<Hanoi> astar = new AStar<Hanoi>(
                        new GamesModule<Hanoi>(game), heuristic,
                        ToEvalSortedList.class);
                List<gps.games.wrapper.Action> seq = astar.moves().get();
                Game<Hanoi> testGame = game.copy();
                for (gps.games.wrapper.Action a : seq) {
                    testGame.applyAction(a);
                }
                assertTrue(testGame.isTerminal());
                System.out.println("Size: " + seq.size());

            } catch (Exception e) {
                fail();
            }
        }
    }
}
