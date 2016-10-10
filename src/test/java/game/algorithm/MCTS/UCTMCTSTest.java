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
package game.algorithm.MCTS;

import game.connect4.ConnectGame;
import gps.GPS;
import gps.ResultEnum;
import gps.common.BenchmarkField;
import gps.games.GamesModule;
import gps.games.IGameResult;
import gps.games.algorithm.analysis.IGameAnalysisResult;
import gps.games.algorithm.monteCarloTreeSearch.AbstractMCTS;
import gps.games.algorithm.monteCarloTreeSearch.MASTMCTS;
import gps.games.algorithm.monteCarloTreeSearch.UCTMCTS;
import gps.games.algorithm.monteCarloTreeSearch.UCTMCTSPruning;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Various test for the UCT variant of MCTS with the game Connect4
 *
 * @author jschloet@tzi.de
 */
public class UCTMCTSTest {

    /**
     * Instance of mcts used for the tests.
     */
    private UCTMCTS<ConnectGame> mcts;

    /**
     * Game wrapper used for this test
     */
    private Game<ConnectGame> c4g;

    /**
     * Initializes a new UCTMCTS with a game of connect4 before each test.
     */
    @Before
    public void init() {
        ConnectGame cg = ConnectGame.createConnect4();
        c4g = new Game<>(GPS.wrap(cg));
    }

    /**
     * Tests the result of MCTS for an empty standard connect4 game. Also checks
     * whether the time limit is met.
     */
    @Test
    public void mctsEmptyC4() {
        mcts = new UCTMCTS<ConnectGame>(new GamesModule<>(c4g));
        // Use a timelimit of a half second.
        long startingTime = System.currentTimeMillis();
        mcts.setTimelimit(500);
        mcts.start();
        // According to wikipedia the starting move should be
        // the middle column, i.e. 3
        assertEquals(new Action(3), mcts.getMove());
        // Should only take one second plus some time to set up
        // the algorithm and to finish the last iteration.
        long usedTime = System.currentTimeMillis() - startingTime;
        assertTrue(usedTime < 550);
        System.out.println(
                "Empy C4 without pruning: " + mcts.getTree().getVisitCount());
    }

    /**
     * Tests the result of MCTS for an empty standard connect4 game. Also checks
     * whether the time limit is met. Uses pruning post analysis so nodes should
     * be pruned.
     */
    @Test
    public void mctsEmptyC4withPruningPostAnalysis() {
        // Testing mcts with pruning post analysis:
        GamesModule<ConnectGame> module = new GamesModule<>(c4g);
        // Enforce the module to analyse because of the time assertion
        module.gameAnalysis().get();
        mcts = new UCTMCTSPruning<>(module);
        long startingTime = System.currentTimeMillis();
        // Use a timelimit of a half second
        mcts.setTimelimit(600);
        mcts.start();
        // According to wikipedia the starting move should be
        // the middle column, i.e. 3
        assertEquals(new Action(3), mcts.getMove());
        // Should only take one second plus some time to set up
        // the algorithm and to finish the last iteration.
        long usedTime = System.currentTimeMillis() - startingTime;
        assertTrue(((UCTMCTSPruning<ConnectGame>) mcts).isPruningSuccessful());
        System.out.println(
                "Empy C4 with pruning : " + mcts.getTree().getVisitCount()
                        + " used Time: " + usedTime + "ms");
    }

    /**
     * The problem size is to big to start pruning during the given time.
     */
    @Test
    public void emptyConnect6DoNotStartPruning() {
        mcts = new UCTMCTSPruning<>(new GamesModule<>(
                new Game<>(GPS.wrap(ConnectGame.createConnect6()))));
        mcts.setTimelimit(600);
        mcts.start();
        assertFalse(((UCTMCTSPruning<ConnectGame>) mcts).isPruningSuccessful());
        System.out.println(
                "Connect6 (Dont start pruning): Move: " + mcts.getMove());
    }

    /**
     * The given time should be enough to start pruning
     */
    @Ignore
    @Test
    public void emptyConnect6DoStartPruning() {
        GamesModule<ConnectGame> module = new GamesModule<>(
                new Game<>(GPS.wrap(ConnectGame.createConnect6())));
        mcts = new UCTMCTSPruning<>(module);
        IGameAnalysisResult gra = module.gameAnalysis().get();
        mcts.setTimelimit(
                (int) ((gra.getAvgBranchingFactor() * gra.getAvgDepth() / 147)
                        + gra.getAvgTime() * 3));
        mcts.start();
        assertTrue(((UCTMCTSPruning<ConnectGame>) mcts).isPruningSuccessful());
        System.out.println("Connect6 (Start pruning): Move: " + mcts.getMove());
    }

