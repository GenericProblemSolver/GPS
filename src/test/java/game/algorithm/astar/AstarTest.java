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
package game.algorithm.astar;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import game.gempuzzle.GemPuzzle;
import game.hanoi.Hanoi;
import gps.GPS;
import gps.games.GamesModule;
import gps.games.algorithm.singleplayer.AStar;
import gps.games.algorithm.singleplayer.BreadthFirst;
import gps.games.algorithm.singleplayer.IterativeDeepening;
import gps.games.algorithm.singleplayer.BreadthFirstSp;
import gps.games.algorithm.singleplayer.DepthFirst;
import gps.games.algorithm.singleplayer.common.datastruct.ToEvalSortedList;
import gps.games.algorithm.singleplayer.common.datastruct.ToEvalTree;
import gps.games.wrapper.Game;

/**
 * Testclass for the A-Star algorithm
 * 
 * @author haker@uni-bremen.de
 *
 */
public class AstarTest {
    private final static Logger logger = Logger
            .getLogger(AstarTest.class.getCanonicalName());

    private AlgoTest<Hanoi> han, miniHan;
    private AlgoTest<GemPuzzle> gem;

    private class AlgoTest<T> {
        AlgoTest(T game) throws InstantiationException, IllegalAccessException,
                IllegalArgumentException, InvocationTargetException,
                NoSuchMethodException, SecurityException {
            final Game<T> g = new Game<>(GPS.wrap(game));
            as = new AStar<T>(new GamesModule<>(g), g.getUserHeuristic(),
                    ToEvalTree.class);
            asl = new AStar<T>(new GamesModule<>(g), g.getUserHeuristic(),
                    ToEvalSortedList.class);
            bf = new BreadthFirst<>(new GamesModule<>(g));
            bfs = new BreadthFirstSp<>(new GamesModule<>(g));
            df = new DepthFirst<>(new GamesModule<>(g));
            ids = new IterativeDeepening<>(new GamesModule<T>(g));
        }

        /**
         * game with astar solver with tree
         */
        private final AStar<T> as;

        /**
         * game with astar solver with list
         */
        private final AStar<T> asl;

        /**
         * game with breadth first solver
         */
        private final BreadthFirst<T> bf;

        /**
         * game with breadth first solver
         */
        private final BreadthFirstSp<T> bfs;

        /**
         * with breadth depth solver
         */
        private final DepthFirst<T> df;

        /**
         * with iterative deepening solver
         */
        private final IterativeDeepening<T> ids;
    }

    private void runTest(String algoname, String gamename, ITestFunc res) {

        long nanos = System.nanoTime();
        String s = res.run();
        if (s.equals("true")) {
            s = "Goal found!";
        }
        nanos = System.nanoTime() - nanos;
        logger.info("Benchmark:      " + gamename + " with " + algoname
                + ",      result: " + s + "      (" + nanos / 1000000 + "ms)");
    }

    /**
     * Initializes the fields.
     */
    @Before
    public void init() throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        han = new AlgoTest<>(new Hanoi(7));
        miniHan = new AlgoTest<>(new Hanoi(3));
        gem = new AlgoTest<>(new GemPuzzle(3, 0));
    }

    private interface ITestFunc {
        String run();
    }

    @Test
    public void testHanoiASStateSeq() {
        runTest("ASTAR (tree)", "HNAOI",
                () -> han.as.stateSequence().get().size() + " states");
    }

    @Test
    public void testHanoiIDSIsWinnable() {
        runTest("IDS", "HANOI",
                () -> (miniHan.ids.isWinnable().get().toString()));
    }

    @Test
    public void testHanoiASLStateSeq() {
        runTest("ASTAR (list)", "HANOI",
                () -> han.asl.stateSequence().get().size() + " states");
    }

    @Test
    public void testHanoiBFStateSeq() {
        runTest("BREADTH", "HANOI",
                () -> han.bf.stateSequence().get().size() + " states");
    }

    @Test
    @Ignore
    public void testHanoiBFSStateSeq() {
        runTest("BREADTHS", "HANOI",
                () -> han.bfs.stateSequence().get().size() + " states");
    }

    @Test
    public void testHanoiDFStateSeq() {
        runTest("DEPTH", "HANOI",
                () -> han.df.stateSequence().get().size() + " states");
    }

    @Test
    public void testGemASStateSeq() {
        runTest("ASTAR (tree)", "GEM",
                () -> gem.as.stateSequence().get().size() + " states");
    }

    @Test
    public void testGemASLStateSeq() {
        runTest("ASTAR (list)", "GEM",
                () -> gem.asl.stateSequence().get().size() + " states");
    }

    @Test
    public void testGemBFStateSeq() {
        runTest("BREADTH", "GEM",
                () -> gem.bf.stateSequence().get().size() + " states");
    }

    @Test
    @Ignore
    public void testGemBFSStateSeq() {
        runTest("BREADTHS", "GEM",
                () -> gem.bfs.stateSequence().get().size() + " states");
    }

    @Test
    public void testGemDFStateSeq() {
        runTest("DEPTH", "GEM",
                () -> gem.df.stateSequence().get().size() + " states");
    }

    @Test
    public void testHanoiBFStateSeq2() {
        runTest("BREADTH", "HNAOI",
                () -> han.bf.stateSequence().get().size() + " states");
        BreadthFirst<Hanoi> bfs = new BreadthFirst<Hanoi>(new GamesModule<>(
                new Game<Hanoi>(GPS.wrap(han.bf.terminalState().get()))));
        assertTrue(bfs.isWinnable().get());
    }

}
