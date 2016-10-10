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
package benchmarks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import butt.tool.benchmark.IBenchmarkRunner;
import butt.tool.benchmark.IRunnableBenchmark;
import butt.tool.benchmark.common.GameHelper;
import game.connect4.ConnectGame;
import gps.GPS;
import gps.games.GamesModule;
import gps.games.wrapper.Game;
import gps.util.Tuple;

/**
 * Benchmarks for the Connect game
 * 
 * @author haker@uni-bremen.de
 *
 */
public class BenchmarkConnect implements IBenchmarkRunner<ConnectGame> {

    private static class D {
        public D(int pPlayers, int pFieldWidth, int pFieldHeight,
                int pWinStreakLen) {
            players = pPlayers;
            fieldWidth = pFieldWidth;
            fieldHeight = pFieldHeight;
            winStreakLen = pWinStreakLen;
        }

        int players;
        int fieldWidth;
        int fieldHeight;
        int winStreakLen;
    }

    @Override
    public List<IRunnableBenchmark<ConnectGame>> getRunners() {
        ArrayList<IRunnableBenchmark<ConnectGame>> list = new ArrayList<IRunnableBenchmark<ConnectGame>>();
        streamHelper().forEach(tuple -> list.addAll(GameHelper.gameBenchmarks(
                new GamesModule<>(tuple.getY()), tuple.getX())));
        return list;

    }

    /**
     * Stream of tuples that represent tuples of problem description and game
     * instance.
     * 
     * @return The tuple.
     */
    private static Stream<Tuple<String, Game<ConnectGame>>> streamHelper() {
        return Stream.of(new D(2, 7, 6, 4), new D(2, 19, 19, 6))
                .map(data -> new Tuple<String, Game<ConnectGame>>(
                        "ConnectGame(Players:" + data.players + ";"
                                + data.fieldWidth + "x" + data.fieldHeight
                                + ";Streak:" + data.winStreakLen + ")",
                        new Game<>(GPS.wrap(new ConnectGame(data.players,
                                data.fieldWidth, data.fieldHeight,
                                data.winStreakLen)))));
    }

    @Override
    public Stream<Game<ConnectGame>> getProblemInstancesStream() {
        return streamHelper().map(Tuple::getY);
    }

}