    /**
     * Test whether the constructor with options sets the basic starting time
     * correct.
     */
    @Ignore
    @Test
    public void pruningOptionsConstructorTest() {
        mcts = new UCTMCTSPruning<>(new GamesModule<>(
                new Game<>(GPS.wrap(ConnectGame.createConnect6()))));
        mcts.setTimelimit(300);
        mcts.start();
        assertFalse(((UCTMCTSPruning<ConnectGame>) mcts).isPruningSuccessful());
    }

    /**
     * Tests if MCTS selects a move that can prevent an otherwise straight loss.
     */
    @Test
    public void preventStraightLoss() {
        c4g.applyAction(new Action(3));
        c4g.applyAction(new Action(4));
        c4g.applyAction(new Action(3));
        c4g.applyAction(new Action(3));
        c4g.applyAction(new Action(4));
        c4g.applyAction(new Action(2));
        c4g.applyAction(new Action(5));
        c4g.applyAction(new Action(6));
        c4g.applyAction(new Action(2));
        mcts = new UCTMCTS<ConnectGame>(new GamesModule<>(c4g));
        mcts.setTimelimit(500);
        mcts.start();
        assertEquals(new Action(5), mcts.getMove());
    }

    /**
     * Tests if MCTS selects a move that causes a straight win.
     */
    @Test
    public void takeStraightWin() {
        c4g.applyAction(new Action(3));
        c4g.applyAction(new Action(3));
        c4g.applyAction(new Action(4));
        c4g.applyAction(new Action(3));
        c4g.applyAction(new Action(3));
        c4g.applyAction(new Action(4));
        c4g.applyAction(new Action(2));
        c4g.applyAction(new Action(5));
        c4g.applyAction(new Action(6));
        c4g.applyAction(new Action(2));
        c4g.applyAction(new Action(0));
        mcts = new UCTMCTS<ConnectGame>(new GamesModule<>(c4g));
        mcts.setTimelimit(500);
        mcts.start();
        assertEquals(new Action(5), mcts.getMove());
    }

    /**
     * Tests whether the starting player wins if two MCTS play against each
     * other.
     */
    // Ignoring this, as it only succeeds on about
    // 86 out of 100 tries and builds should not
    // fail at random.
    @Ignore
    @Test
    public void forceWinFirstPlayer() {
        while (!c4g.isTerminal()) {
            mcts = new UCTMCTS<ConnectGame>(new GamesModule<>(c4g));
            mcts.setTimelimit(250);
            mcts.start();
            c4g.applyAction(mcts.getMove());
        }
        assertEquals(0, c4g.getPlayer());
    }

    /**
     * Tests whether the second player wins if two MCTS play against each other
     * and the first player starts by placing a mark in an outer column
     */
    // Ignoring this, as it only succeeds on about
    // 83 out of 100 tries and builds should not
    // fail at random.
    @Ignore
    @Test
    public void forceWinSecondPlayer() {
        c4g.applyAction(new Action(0));
        while (!c4g.isTerminal()) {
            mcts = new UCTMCTS<ConnectGame>(new GamesModule<>(c4g));
            mcts.setTimelimit(250);
            mcts.start();
            c4g.applyAction(mcts.getMove());
        }
        assertEquals(1, c4g.getPlayer());
    }

    /**
     * Tests whether a given repetition limit is met.
     */
    @Test
    public void repetitionLimit() {
        mcts = new UCTMCTS<ConnectGame>(new GamesModule<>(c4g));
        mcts.useRepetitionLimit(100);
        mcts.start();
        assertEquals(100, mcts.getTree().getVisitCount());
    }

    /**
     * Tests whether the continuation of mcts with the existing game tree works.
     */
    @Test
    public void resume() {
        mcts = new UCTMCTS<ConnectGame>(new GamesModule<>(c4g));
        mcts.useRepetitionLimit(1);
        mcts.start();
        // Should do one repetition
        assertTrue(mcts.getTree().getVisitCount() == 1);
        mcts.resume();
        // Should do one repetition again with the existing tree
        assertTrue(mcts.getTree().getVisitCount() == 2);
        mcts.resume(100);
        assertTrue(mcts.getTree().getVisitCount() > 2);
    }

    /**
     * The isApplicable method should return true
     */
    @Test
    public void isApplicable() {
        mcts = new UCTMCTS<ConnectGame>(new GamesModule<>(c4g));
        assertTrue(mcts.isApplicable(ResultEnum.BEST_MOVE));
        assertFalse(mcts.isApplicable(ResultEnum.MOVES));
    }

