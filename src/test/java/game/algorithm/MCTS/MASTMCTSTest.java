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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import game.connect4.ConnectGame;
import gps.GPS;
import gps.games.GamesModule;
import gps.games.algorithm.monteCarloTreeSearch.AbstractMCTS;
import gps.games.algorithm.monteCarloTreeSearch.MASTMCTS;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;

/**
 * Functionality test for the MAST extension of MCTS.
 *
 * @author igor@uni-bremen.de
 */
public class MASTMCTSTest {

    /**
     * Initializes a new UCTMCTS with a game of connect4 before each test.
     */
    @Before
    public void init() {
        // Nothing to initialize yet.
    }

    @Test
    public void testCorrectListSort() {

        MASTMCTS<ConnectGame> instance = new MASTMCTS<>(
                new GamesModule<ConnectGame>(
                        new Game<>(GPS.wrap(ConnectGame.createConnect4()))));
        ArrayList<Action> actions = new ArrayList<>();

        Action a1 = new Action(1, "a1");
        Action a2 = new Action(2, "a2");
        Action a3 = new Action(3, "a3");

        actions.add(a1);
        actions.add(a2);
        actions.add(a3);

        instance.setScore(a1, 100.0, 3);
        instance.setScore(a2, 100.0, 2);
        instance.setScore(a3, 100.0, 1);

        instance.getNextPossibleAction(actions);

        // in this test its not interesting which action will be used
        // because it's random anyways. important is the correct sorting
        assertEquals(actions.indexOf(a3), 0);
        assertEquals(actions.indexOf(a2), 1);
        assertEquals(actions.indexOf(a1), 2);
    }

    /**
     * Starts the algorithm
     */
    // TODO:: Fails right now and should be fixed in this branch.
    @Test
    public void startAlgorithm() {
        AbstractMCTS<ConnectGame> mcts = new MASTMCTS<>(
                new GamesModule<ConnectGame>(
                        new Game<>(GPS.wrap(ConnectGame.createConnect4()))));
        mcts.setTimelimit(100);
        mcts.start();
    }

}