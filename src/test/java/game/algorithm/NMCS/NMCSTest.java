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

import game.gempuzzle.GemPuzzle;
import game.hanoi.Hanoi;
import game.solitaire.Solitaire;
import gps.GPS;
import gps.games.MemorySavingMode;
import gps.games.algorithm.nestedMonteCarloSearch.*;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Various tests of the NMCS algorithm. 
 * Uses the hanoi game for these tests
 * 
 * @author jschloet@tzi.de
 */
public class NMCSTest {

    /**
     * The hanoi game used for the tests
     */
    private Hanoi hanoi;

    /**
     * A hanoi wrapper used to test the NMCS algorithm
     * independent of the preprocesing
     */
    private Game<Hanoi> game;

    /**
     * NMCS algorithm which is tested
     */
    private NMCS<Hanoi> nmcs;

    /**
     * Tests whether the algorithm finds a solution
     * for hanoi with 3 disks. In this case circle 
     * avoidance is activated. Path Storage is used.
     */
    @Test
    public void nestedCallHanoi3CAPathStorage() {
        hanoi = new Hanoi(3);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(game, new NMCSPathStorage<>(), MemorySavingMode.NONE);
        nmcs.setStartingLevel(3);
        nmcs.setCircleAvoidance(true);
        nmcs.start();
        for (Action a : nmcs.getSequence()) {
            game.applyAction(a);
        }
        assertTrue(game.isTerminal());
        System.out.println("3L3CAPathStorage: " + nmcs.getSequence().size());
        assertTrue(nmcs.getSequence().size() <= 11);
    }

