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
package game.algorithm.NMCS;

import game.hanoi.Hanoi;
import gps.GPS;
import gps.common.BenchmarkField;
import gps.games.GamesModule;
import gps.games.IGameResult;
import gps.games.algorithm.nestedMonteCarloSearch.HeuristicUsage;
import gps.games.algorithm.nestedMonteCarloSearch.IterativeNMCS;
import gps.games.algorithm.nestedMonteCarloSearch.NMCSGameTree;
import gps.games.algorithm.nestedMonteCarloSearch.NMCSPathStorage;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import gps.util.Tuple;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Various tests on {@link IterativeNMCS}.
 * 
 * @author jschloet@tzi.de
 *
 */
public class IterativeNMCSTest {

    /**
     * Tests whether iterative nmcs finds a legal solution
     * for hanoi.
     */
    @Test
    public void hanoiThreeDiskLevel3() {
        Hanoi hanoi = new Hanoi(3);
        Game<Hanoi> game = new Game<>(GPS.wrap(hanoi));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.CONSTANT),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 3, null);
        inmcs.useTimeLimit(250);
        inmcs.terminateOnFoundSolution(false);
        inmcs.setStartingLevel(3);
        inmcs.startAlgorithm();
        assertTrue(inmcs.moves().isPresent());
        assertTrue(inmcs.moves().get().size() < 11);
        inmcs.moves().get().forEach(game::applyAction);
        assertTrue(game.isTerminal());
    }

    /**
     * Tests whether iterative nmcs finds a legal solution
     * for hanoi without using the heuristic version.
     */
    @Test
    public void hanoiThreeDiskLevel3WithoutHeuristic() {
        Hanoi hanoi = new Hanoi(3);
        Game<Hanoi> game = new Game<>(GPS.wrap(hanoi));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game), null,
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 3, null);
        inmcs.useTimeLimit(500);
        inmcs.terminateOnFoundSolution(false);
        inmcs.setStartingLevel(3);
        inmcs.disableHeuristicUsage();
        inmcs.startAlgorithm();
        assertTrue(inmcs.moves().get().size() < 11);
        for (Action action : inmcs.moves().get()) {
            game.applyAction(action);
        }
        assertTrue(game.isTerminal());
    }

    /**
     * Tests whether a iterative nmcs that does not 
     * execute one full nmcs iteration stores the
     * intermediate result correctly.
     */
    @Test
    public void noFinishedNMCSLevel0() {
        Hanoi hanoi = new Hanoi(7);
        Game<Hanoi> game = new Game<>(GPS.wrap(hanoi));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.CONSTANT),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 0, null);
        inmcs.useTimeLimit(500);
        inmcs.terminateOnFoundSolution(false);
        inmcs.setStartingLevel(0);
        inmcs.startAlgorithm();
        inmcs.moves().get().forEach(game::applyAction);
        assertTrue(game.isTerminal());
    }

    /**
     * Tests whether a iterative nmcs that does not 
     * execute one full nmcs iteration stores the
     * intermediate result correctly.
     */
    @Test
    public void noFinishedNMCSLevel2() {
        Hanoi hanoi = new Hanoi(7);
        Game<Hanoi> game = new Game<>(GPS.wrap(hanoi));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.CONSTANT),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 2, null);
        inmcs.useTimeLimit(500);
        inmcs.terminateOnFoundSolution(false);
        inmcs.setStartingLevel(2);
        inmcs.setCircleAvoidance(true);
        inmcs.setShortestPath(true);
        List<Action> actions = inmcs.moves().get();
        for (int i = 0; i < actions.size(); i++) {
            game.applyAction(actions.get(i));
        }
        assertTrue(game.isTerminal());
    }

    /**
     * Tests whether a iterative nmcs that does not
     * execute one full nmcs iteration and uses {@link gps.games.algorithm.nestedMonteCarloSearch.DynamicHeuristicNMCS}
     * stores the intermediate result correctly.
     */
    @Test
    public void noFinishedNMCSLevel0DynamicHeuristic() {
        Hanoi hanoi = new Hanoi(11);
        Game<Hanoi> game = new Game<>(GPS.wrap(hanoi));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<Hanoi>(
                new GamesModule<Hanoi>(Game.copy(game)),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.DYNAMIC),
                new NMCSGameTree<>(game.asRoot()), true, true, 0, null);
        inmcs.useTimeLimit(2500);
        inmcs.terminateOnFoundSolution(false);
        inmcs.setStartingLevel(0);
        inmcs.setCircleAvoidance(true);
        inmcs.setShortestPath(true);
        if (inmcs.moves().isPresent()) {
            List<Action> actions = inmcs.moves().get();
            for (int i = 0; i < actions.size(); i++) {
                game.applyAction(actions.get(i));
            }
            assertTrue(game.isTerminal());
            System.out.println("Hanoi 11 dynamic: " + actions.size());
        }
    }

    /**
     * Tests whether the result methods, i.e the methods of {@link IGameResult} that {@link IterativeNMCS}
     * overrides, work properly after the algorithm has already 
     */
    @Test
    public void resultMethodsTestAfterFinishedAlgorithm() {
        Game<Hanoi> game = new Game<>(GPS.wrap(new Hanoi(4)));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.CONSTANT),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 1, null);
        inmcs.useTimeLimit(250);
        inmcs.setStartingLevel(1);
        inmcs.terminateOnFoundSolution(false);
        inmcs.startAlgorithm();
        // The wrapped terminal state should be a terminal state
        assertTrue(GPS.wrap(inmcs.terminalState().get()).isTerminal());
        // The state sequence should start with the given game
        assertEquals(new Hanoi(4), inmcs.stateSequence().get().get(0));
        // The state sequence should end with a terminal state.
        assertTrue(GPS
                .wrap(inmcs.stateSequence().get()
                        .get(inmcs.stateSequence().get().size() - 1))
                .isTerminal());
        // The best move should be the first move of the move sequence
        assertEquals(inmcs.bestMove().get(), inmcs.moves().get().get(0));
    }

    /**
     * Tests whether the terminal method works without calling the algorithm before
     */
    @Test
    public void terminalMethodTest() {
        Game<Hanoi> game = new Game<>(GPS.wrap(new Hanoi(4)));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.NONE),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 1, null);
        inmcs.useTimeLimit(250);
        inmcs.setStartingLevel(1);
        // The wrapped terminal state should be a terminal state
        assertTrue(GPS.wrap(inmcs.terminalState().get()).isTerminal());
    }

    /**
     * Tests whether the stateSequence method works without calling the algorithm before
     */
    @Test
    public void stateSequenceTest() {
        Game<Hanoi> game = new Game<>(GPS.wrap(new Hanoi(4)));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.DYNAMIC),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 1, null);
        inmcs.useTimeLimit(250);
        inmcs.terminateOnFoundSolution(false);
        inmcs.setStartingLevel(1);
        assertTrue(GPS
                .wrap(inmcs.stateSequence().get()
                        .get(inmcs.stateSequence().get().size() - 1))
                .isTerminal());
        assertEquals(GPS.wrap(inmcs.stateSequence().get().get(0)).getSource(),
                game.getProblem());
    }

    @Test
    public void PathStorageMultiLevel() {
        Game<Hanoi> game = new Game<>(GPS.wrap(new Hanoi(5)));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.DYNAMIC),
                new NMCSPathStorage<>(), true, true, 3, null);
        inmcs.useTimeLimit(2500);
        inmcs.terminateOnFoundSolution(false);
        inmcs.setStartingLevel(3);
        if (inmcs.moves().isPresent()) {
            List<Action> actions = inmcs.moves().get();
            for (int i = 0; i < actions.size(); i++) {
                game.applyAction(actions.get(i));
            }
            assertTrue(game.isTerminal());
        }
    }

    /**
     * Tests whether the moves method works without calling the algorithm before
     */
    @Test
    public void movesTest() {
        Game<Hanoi> game = new Game<>(GPS.wrap(new Hanoi(4)));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.CONSTANT),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 1, null);
        inmcs.useTimeLimit(250);
        inmcs.setStartingLevel(1);
        inmcs.moves().get().forEach(game::applyAction);
        assertTrue(game.isTerminal());
    }

    /**
     * Tests whether the bestMove method works without calling the algorithm before
     */
    public void bestMoveTest() {
        Game<Hanoi> game = new Game<>(GPS.wrap(new Hanoi(4)));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.CONSTANT),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 1, null);
        inmcs.useTimeLimit(250);
        inmcs.setStartingLevel(1);
        game.applyAction(inmcs.bestMove().get());
    }

    /**
     * Tests the default constructor.
     */
    // Not really useful but gets the unused warning in IterativeNMCS removed
    @Ignore
    @Test
    public void testDefaultConstructor() {
        new IterativeNMCS<Hanoi>(
                new GamesModule<Hanoi>(new Game<>(GPS.wrap(new Hanoi(4)))));
    }

    /**
     * Tests whether the termination via {@link Thread#interrupt()} works. 
     */
    //TODO:: This is the test that fails sometimes. Ignoring it now.
    @Ignore
    @Test
    public void interruptTest() {
        //Remember the active threads
        int threadNumber = Thread.activeCount();
        Game<Hanoi> game = new Game<>(GPS.wrap(new Hanoi(4)));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.CONSTANT),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 1, null);
        inmcs.setStartingLevel(1);
        Thread thread = new Thread() {
            @Override
            public void run() {
                inmcs.startAlgorithm();
            }
        };
        thread.start();
        //There should be more active threads now
        assertTrue(threadNumber < Thread.activeCount());
        thread.interrupt();
        //Wait for the recursion to dissolve
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // There should be as many active thread as there were at the beginning
        assertEquals(threadNumber, Thread.activeCount());
    }

    @Test
    public void terminateOnFoundSolution() {
        Game<Hanoi> game = new Game<>(GPS.wrap(new Hanoi(8)));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.CONSTANT),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 1, null);
        List<Action> actions = inmcs.moves().get();
        int s1 = actions.size();
        Game<Hanoi> testGame = game.copy();
        actions.forEach(testGame::applyAction);
        assertTrue(testGame.isTerminal());
        actions = inmcs.moves().get();
        int s2 = actions.size();
        testGame = game.copy();
        actions.forEach(testGame::applyAction);
        assertTrue(testGame.isTerminal());
        // Due to the recent changes in IterativeNMCS this can now be equal too:
        assertTrue(s2 <= s1);
    }

    @Test
    public void benchmarkTest() {
        Game<Hanoi> game = new Game<>(GPS.wrap(new Hanoi(8)));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.DYNAMIC),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 1, null);
        List<Action> actions = inmcs.moves().get();
        // The best solution depth should equal the size of actions
        assertEquals(actions.size(), (inmcs.getBenchmark()
                .getField(BenchmarkField.BEST_MOVE_DEPTH).get()));
        int seenNodes1 = inmcs.getBenchmark()
                .getField(BenchmarkField.SEEN_NODES).get().intValue();
        int simNumber1 = inmcs.getBenchmark()
                .getField(BenchmarkField.NUMBER_OF_SIMULATIONS).get()
                .intValue();
        // Due to recent changes, the simNumber can be greater than one.
        // For example a second simulation can be started but aborted and
        // would be counted.
        assertTrue(1 <= simNumber1);
        // At least as many nodes should be seen as the solution depth
        assertTrue(seenNodes1 >= actions.size());
        Game<Hanoi> testGame = game.copy();
        actions.forEach(testGame::applyAction);
        assertTrue(testGame.isTerminal());
        actions = inmcs.moves().get();
        // The best solution depth should equal the size of actions
        assertEquals(actions.size(), inmcs.getBenchmark()
                .getField(BenchmarkField.BEST_MOVE_DEPTH).get());
        int seenNodes2 = inmcs.getBenchmark()
                .getField(BenchmarkField.SEEN_NODES).get().intValue();
        int simNumber2 = inmcs.getBenchmark()
                .getField(BenchmarkField.NUMBER_OF_SIMULATIONS).get()
                .intValue();
        // More simulations should be executed
        assertTrue(simNumber1 < simNumber2);
        // More nodes should be seen
        assertTrue(seenNodes1 < seenNodes2);
        assertTrue(actions.size() < seenNodes2);
        testGame = game.copy();
        actions.forEach(testGame::applyAction);
    }
}