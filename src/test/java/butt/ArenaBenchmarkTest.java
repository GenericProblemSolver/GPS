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
package butt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.math.BigIntegerMath;

import butt.tool.algorithmArena.games.ArenaBenchmark;
import butt.tool.algorithmArena.games.GameAlgorithmWithInfo;
import game.connect4.ConnectGame;
import gps.GPS;
import gps.games.algorithm.alphabetapruning.AlphaBetaPruning;
import gps.games.algorithm.mtdf.MTDf;
import gps.games.wrapper.Game;
import gps.games.wrapper.Player;

/**
 * @author alueck@uni-bremen.de
 * @author igor@uni-bremen.de
 */
public class ArenaBenchmarkTest {

    private ArenaBenchmark<ConnectGame> arena;

    private final GameAlgorithmWithInfo mtdf = new GameAlgorithmWithInfo(
            MTDf.class, new Object[] {}, -1, 8);

    private final GameAlgorithmWithInfo ab = new GameAlgorithmWithInfo(
            AlphaBetaPruning.class, new Object[] {}, -1, 6);

    @Before
    public void init() {
        Game<ConnectGame> game = new Game<>(
                GPS.wrap(ConnectGame.createConnect4()));
        Map<Player, GameAlgorithmWithInfo> map = new LinkedHashMap<>();
        map.put(new Player(0), ab);
        map.put(new Player(1), mtdf);
        arena = new ArenaBenchmark<>(game, map);
    }

    @Test
    public void testScoreBeforeBattle() {
        assertNotNull(arena.getWinner());
        assertTrue(arena.getScoreboard().containsKey(arena.getWinner()));
        assertTrue(arena.getScoreboard().containsKey(mtdf));
    }

    @Test
    public void testBattle() {
        final int timesPlayed = 3;
        // check for a game with 2 different players
        // if each second round a specific algorithm is first
        for (int i = 0; i < timesPlayed; i++) {
            arena.battlePhase(1); // play only once and check order
            ArrayList<List<GameAlgorithmWithInfo>> algorithmOrder = new ArrayList<>(
                    arena.getAllOrders());
            if (i % 2 == 1) {
                assertEquals(
                        algorithmOrder.get(i % algorithmOrder.size()).get(0),
                        mtdf);
            } else {
                assertEquals(
                        algorithmOrder.get(i % algorithmOrder.size()).get(0),
                        ab);
            }
            assertEquals(mtdf, arena.getWinner());
        }
    }

    @Test
    public void testPermutation() {
        Game<ConnectGame> game2 = new Game<>(
                GPS.wrap(ConnectGame.createConnect6()));
        Map<Player, GameAlgorithmWithInfo> map = new LinkedHashMap<>();
        map.put(new Player(0), new GameAlgorithmWithInfo(AlphaBetaPruning.class,
                new Object[] {}, -1, 6));
        map.put(new Player(1), mtdf);
        map.put(new Player(2), new GameAlgorithmWithInfo(AlphaBetaPruning.class,
                new Object[] {}, -1, 6));
        map.put(new Player(3), new GameAlgorithmWithInfo(AlphaBetaPruning.class,
                new Object[] {}, -1, 6));
        arena = new ArenaBenchmark<>(game2, map);
        arena.getNextGameOrder();
        assertTrue(BigIntegerMath.factorial(map.size()).intValue() == arena
                .getAllOrders().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPlayerCountLowerThan2() {
        Game<ConnectGame> game3 = new Game<>(
                GPS.wrap(ConnectGame.createConnect6()));
        Map<Player, GameAlgorithmWithInfo> map = new LinkedHashMap<>();
        map.put(new Player(1), mtdf);
        arena = new ArenaBenchmark<>(game3, map);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullParameters() {
        arena = new ArenaBenchmark<>(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBattlePhase() {
        arena.battlePhase(0);
    }

}
