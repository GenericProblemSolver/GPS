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
package games.gdl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.ggp.base.util.gdl.factory.GdlFactory;
import org.ggp.base.util.gdl.factory.exceptions.GdlFormatException;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.symbol.factory.exceptions.SymbolFormatException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gps.games.gdl.StanfordGDLGame;
import gps.games.gdl.StanfordGDLGame.PlayerSwitchingMode;
import gps.games.wrapper.Action;
import gps.games.wrapper.Player;

public class StanfordGameTest {
    private StateMachine sm;

    private String testFile = "\r\n" + "    (role white)\r\n"
            + "    (role black)\r\n" + "\r\n"
            + "    (<= (base (cell ?m ?n x)) (index ?m) (index ?n))\r\n"
            + "    (<= (base (cell ?m ?n o)) (index ?m) (index ?n))\r\n"
            + "    (<= (base (cell ?m ?n b)) (index ?m) (index ?n))\r\n"
            + "    (base (control white))\r\n"
            + "    (base (control black))\r\n" + "\r\n"
            + "    (<= (input ?r (mark ?m ?n)) (role ?r) (index ?m) (index ?n))\r\n"
            + "    (<= (input ?r noop) (role ?r))\r\n" + "\r\n"
            + "    (index 1)\r\n" + "    (index 2)\r\n" + "    (index 3)\r\n"
            + "\r\n" + "    (init (cell 1 1 b))\r\n"
            + "    (init (cell 1 2 b))\r\n" + "    (init (cell 1 3 b))\r\n"
            + "    (init (cell 2 1 b))\r\n" + "    (init (cell 2 2 b))\r\n"
            + "    (init (cell 2 3 b))\r\n" + "    (init (cell 3 1 b))\r\n"
            + "    (init (cell 3 2 b))\r\n" + "    (init (cell 3 3 b))\r\n"
            + "    (init (control white))\r\n" + "    \r\n" + "        \r\n"
            + "    (<= (legal ?w (mark ?x ?y))\r\n"
            + "        (true (cell ?x ?y b))\r\n"
            + "        (true (control ?w)))\r\n" + "    \r\n"
            + "    (<= (legal white noop)\r\n"
            + "        (true (control black)))\r\n" + "    \r\n"
            + "    (<= (legal black noop)\r\n"
            + "        (true (control white)))\r\n" + "    \r\n" + "\r\n"
            + "    (<= (next (cell ?m ?n x))\r\n"
            + "        (does white (mark ?m ?n))\r\n"
            + "        (true (cell ?m ?n b)))\r\n" + "    \r\n"
            + "    (<= (next (cell ?m ?n o))\r\n"
            + "        (does black (mark ?m ?n))\r\n"
            + "        (true (cell ?m ?n b)))\r\n" + "    \r\n"
            + "    (<= (next (cell ?m ?n ?w))\r\n"
            + "        (true (cell ?m ?n ?w))\r\n"
            + "        (distinct ?w b))\r\n" + "    \r\n"
            + "    (<= (next (cell ?m ?n b))\r\n"
            + "        (does ?w (mark ?j ?k))\r\n"
            + "        (true (cell ?m ?n b))\r\n"
            + "        (distinct ?m ?j))\r\n" + "    \r\n"
            + "    (<= (next (cell ?m ?n b))\r\n"
            + "        (does ?w (mark ?j ?k))\r\n"
            + "        (true (cell ?m ?n b))\r\n"
            + "        (distinct ?n ?k))\r\n" + "    \r\n"
            + "    (<= (next (control white))\r\n"
            + "        (true (control black)))\r\n" + "    \r\n"
            + "    (<= (next (control black))\r\n"
            + "        (true (control white)))\r\n" + "    \r\n" + "    \r\n"
            + "    (<= (row ?m ?x)\r\n" + "        (true (cell ?m 1 ?x))\r\n"
            + "        (true (cell ?m 2 ?x))\r\n"
            + "        (true (cell ?m 3 ?x)))\r\n" + "    \r\n"
            + "    (<= (column ?n ?x)\r\n" + "        (true (cell 1 ?n ?x))\r\n"
            + "        (true (cell 2 ?n ?x))\r\n"
            + "        (true (cell 3 ?n ?x)))\r\n" + "    \r\n"
            + "    (<= (diagonal ?x)\r\n" + "        (true (cell 1 1 ?x))\r\n"
            + "        (true (cell 2 2 ?x))\r\n"
            + "        (true (cell 3 3 ?x)))\r\n" + "    \r\n"
            + "    (<= (diagonal ?x)\r\n" + "        (true (cell 1 3 ?x))\r\n"
            + "        (true (cell 2 2 ?x))\r\n"
            + "        (true (cell 3 1 ?x)))\r\n" + "    \r\n" + "    \r\n"
            + "    (<= (line ?x) (row ?m ?x))\r\n"
            + "    (<= (line ?x) (column ?m ?x))\r\n"
            + "    (<= (line ?x) (diagonal ?x))\r\n" + "    \r\n" + "    \r\n"
            + "    (<= open (true (cell ?m ?n b)))\r\n" + "    \r\n"
            + "    \r\n" + "    (<= (goal white 100)\r\n"
            + "        (line x)\r\n" + "        (not (line o)))\r\n"
            + "    \r\n" + "    (<= (goal white 50)\r\n"
            + "        (not (line x))\r\n" + "        (not (line o)))\r\n"
            + "    \r\n" + "    (<= (goal white 0)\r\n"
            + "        (not (line x))\r\n" + "        (line o))\r\n" + "\r\n"
            + "    (<= (goal black 100)\r\n" + "        (not (line x))\r\n"
            + "        (line o))\r\n" + "      \r\n"
            + "    (<= (goal black 50)\r\n" + "        (not (line x))\r\n"
            + "        (not (line o)))\r\n" + "  \r\n"
            + "    (<= (goal black 0)\r\n" + "        (line x)\r\n"
            + "        (not (line o)))\r\n" + "    \r\n" + "    \r\n"
            + "    (<= terminal\r\n" + "        (line x))\r\n" + "    \r\n"
            + "    (<= terminal\r\n" + "        (line o))\r\n" + "    \r\n"
            + "    (<= terminal\r\n" + "        (not open))\r\n" + "\r\n";

