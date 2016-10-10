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
package game.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import game.hanoi.Hanoi;
import game.pathfinder.Maze;
import gps.GPS;
import gps.games.MemorySavingMode;
import gps.games.util.GameTree;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import gps.games.wrapper.successor.INode;

/**
 * Various tests on the gameTree
 * 
 * @author jschloet@tzi.de
 *
 */
public class GameTreeTest {

    @Test
    public void getPathToRootMaze() {
        Game<Maze> game = new Game<Maze>(
                GPS.wrap(new Maze("pathfinding/maze.bmp")));
        GameTree<Maze> gameTree = new GameTree<>();
        //Constructing a gameTree:
        gameTree.insertRoot(game.copy().asRoot());
        assertTrue(gameTree.getPathTo(game.copy().asRoot()).size() == 1);
    }

    @Test
    public void getPathToRootHanoi() {
        Game<Hanoi> game = new Game<Hanoi>(GPS.wrap(new Hanoi(18)));
        GameTree<Hanoi> gameTree = new GameTree<>();
        //Constructing a gameTree:
        gameTree.insertRoot(game.copy().asRoot());
        assertTrue(gameTree.getPathTo(game.copy().asRoot()).size() == 1);
    }

    @Test
    public void getPathToAsActions() {
        Game<Hanoi> game = new Game<Hanoi>(GPS.wrap(new Hanoi(5)));
        Game<Hanoi> testGame = game.copy();
        GameTree<Hanoi> gameTree = new GameTree<Hanoi>();
        //Constructing a gameTree:
        gameTree.insertRoot(game.copy().asRoot());
        INode<Hanoi> root = game.copy().asRoot();
        INode<Hanoi> prev = root;
        INode<Hanoi> succ = root.getSuccessors(MemorySavingMode.NONE).get(1);
        gameTree.insert(prev, succ);
        prev = succ;
        succ = succ.getSuccessors(MemorySavingMode.NONE).get(4);
        gameTree.insert(prev, succ);
        prev = succ;
        succ = succ.getSuccessors(MemorySavingMode.NONE).get(1);
        gameTree.insert(prev, succ);
        INode<Hanoi> goalState = succ;
        //Right now everything is fine:
        gameTree.getPathTo(goalState).forEach(g -> System.out.println(g));
        System.out.println("----------------------------------------");
        for (Action a : gameTree.getPathToAsActions(goalState)) {
            testGame.applyAction(a);
        }
        assertEquals(testGame, goalState.getGame());
        testGame = game.copy();
        // Now insert a better path:
        prev = root;
        succ = root.getSuccessors(MemorySavingMode.NONE).get(2);
        gameTree.insert(prev, succ);
        // The path is still fine
        gameTree.getPathTo(goalState).forEach(g -> System.out.println(g));
        // But the action sequence is not:
        for (Action a : gameTree.getPathToAsActions(goalState)) {
            testGame.applyAction(a);
        }
        assertEquals(testGame, goalState.getGame());
    }

    @Test
    public void lastActionTest() {
        Game<Hanoi> game = new Game<Hanoi>(GPS.wrap(new Hanoi(3)));
        Game<Hanoi> testGame = game.copy();
        GameTree<Hanoi> gameTree = new GameTree<Hanoi>();
        gameTree.insertRoot(game.copy().asRoot());
        INode<Hanoi> root = game.copy().asRoot();
        INode<Hanoi> prev = root;
        INode<Hanoi> succ = root.getSuccessors(MemorySavingMode.NONE).get(2);
        //Build a path to a terminal
        gameTree.insert(prev, succ);
        prev = succ;
        succ = succ.getSuccessors(MemorySavingMode.NONE).get(1);
        gameTree.insert(prev, succ);
        prev = succ;
        succ = succ.getSuccessors(MemorySavingMode.NONE).get(4);
        gameTree.insert(prev, succ);
        prev = succ;
        succ = succ.getSuccessors(MemorySavingMode.NONE).get(1);
        gameTree.insert(prev, succ);
        prev = succ;
        succ = succ.getSuccessors(MemorySavingMode.NONE).get(0);
        gameTree.insert(prev, succ);
        prev = succ;
        succ = succ.getSuccessors(MemorySavingMode.NONE).get(4);
        INode<Hanoi> temp = succ;
        gameTree.insert(prev, succ);
        prev = succ;
        succ = succ.getSuccessors(MemorySavingMode.NONE).get(1);
        gameTree.insert(prev, succ);
        prev = succ;
        succ = succ.getSuccessors(MemorySavingMode.NONE).get(2);
        INode<Hanoi> goalState = succ;
        // The solution is correct:
        gameTree.insert(prev, succ);
        for (Action a : gameTree.getPathToAsActions(goalState)) {
            testGame.applyAction(a);
        }
        assertEquals(testGame, goalState.getGame());
        // find a better terminal (with a lower depth):
        prev = temp;
        succ = temp.getSuccessors(MemorySavingMode.NONE).get(2);
        gameTree.insert(prev, succ);
        testGame = game.copy();
        // Search for the path to the old terminal:
        try {
            for (Action a : gameTree.getPathToAsActions(goalState)) {
                testGame.applyAction(a);
            }
        } catch (Exception e) {
            fail();
            // The path is not legal, as the last action is the incomming of goalState
            // instead of the incomming of succ 
        }
        // If we search the path to succ (which is the new better terminal)
        // everything is fine:
        testGame = game.copy();
        for (Action a : gameTree.getPathToAsActions(succ)) {
            testGame.applyAction(a);
        }
        assertEquals(testGame, succ.getGame());
    }
}
