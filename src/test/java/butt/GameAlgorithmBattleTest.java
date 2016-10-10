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

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import butt.tool.algorithmArena.games.GameAlgorithmBattle;
import butt.tool.algorithmArena.games.GameAlgorithmWithInfo;
import game.connect4.ConnectGame;
import gps.GPS;
import gps.games.algorithm.alphabetapruning.AlphaBetaPruning;
import gps.games.algorithm.mtdf.MTDf;
import gps.games.wrapper.Game;
import gps.games.wrapper.Player;

/**
 * @author alueck@uni-bremen.de
 */
public class GameAlgorithmBattleTest {

    @Test
    public void test() {
        Game<ConnectGame> game = new Game<>(
                GPS.wrap(ConnectGame.createConnect4()));

        Map<Player, GameAlgorithmWithInfo> map = new LinkedHashMap<>();
        map.put(new Player(0), new GameAlgorithmWithInfo(AlphaBetaPruning.class,
                new Object[] {}, -1, 6));
        map.put(new Player(1),
                new GameAlgorithmWithInfo(MTDf.class, new Object[] {}, -1, 8));
        GameAlgorithmBattle<ConnectGame> battle = new GameAlgorithmBattle<>(
                game, map);
        battle.battle();
    }
}