    private String[] initialMoves = new String[] { "( mark 2 1 )",
            "( mark 2 2 )", "( mark 2 3 )", "( mark 1 1 )", "( mark 1 2 )",
            "( mark 1 3 )", "( mark 3 1 )", "( mark 3 2 )", "( mark 3 3 )" };

    private static Logger logger = LoggerFactory
            .getLogger(StanfordGameTest.class);

    @Before
    public void setUp() throws Exception {
        sm = new ProverStateMachine();
        sm.initialize(GdlFactory.createList(testFile));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testApplyAction() {
        StanfordGDLGame game = new StanfordGDLGame(sm);
        List<Action> actions;
        int turns = 0;
        while (!game.isTerminal()) {
            actions = game.getActions();
            game.applyAction(actions.get(0));
            System.out.println(turns++ + "playing " + actions.get(0));

        }
        assertTrue(turns < 10);

    }

    @Test
    public void testGetActions()
            throws GdlFormatException, SymbolFormatException {
        StanfordGDLGame game = new StanfordGDLGame(sm);
        final List<Action> actions = game.getActions();
        System.out.println("actions: " + actions);
        assertFalse(actions.isEmpty());
        for (final String move : initialMoves) {
            assertTrue(actions.toString().contains(move));
        }
    }

    @Test
    public void testIsTerminal()
            throws GdlFormatException, SymbolFormatException {
        StanfordGDLGame game = new StanfordGDLGame(sm);
        assertFalse(game.isTerminal());
    }

    @Test
    public void testHeuristic() {
        StanfordGDLGame game = new StanfordGDLGame(sm);
        assertTrue(game.hasHeuristicMethod());
        assertEquals(50, game.heuristic());
    }

    @Test
    public void testHeuristicPlayer() {
        StanfordGDLGame game = new StanfordGDLGame(sm);
        Player p1 = game.getPlayer();
        assertTrue(game.hasHeuristicPlayerMethod());
        assertEquals(50, game.heuristic(p1));
    }

    @Test
    public void testGetPlayer() {
        StanfordGDLGame game = new StanfordGDLGame(sm);
        Player p = game.getPlayer();
        assertNotNull(p);
        assertNotNull(p.get());
        logger.debug("player: " + p.get());
        // System.out.println("player: "+p.get());
        assertTrue("white".equals(p.get().toString())
                || "black".equals(p.get().toString()));
    }

    @Test
    public void testGetUtility() {
        StanfordGDLGame game = new StanfordGDLGame(sm);
        assertTrue(game.hasUtilityMethod());
        assertEquals(50, game.getUtility());
        game.setPlayerSwitchingMode(PlayerSwitchingMode.alternate);
        makeMove(game, "( mark 3 1 )");
        makeMove(game, "noop");
        assertEquals(50, game.getUtility());
        makeMove(game, "noop");
        makeMove(game, "( mark 2 2 )");
        assertEquals(50, game.getUtility());
        makeMove(game, "( mark 2 1 )");
        makeMove(game, "noop");
        assertEquals(50, game.getUtility());
        makeMove(game, "noop");
        makeMove(game, "( mark 1 1 )");
        assertEquals(50, game.getUtility());
        makeMove(game, "( mark 2 3 )");
        makeMove(game, "noop");
        assertEquals(50, game.getUtility());
        makeMove(game, "noop");
        makeMove(game, "( mark 3 3 )");
        assertTrue(game.isTerminal());
        assertEquals(0, game.getUtility());
    }

    @Test
    public void testGetUtilityPlayer() {
        StanfordGDLGame game = new StanfordGDLGame(sm);
        Player p1 = game.getPlayer();
        assertTrue(game.hasUtilityPlayerMethod());
        assertEquals(50, game.getUtility(p1));
        game.setPlayerSwitchingMode(PlayerSwitchingMode.alternate);
        makeMove(game, "( mark 3 1 )");
        Player p2 = game.getPlayer();
        makeMove(game, "noop");
        assertEquals(game.getUtility(p1), game.getUtility(p2));
        makeMove(game, "noop");
        makeMove(game, "( mark 2 2 )");
        assertEquals(game.getUtility(p1), game.getUtility(p2));
        makeMove(game, "( mark 2 1 )");
        makeMove(game, "noop");
        assertEquals(game.getUtility(p1), game.getUtility(p2));
        makeMove(game, "noop");
        makeMove(game, "( mark 1 1 )");
        assertEquals(game.getUtility(p1), game.getUtility(p2));
        makeMove(game, "( mark 2 3 )");
        makeMove(game, "noop");
        assertEquals(game.getUtility(p1), game.getUtility(p2));
        makeMove(game, "noop");
        makeMove(game, "( mark 3 3 )");
        assertTrue(game.isTerminal());
        assertEquals(0, game.getUtility(p1));
        assertEquals(100, game.getUtility(p2));
    }

    /**
     * makes a given move in the given game
     * 
     * @param game
     * @param move
     */
    private void makeMove(StanfordGDLGame game, final String move) {
        game.applyAction(move(game.getActions(), move));
        System.out.println("current state: " + game.getCurrentState());
    }

    public Action move(final Collection<Action> actions, final String move) {
        Action result = null;

        for (final Action a : actions) {
            if (a.toString().contains(move)) {
                result = a;
                break;
            }
        }

        assertNotNull(result);
        return result;
    }
}