    /**
     * Tests whether the algorithm finds a solution
     * for hanoi with 3 disks. In this case circle
     * avoidance is activated. Game Tree is used.
     */
    @Test
    public void nestedCallHanoi3CAGameTree() {
        hanoi = new Hanoi(3);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(game, new NMCSGameTree<>(game.copy().asRoot()),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(3);
        nmcs.setCircleAvoidance(true);
        nmcs.start();
        for (Action a : nmcs.getSequence()) {
            game.applyAction(a);
        }
        assertTrue(game.isTerminal());
        System.out.println("3L3CAGameTree: " + nmcs.getSequence().size());
        assertTrue(nmcs.getSequence().size() <= 11);
    }

    /**
     * Tests whether the algorithm finds a solution
     * for hanoi with 3 disks using GameTree.
     */
    @Test
    public void nestedCallHanoi3GameTree() {
        hanoi = new Hanoi(3);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(game.copy(), new NMCSGameTree<>(game.copy().asRoot()),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(2);
        nmcs.start();
        for (Action a : nmcs.getSequence()) {
            game.applyAction(a);
        }
        assertTrue(game.isTerminal());
        System.out.println("3L3GameTree: " + nmcs.getSequence().size());
        assertTrue(nmcs.getSequence().size() <= 11);
    }

    /**
     * *Tests whether the algorithm finds a solution
    * for hanoi with 3 disks. Using Path Storage
    */
    @Test
    public void nestedCallHanoi3PathStorage() {
        hanoi = new Hanoi(3);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(game.copy(), new NMCSPathStorage<>(),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(2);
        nmcs.start();
        for (Action a : nmcs.getSequence()) {
            game.applyAction(a);
        }
        assertTrue(game.isTerminal());
        System.out.println("3L3PathStorage: " + nmcs.getSequence().size());
        assertTrue(nmcs.getSequence().size() <= 11);
    }

    /**
     * Tests whether the algorithm finds a solution
     * for hanoi with 4 disks. In this case circle 
     * avoidance is activated.
     */
    @Ignore
    @Test
    public void nestedCallHanoi4GameTree() {
        hanoi = new Hanoi(4);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(game, new NMCSGameTree<>(game.copy().asRoot()),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(2);
        nmcs.start();
        System.out.println("4L2GameTree: " + nmcs.getSequence().size());
    }

    /**
     * Tests whether the algorithm finds a solution
     * for hanoi with 4 disks. In this case circle 
     * avoidance is activated. Game Tree is used
     */
    @Test
    public void nestedCallHanoi4CASPGameTree() {
        hanoi = new Hanoi(4);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(game, new NMCSGameTree<>(game.copy().asRoot()),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(2);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        System.out.println("4L2CASPGameTree: " + nmcs.getSequence().size());
        assertTrue(nmcs.getSequence().size() <= 22);
    }

    /**
     * Tests whether the algorithm finds a solution
     * for hanoi with 4 disks. In this case circle
     * avoidance is activated. Path storage is used.
     */
    @Test
    public void nestedCallHanoi4CASPPathStorage() {
        hanoi = new Hanoi(4);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(game, new NMCSPathStorage<>(), MemorySavingMode.NONE);
        nmcs.setStartingLevel(2);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        System.out.println("4L2CASPPathStorage: " + nmcs.getSequence().size());
        assertTrue(nmcs.getSequence().size() <= 22);
    }

    /**
     * Tests whether the algorithm finds a solution
     * for hanoi with 5 disks. In this case circle 
     * avoidance is activated.
     */
    @Ignore
    @Test
    public void nestedCallHanoi5CAGameTree() {
        hanoi = new Hanoi(5);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(game, new NMCSGameTree<>(game.copy().asRoot()),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(1);
        nmcs.setCircleAvoidance(true);
        nmcs.start();
        System.out.println("5L1CAGameTree: " + nmcs.getSequence().size());
    }

    /**
     * Tests whether a legal solution for Hanoi 7 is found using a level
     * 0 search with circle avoidance and shortest path cut offs.
     * GameTree is used.
     */
    @Test
    public void nestedCallHanoi7SPGameTree() {
        hanoi = new Hanoi(7);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(game, new NMCSGameTree<>(game.copy().asRoot()),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        System.out.println("7L0CASPGameTree: " + nmcs.getSequence().size());
        for (Action action : nmcs.getSequence()) {
            game.applyAction(action);
        }
        assertTrue(game.isTerminal());
    }

    /**
     * Tests whether a legal solution for Hanoi 7 is found using a level
     * 0 search with circle avoidance and shortest path cut offs.
     * Path storage is used.
     */
    @Test
    public void nestedCallHanoi7SPPathStorage() {
        hanoi = new Hanoi(7);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(game, new NMCSPathStorage<>(), MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        System.out.println("7L0CASPPathStorage: " + nmcs.getSequence().size());
        for (Action action : nmcs.getSequence()) {
            game.applyAction(action);
        }
        assertTrue(game.isTerminal());
    }

    /**
     * Tests whether a legal solution for Hanoi 7 is found using a level
     * 0 search with circle avoidance and shortest path cut offs.
     * GameTree is used
     */
    @Test
    public void nestedCallHanoi7SPHeuristicGameTree() {
        hanoi = new Hanoi(7);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new HeuristicNMCS<>(game,
                new NMCSGameTree<>(game.copy().asRoot()),
                game.getUserHeuristic(), MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        System.out.println(
                "7L0CASPHeuristicGameTree: " + nmcs.getSequence().size());
        for (Action action : nmcs.getSequence()) {
            game.applyAction(action);
        }
        assertTrue(game.isTerminal());
    }

    /**
     * Tests whether a legal solution for Hanoi 7 is found using a level
     * 0 search with circle avoidance and shortest path cut offs.
     * Path storage is used
     */
    @Test
    public void nestedCallHanoi7SPHeuristicPathStorage() {
        hanoi = new Hanoi(7);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new HeuristicNMCS<>(game, new NMCSPathStorage<>(),
                game.getUserHeuristic(), MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        System.out.println("7L0CASPPathStorage: " + nmcs.getSequence().size());
        for (Action action : nmcs.getSequence()) {
            game.applyAction(action);
        }
        assertTrue(game.isTerminal());
    }

    /**
     * Tests whether a legal solution for Hanoi 7 is found using a level
     * 0 search with circle avoidance and shortest path cut offs.
     */
    @Test
    public void nestedCallHanoi7SPdynamicHeuristic() {
        hanoi = new Hanoi(7);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new DynamicHeuristicNMCS<>(game,
                new NMCSGameTree<>(game.asRoot()), game.getUserHeuristic(),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        System.out.println(
                "7L0CASPDynamicHeuristic: " + nmcs.getSequence().size());
        for (Action action : nmcs.getSequence()) {
            game.applyAction(action);
        }
        assertTrue(game.isTerminal());
    }

    /**
     * Tests whether the dynamic heuristic version of
     * NMCS finds better solutions for Hanoi with 8 disks
     * then the normal heuristic version of NMCS.
     */
    // is only useful to compare both versions.
    @Ignore
    @Test
    public void hanoi8Level0Heuristic() {
        hanoi = new Hanoi(8);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(game.copy(), new NMCSGameTree<>(game.asRoot()),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        System.out.println("8L0CASP: " + nmcs.getSequence().size());
        nmcs = new HeuristicNMCS<>(game.copy(),
                new NMCSGameTree<>(game.asRoot()), game.getUserHeuristic(),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        int heuristic = nmcs.getSequence().size();
        System.out.println("8L0CASPHeuristic: " + heuristic);
        nmcs = new DynamicHeuristicNMCS<>(game.copy(),
                new NMCSGameTree<>(game.asRoot()), game.getUserHeuristic(),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        int dynamicHeuristic = nmcs.getSequence().size();
        System.out.println("8L0CASPDynamicHeuristic: " + dynamicHeuristic);
        // If this works as hoped, the dynamic version of NMCS should find better
        // solutions, i.e. a shorter path.
        assertTrue(dynamicHeuristic < heuristic);
    }

    @Test
    public void gemPuzzleHeuristic() {
        GemPuzzle puzzle = new GemPuzzle(3, 0);
        Game<GemPuzzle> puzzleGame = new Game<>(GPS.wrap(puzzle));
        NMCS<GemPuzzle> nmcs = new HeuristicNMCS<>(puzzleGame.copy(),
                new NMCSGameTree<>(puzzleGame.asRoot()),
                puzzleGame.getUserHeuristic(), MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        int heuristic = nmcs.getSequence().size();
        System.out.println("8L0CASPHeuristic: " + heuristic);
        nmcs = new DynamicHeuristicNMCS<>(puzzleGame.copy(),
                new NMCSGameTree<>(puzzleGame.asRoot()),
                puzzleGame.getUserHeuristic(), MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        int dynamicHeuristic = nmcs.getSequence().size();
        System.out.println("8L0CASPDynamicHeuristic: " + dynamicHeuristic);
    }

    /**
     * Tests whether the algorithm finds a solution
     * for hanoi with 5 disks. 
     */
    @Ignore
    @Test
    public void nestedCallHanoi5GameTree() {
        hanoi = new Hanoi(5);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(game, new NMCSGameTree<>(game.copy().asRoot()),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(1);
        nmcs.start();
        System.out.println("5L1: " + nmcs.getSequence().size());
    }

    /**
     * Tests whether a solution determined by NMCS is actually legal.
     * Applies the solution to the game and tests whether a terminal 
     * test is reached.
     */
    @Test
    public void nestedCallSolutionLegalGameTree() {
        hanoi = new Hanoi(5);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(Game.copy(game),
                new NMCSGameTree<>(game.copy().asRoot()),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        for (Action action : nmcs.getSequence()) {
            game.applyAction(action);
        }
        assertTrue(game.isTerminal());
    }

    /**
     * Tests whether a solution determined by NMCS is actually legal.
     * Applies the solution to the game and tests whether a terminal
     * test is reached.
     */
    @Test
    public void nestedCallSolutionLegalPathStorage() {
        hanoi = new Hanoi(5);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new NMCS<>(Game.copy(game), new NMCSPathStorage<>(),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        for (Action action : nmcs.getSequence()) {
            game.applyAction(action);
        }
        assertTrue(game.isTerminal());
    }

    @Test
    public void gemPuzzleLevel0() {
        GemPuzzle puzzle = new GemPuzzle(3, 0);
        Game<GemPuzzle> g = new Game<>(GPS.wrap(puzzle));
        NMCS<GemPuzzle> nmcs = new NMCS<>(g,
                new NMCSGameTree<>(g.copy().asRoot()), MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        System.out.println("gemPuzzleLevel0: " + nmcs.getSequence().size());
    }

    @Test
    public void gemPuzzleLevel0Heuristic() {
        GemPuzzle puzzle = new GemPuzzle(3, 0);
        Game<GemPuzzle> g = new Game<>(GPS.wrap(puzzle));
        NMCS<GemPuzzle> nmcs = new HeuristicNMCS<>(g,
                new NMCSGameTree<>(g.copy().asRoot()), g.getUserHeuristic(),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        System.out.println(
                "gemPuzzleLevel0Heuristic: " + nmcs.getSequence().size());
    }

    @Test
    public void solitaireTestGameTree() {
        Solitaire solitaire = new Solitaire();
        Game<Solitaire> g = new Game<>(GPS.wrap(solitaire));
        //TODO:: Does solitaire work?
        System.out.println(g.getActions());
        NMCS<Solitaire> nmcs = new NMCS<>(g,
                new NMCSGameTree<>(g.copy().asRoot()), MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        System.out.println("GameTree:");
        System.out.println("Sequence size: " + nmcs.getSequence().size());
        System.out.println("Value: " + nmcs.getResult().getValue());
        Game<Solitaire> testGame = g.copy();
        nmcs.getSequence().forEach(testGame::applyAction);
        assertTrue(testGame.isTerminal());
    }

    @Test
    public void solitaireTestPathStorage() {
        Solitaire solitaire = new Solitaire();
        Game<Solitaire> g = new Game<>(GPS.wrap(solitaire));
        //TODO:: Does solitaire work?
        System.out.println(g.getActions());
        NMCS<Solitaire> nmcs = new NMCS<>(g, new NMCSPathStorage<>(),
                MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        nmcs.start();
        System.out.println("PathStorage:");
        System.out.println("Sequence size: " + nmcs.getSequence().size());
        System.out.println("Value: " + nmcs.getResult().getValue());
        Game<Solitaire> testGame = g.copy();
        nmcs.getSequence().forEach(testGame::applyAction);
        assertTrue(testGame.isTerminal());
    }

    @Test
    public void terminateOnFoundSolutionGameTree() {
        hanoi = new Hanoi(8);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new HeuristicNMCS<>(game,
                new NMCSGameTree<>(game.copy().asRoot()),
                game.getUserHeuristic(), MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        //terminate on found solution:
        nmcs.terminateOnFoundSolution(true);
        nmcs.start();
        // Remember the depth of the solution:
        int s1 = nmcs.getSequence().size();
        System.out.println("Terminate on found solution (1): "
                + nmcs.getSequence().size());
        Game<Hanoi> testGame = game.copy();
        for (Action action : nmcs.getSequence()) {
            testGame.applyAction(action);
        }
        assertTrue(testGame.isTerminal());
        //continue:
        nmcs.start();
        int s2 = nmcs.getSequence().size();
        System.out.println("Terminate on found solution (2): "
                + nmcs.getSequence().size());
        testGame = game.copy();
        for (Action action : nmcs.getSequence()) {
            testGame.applyAction(action);
        }
        assertTrue(testGame.isTerminal());
        //The second solution should be better than the first one:
        assertTrue(s2 < s1);
    }

    @Test
    public void terminateOnFoundSolutionPathStorage() {
        hanoi = new Hanoi(8);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new HeuristicNMCS<>(game, new NMCSPathStorage<>(),
                game.getUserHeuristic(), MemorySavingMode.NONE);
        nmcs.setStartingLevel(0);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        //terminate on found solution:
        nmcs.terminateOnFoundSolution(true);
        nmcs.start();
        // Remember the depth of the solution:
        int s1 = nmcs.getSequence().size();
        System.out.println("Terminate on found solution (1): "
                + nmcs.getSequence().size());
        Game<Hanoi> testGame = game.copy();
        for (Action action : nmcs.getSequence()) {
            testGame.applyAction(action);
        }
        assertTrue(testGame.isTerminal());
        //continue:
        nmcs.start();
        int s2 = nmcs.getSequence().size();
        System.out.println("Terminate on found solution (2): "
                + nmcs.getSequence().size());
        testGame = game.copy();
        for (Action action : nmcs.getSequence()) {
            testGame.applyAction(action);
        }
        assertTrue(testGame.isTerminal());
        //The second solution should be better than the first one:
        assertTrue(s2 < s1);
    }

    @Test
    public void terminateOnFoundSolutionGameTreeHigherLevel() {
        hanoi = new Hanoi(8);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new HeuristicNMCS<>(game,
                new NMCSGameTree<>(game.copy().asRoot()),
                game.getUserHeuristic(), MemorySavingMode.NONE);
        nmcs.setStartingLevel(3);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        //terminate on found solution:
        nmcs.terminateOnFoundSolution(true);
        nmcs.start();
        // Remember the depth of the solution:
        int s1 = nmcs.getSequence().size();
        System.out.println("Terminate on found solution (1): "
                + nmcs.getSequence().size());
        Game<Hanoi> testGame = game.copy();
        for (Action action : nmcs.getSequence()) {
            testGame.applyAction(action);
        }
        assertTrue(testGame.isTerminal());
        //continue:
        nmcs.start();
        int s2 = nmcs.getSequence().size();
        System.out.println("Terminate on found solution (2): "
                + nmcs.getSequence().size());
        testGame = game.copy();
        for (Action action : nmcs.getSequence()) {
            testGame.applyAction(action);
        }
        assertTrue(testGame.isTerminal());
        //The second solution should be better than the first one:
        assertTrue(s2 < s1);
    }

    @Test
    public void terminateOnFoundSolutionPathStorageHigherLevel() {
        hanoi = new Hanoi(8);
        game = new Game<>(GPS.wrap(hanoi));
        nmcs = new HeuristicNMCS<>(game, new NMCSPathStorage<>(),
                game.getUserHeuristic(), MemorySavingMode.NONE);
        nmcs.setStartingLevel(3);
        nmcs.setCircleAvoidance(true);
        nmcs.setShortestPath(true);
        //terminate on found solution:
        nmcs.terminateOnFoundSolution(true);
        nmcs.start();
        // Remember the depth of the solution:
        int s1 = nmcs.getSequence().size();
        System.out.println("Terminate on found solution (1): "
                + nmcs.getSequence().size());
        Game<Hanoi> testGame = game.copy();
        for (Action action : nmcs.getSequence()) {
            testGame.applyAction(action);
        }
        assertTrue(testGame.isTerminal());
        //continue:
        nmcs.start();
        int s2 = nmcs.getSequence().size();
        System.out.println("Terminate on found solution (2): "
                + nmcs.getSequence().size());
        testGame = game.copy();
        for (Action action : nmcs.getSequence()) {
            testGame.applyAction(action);
        }
        assertTrue(testGame.isTerminal());
        //The second solution should be better than the first one:
        assertTrue(s2 < s1);
    }
}