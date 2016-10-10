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
package game.algorithm.recycling;

import game.gempuzzle.GemPuzzle;
import game.hanoi.Hanoi;
import game.pathfinder.Maze;
import game.solitaire.Solitaire;
import gps.GPS;
import gps.games.GamesModule;
import gps.games.algorithm.nestedMonteCarloSearch.*;
import gps.games.algorithm.recycling.AbstractRecycler;
import gps.games.algorithm.recycling.SearchRecycler;
import gps.games.algorithm.recycling.SearchRecyclerAlgorithm;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import gps.games.wrapper.successor.INode;
import gps.util.Tuple;
import org.junit.Ignore;
import org.junit.Test;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Various tests on the {@link AbstractRecycler} and its extension.
 * Each with using the {@link AbstractRecycler} in combination with {@link IterativeNMCS}
 *
 * @author jschloet@tzi.de
 */
public class SearchRecyclerTest {

    @Test
    public void recyclingHanoi11SearchRecycler240() {
        Hanoi puzzle = new Hanoi(11);
        recyclingComparison(puzzle, 2500, HeuristicUsage.DYNAMIC, 240, 0, true,
                SearchRecyclerAlgorithm.BREADTH_FIRST_SEARCH);
    }

    @Test
    public void recyclingHanoi11SearchRecycler120() {
        Hanoi puzzle = new Hanoi(11);
        recyclingComparison(puzzle, 2500, HeuristicUsage.CONSTANT, 120, 1,
                false, SearchRecyclerAlgorithm.ASTAR);
    }

    @Test
    public void recyclingHanoi5SearchRecycler240() {
        Hanoi puzzle = new Hanoi(5);
        recyclingComparison(puzzle, 500, HeuristicUsage.CONSTANT, 240, 2, false,
                SearchRecyclerAlgorithm.BREADTH_FIRST_SEARCH);
    }

    @Test
    public void recyclingHanoi7SearchRecycler240() {
        Hanoi puzzle = new Hanoi(7);
        recyclingComparison(puzzle, 1000, HeuristicUsage.CONSTANT, 240, 1, true,
                SearchRecyclerAlgorithm.ALL);
    }

    @Test
    public void recyclingHanoi5SearchRecycler120() {
        Hanoi puzzle = new Hanoi(5);
        recyclingComparison(puzzle, 500, HeuristicUsage.CONSTANT, 120, 3, false,
                SearchRecyclerAlgorithm.DEPTH_FIRST_SEARCH);
    }

    @Test
    public void recyclingHanoi7SearchRecycler120() {
        Hanoi puzzle = new Hanoi(7);
        recyclingComparison(puzzle, 1000, HeuristicUsage.DYNAMIC, 120, 2, false,
                SearchRecyclerAlgorithm.ALL);
    }

    @Test
    public void recyclingGem3SearchRecycler240() {
        GemPuzzle puzzle = new GemPuzzle(3, 0);
        recyclingComparison(puzzle, 2500, HeuristicUsage.CONSTANT, 240, 1, true,
                SearchRecyclerAlgorithm.BREADTH_FIRST_SEARCH);
    }

    @Test
    public void recyclingGem3SearchRecycler120() {
        GemPuzzle puzzle = new GemPuzzle(3, 0);
        recyclingComparison(puzzle, 2500, HeuristicUsage.CONSTANT, 120, 0, true,
                SearchRecyclerAlgorithm.ASTAR);
    }

    @Test
    public void recyclingSolitaireSearchRecycler240() {
        Solitaire m = new Solitaire();
        recyclingComparison(m, 2500, HeuristicUsage.NONE, 240, 1, true,
                SearchRecyclerAlgorithm.DEPTH_FIRST_SEARCH);
    }

    @Test
    public void terminateOnFoundSolution() {
        Game<Hanoi> game = new Game<>(GPS.wrap(new Hanoi(8)));
        IterativeNMCS<Hanoi> inmcs = new IterativeNMCS<>(
                new GamesModule<>(game),
                new Tuple<>(game.getUserHeuristic(), HeuristicUsage.CONSTANT),
                new NMCSGameTree<>(game.copy().asRoot()), true, true, 1,
                new SearchRecycler<>(240,
                        SearchRecyclerAlgorithm.BREADTH_FIRST_SEARCH));
        List<Action> actions = inmcs.moves().get();
        int s1 = actions.size();
        System.out.println(s1);
        Game<Hanoi> testGame = game.copy();
        actions.forEach(testGame::applyAction);
        assertTrue(testGame.isTerminal());
        actions = inmcs.moves().get();
        int s2 = actions.size();
        System.out.println(s2);
        testGame = game.copy();
        actions.forEach(testGame::applyAction);
        assertTrue(testGame.isTerminal());
        // Because of the recycler this can be equal as well:
        assertTrue(s2 <= s1);
    }

    @Ignore
    //Ignore this, as NMCS is not the right choice to solve this (heap space).
    @Test
    public void recyclingPathfindingSearchRecycler240() {
        Maze m = new Maze("pathfinding/maze.bmp");
        recyclingComparison(m, 2500, HeuristicUsage.CONSTANT, 240, 4, true,
                SearchRecyclerAlgorithm.BREADTH_FIRST_SEARCH);
    }

    private static <T> void recyclingComparison(T problem, int timeLimit,
            HeuristicUsage usage, int seperator, int level, boolean gameTree,
            SearchRecyclerAlgorithm algorithm) {
        try {
            Game<T> game = new Game<>(GPS.wrap(problem));
            INMCSData<T> data = (gameTree)
                    ? new NMCSGameTree<>(game.copy().asRoot())
                    : new NMCSPathStorage<>();
            AbstractRecycler<T> recycler = new SearchRecycler<>(seperator,
                    algorithm);
            IterativeNMCS<T> nmcs = new IterativeNMCS<>(
                    new GamesModule<>(game.copy()),
                    new Tuple<>(game.getUserHeuristic(), usage), data, true,
                    true, level, recycler);
            nmcs.useTimeLimit(timeLimit);
            nmcs.terminateOnFoundSolution(false);
            if (nmcs.stateSequence().isPresent()
                    && recycler.getBestElem() != null) {
                List<Action> nmcsResult = nmcs.moves().get();
                List<Action> recyclerResult = recycler.getBestElem().stream()
                        .filter(a -> !a.isRoot()).map(INode::getAction)
                        .collect(Collectors.toList());
                //TODO:: The results are not correct right now:
                System.out.println(nmcsResult.size());
                System.out.println(recyclerResult.size());
                Game<T> testGame = game.copy();
                nmcsResult.forEach(testGame::applyAction);
                testGame = game.copy();
                recyclerResult.forEach(testGame::applyAction);
                assertTrue(testGame.isTerminal());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}