    /**
     * Executes various test on the {@link IGameResult} functionality of
     * {@link #mcts}.
     */
    @Test
    public void mctsResult() {
        mcts = new UCTMCTS<ConnectGame>(new GamesModule<>(c4g));
        IGameResult<ConnectGame> result = mcts;
        mcts.useRepetitionLimit(0);
        // The returned GameResult should not be null
        assertNotNull(result);
        // The returned optional should not be null
        assertNotNull(result.bestMove());
        // After one repetition a value for the best move
        // should be present
        assertTrue(result.bestMove().isPresent());
        // Should be null as no repetition was used.
        assertNull(result.bestMove().get().get());
        mcts.resume(250);
        // Should be up to date now:
        // Just testing whether result.bestMove() returns the correct move
        // The quality of that move is tested in the other test cases.
        assertEquals(mcts.getMove().get(), result.bestMove().get().get());
    }

    /**
     * Tests whether the algorithm actually stops when
     * {@link Thread#interrupt()} is called.
     */
    @Test
    public void stopOnInterrupt() {
        try {
            GamesModule<ConnectGame> module = new GamesModule<>(c4g);
            // TODO:: An interrupt during the analysis has not the correct
            // effect
            // Therefore the next line is added to prevent an interrupt during
            // the analysis
            module.gameAnalysis().get();
            mcts = new UCTMCTSPruning<>(module);
            Thread thread = new Thread(() -> mcts.start());
            thread.start();
            Thread.sleep(250);
            thread.interrupt();
            thread.join();
            assertTrue(mcts.bestMove().isPresent());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void benchmarkMCTS() {
        mcts = new UCTMCTS<>(new GamesModule<>(c4g));
        benchmarks(mcts);
        mcts = new UCTMCTSPruning<>(new GamesModule<>(c4g), 120);
        benchmarks(mcts);
        mcts = new MASTMCTS<>(new GamesModule<>(c4g));
        benchmarks(mcts);
    }

    private void benchmarks(final AbstractMCTS<ConnectGame> mcts) {
        mcts.useRepetitionLimit(1);
        mcts.start();
        // One Repetition should mean one simululation is played
        assertEquals(1, mcts.getBenchmark()
                .getField(BenchmarkField.NUMBER_OF_SIMULATIONS).get());
        // With only one simulation played, the clearness of best move value should equal one
        // utility return value of connect game
        assertTrue(mcts.getBenchmark()
                .getField(BenchmarkField.CLEARNESS_OF_BEST_MOVE).get()
                .intValue() == 100000
                || mcts.getBenchmark()
                        .getField(BenchmarkField.CLEARNESS_OF_BEST_MOVE).get()
                        .intValue() == -100000
                || mcts.getBenchmark()
                        .getField(BenchmarkField.CLEARNESS_OF_BEST_MOVE).get()
                        .intValue() == 0);
        // The heuristic value of the best move should be present:
        assertTrue(mcts.getBenchmark()
                .getField(BenchmarkField.BEST_MOVE_HEURISTIC).isPresent());
        mcts.setTimelimit(200);
        mcts.start();
        //Now the number of simulations should be higher:
        assertTrue(1 < mcts.getBenchmark()
                .getField(BenchmarkField.NUMBER_OF_SIMULATIONS).get()
                .intValue());
        // The values should not be 0
        assertTrue(0 != mcts.getBenchmark()
                .getField(BenchmarkField.GAME_TREE_DEPTH).get().intValue());
        assertTrue(0 != mcts.getBenchmark()
                .getField(BenchmarkField.DEEPEST_DISCOVERED_NODE).get()
                .intValue());
        assertTrue(0 != mcts.getBenchmark().getField(BenchmarkField.SEEN_NODES)
                .get().intValue());
        assertTrue(0 != mcts.getBenchmark()
                .getField(BenchmarkField.PROCESSED_NODES).get().intValue());
        //The deepest discovered node should be higher then the game tree depth
        assertTrue(
                mcts.getBenchmark().getField(BenchmarkField.GAME_TREE_DEPTH)
                        .get().intValue() < mcts.getBenchmark()
                                .getField(
                                        BenchmarkField.DEEPEST_DISCOVERED_NODE)
                                .get().intValue());
        //The number of seen nodes should be higher than the highest depth and game tree depth:
        assertTrue(mcts.getBenchmark().getField(BenchmarkField.GAME_TREE_DEPTH)
                .get().intValue() < mcts.getBenchmark()
                        .getField(BenchmarkField.SEEN_NODES).get().intValue());
        assertTrue(mcts.getBenchmark()
                .getField(BenchmarkField.DEEPEST_DISCOVERED_NODE).get()
                .intValue() < mcts.getBenchmark()
                        .getField(BenchmarkField.SEEN_NODES).get().intValue());
        //and higher than the number of processed nodes
        assertTrue(mcts.getBenchmark().getField(BenchmarkField.PROCESSED_NODES)
                .get().intValue() < mcts.getBenchmark()
                        .getField(BenchmarkField.SEEN_NODES).get().intValue());
    }

}