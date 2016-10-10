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
package game.algorithm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import game.connect4.ConnectGame;
import game.hanoi.Hanoi;
import gps.GPS;
import gps.ResultEnum;
import gps.common.AlgorithmUtility;
import gps.games.GamesModule;
import gps.games.algorithm.AbstractGameAlgorithm;

import gps.games.algorithm.heuristic.DeltaHeuristic;
import gps.games.algorithm.monteCarloTreeSearch.UCTMCTSPruning;
import gps.games.algorithm.singleplayer.AStar;
import gps.games.algorithm.singleplayer.common.datastruct.ToEvalSortedList;
import gps.games.wrapper.Game;

public class ApplicableAlgorithmTest {

    @Test
    public void testApplicableiterative() {
        // Check whether iterativeNMCS is not included in the algorithms for connect4:
        ConnectGame cg = ConnectGame.createConnect4();
        Game<ConnectGame> game = new Game<>(GPS.wrap(cg));
        GamesModule<ConnectGame> module = new GamesModule<ConnectGame>(game);
        module.gameAnalysis();
        @SuppressWarnings("unchecked")
        List<AbstractGameAlgorithm<ConnectGame>> list1 = (List<AbstractGameAlgorithm<ConnectGame>>) AlgorithmUtility
                .getApplicableAlgorithms(AbstractGameAlgorithm.class,
                        ResultEnum.MOVES, module);
        assertTrue(list1.stream().filter(a -> a.getName().contains("NMCS"))
                .collect(Collectors.toList()).isEmpty());
        // Check whether iterativeNMCS is included in the algorithms for hanoi:
        Hanoi h = new Hanoi(3);
        Game<Hanoi> g = new Game<>(GPS.wrap(h));
        GamesModule<Hanoi> moduleH = new GamesModule<Hanoi>(g);
        moduleH.gameAnalysis();
        @SuppressWarnings("unchecked")
        List<AbstractGameAlgorithm<Hanoi>> list2 = (List<AbstractGameAlgorithm<Hanoi>>) AlgorithmUtility
                .getApplicableAlgorithms(AbstractGameAlgorithm.class,
                        ResultEnum.MOVES, moduleH);
        assertFalse(list2.stream().filter(a -> a.getName().contains("NMCS"))
                .collect(Collectors.toList()).isEmpty()); // must not be empty
    }

    @Test
    public void testApplicableHanoi() {
        Hanoi h = new Hanoi(3);
        Game<Hanoi> g = new Game<>(GPS.wrap(h));
        GamesModule<Hanoi> module = new GamesModule<>(g);
        module.gameAnalysis();
        @SuppressWarnings("unchecked")
        List<AbstractGameAlgorithm<Hanoi>> l = (List<AbstractGameAlgorithm<Hanoi>>) AlgorithmUtility
                .getApplicableAlgorithms(AbstractGameAlgorithm.class,
                        ResultEnum.MOVES, module);
        assertFalse(l.isEmpty()); // must not be empty
    }

    @Test
    public void getConstructorAStar() {
        Game<Hanoi> game = new Game<>(GPS.wrap(new Hanoi(4)));
        AStar<Hanoi> astar = new AStar<>(new GamesModule<>(game));
        assertTrue(AlgorithmUtility.findCompatibleConstructor(astar.getClass(),
                GamesModule.class, DeltaHeuristic.class,
                ToEvalSortedList.class.getClass()) != null);
    }

    @Test
    public void getConstructorAStarWithNull() {
        Game<Hanoi> game = new Game<>(GPS.wrap(new Hanoi(4)));
        AStar<Hanoi> astar = new AStar<>(new GamesModule<>(game));
        assertTrue(AlgorithmUtility.findCompatibleConstructor(astar.getClass(),
                GamesModule.class, null,
                ToEvalSortedList.class.getClass()) != null);
    }

    @Test
    public void getConstructorMCTSPruning() {
        Game<ConnectGame> game = new Game<>(
                GPS.wrap(ConnectGame.createConnect4()));
        UCTMCTSPruning<ConnectGame> pruning = new UCTMCTSPruning<>(
                new GamesModule<>(game));
        assertTrue(AlgorithmUtility.findCompatibleConstructor(
                pruning.getClass(), GamesModule.class, Integer.class) != null);
    }

    @Ignore
    @Test
    public void testApplicableHanoiWithInvalidResult() {
        Hanoi h = new Hanoi(6);
        Game<Hanoi> g = new Game<>(GPS.wrap(h));
        @SuppressWarnings("unchecked")
        List<AbstractGameAlgorithm<Hanoi>> l = (List<AbstractGameAlgorithm<Hanoi>>) AlgorithmUtility
                .getApplicableAlgorithms(AbstractGameAlgorithm.class,
                        ResultEnum.BEST_MOVE, g);
        assertTrue(l.isEmpty()); // should be empty since algorithms do not
                                 // provide best move for singleplayer games
    }
}
