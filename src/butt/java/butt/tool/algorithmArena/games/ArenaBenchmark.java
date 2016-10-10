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
package butt.tool.algorithmArena.games;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.collect.Collections2;

import gps.games.wrapper.Game;
import gps.games.wrapper.Player;

/**
 * By using {@link GameAlgorithmBattle} this class allows to let the algorithms
 * play multiple times against each other.
 * 
 * @author igor@uni-bremen.de
 *
 * @param <T>
 *            The type of the game
 */
public class ArenaBenchmark<T> {

    /**
     * The logger of this class.
     */
    private final static Logger LOGGER = Logger
            .getLogger(ArenaBenchmark.class.getCanonicalName());

    /**
     * Stores the algorithm and the associated score.
     */
    private final Map<GameAlgorithmWithInfo, Integer> scoreboard = new LinkedHashMap<>();

    /**
     * The game which will be played
     */
    private final Game<T> game;

    /**
     * This map associate players with algorithms.
     */
    private Map<Player, GameAlgorithmWithInfo> algorithms;

    /**
     * The initial order for the first round of each game.
     */
    private ArrayList<GameAlgorithmWithInfo> gameBeginOrder;

    /**
     * All possible combinations of {@link #gameBeginOrder}.
     */
    private Collection<List<GameAlgorithmWithInfo>> allGameOrders;

    /**
     * Caching used combinations of {@link #gameBeginOrder}.
     */
    private Collection<List<GameAlgorithmWithInfo>> usedGameOrders;

    /**
     * A counter which is only used for printing purposes.
     */
    private int counter;

    /**
     * Creates a new instance for a specific {@link Game}. Needs a {@link Map}
     * where {@link Player} are associated with {@link GameAlgorithmWithInfo}
     * for simulating a battle against (different) algorithms.
     * <p>
     * Adds also all {@link GameAlgorithmWithInfo} from {@link #algorithms} to
     * the {@link #scoreboard} to ensure that even the worst algorithm shows up
     * if you call {@link #getScoreboard()}.
     * 
     * @param pGame
     *            the game which will be played.
     * @param pAlgorithms
     *            a {@link Map} of players and algorithms.
     */
    public ArenaBenchmark(final Game<T> pGame,
            final Map<Player, GameAlgorithmWithInfo> pAlgorithms) {
        if (pGame == null || pAlgorithms == null || pAlgorithms.size() < 2) {
            throw new IllegalArgumentException();
        }
        game = pGame;
        algorithms = pAlgorithms;

        for (Entry<Player, GameAlgorithmWithInfo> entry : algorithms
                .entrySet()) {
            scoreboard.put(entry.getValue(), 0);
        }

        gameBeginOrder = new ArrayList<>(algorithms.values());
    }

    /**
     * Let the algorithms play against each other multiple times.
     * 
     * @param numberOfGames
     *            number, how often the game has to be played
     */
    public void battlePhase(final int numberOfGames) {
        if (numberOfGames < 1) {
            throw new IllegalArgumentException("The number of games must be "
                    + "greater then zero to begin a battle.");
        }

        for (int i = 1; i <= numberOfGames; i++) {
            final Map<Player, GameAlgorithmWithInfo> playOrder = new LinkedHashMap<>();

            final ArrayList<Player> players = new ArrayList<>(
                    algorithms.keySet());

            final List<GameAlgorithmWithInfo> order = getNextGameOrder();

            for (int n = 0; n < players.size(); n++) {
                playOrder.put(players.get(n), order.get(n));
            }

            final Game<T> newGame = game.copy();
            final GameAlgorithmBattle<T> arena = new GameAlgorithmBattle<>(
                    newGame, playOrder);

            arena.battle();
            Player winner = null;

            for (Player player : algorithms.keySet()) {
                if (winner == null || (newGame.getUtility(winner)
                        .intValue() <= newGame.getUtility(player).intValue())) {
                    winner = player;
                }
            }

            GameAlgorithmWithInfo winningAlgorithm = playOrder.get(winner);

            updateScore(winningAlgorithm);
        }

        printScoreboard();
    }

    /**
     * Returns {@link #scoreboard} which stores {@link GameAlgorithmWithInfo}
     * and his score.
     * 
     * @return the {@link #scoreboard}
     */
    public Map<GameAlgorithmWithInfo, Integer> getScoreboard() {
        return scoreboard;
    }

    /**
     * Returns participating {@link GameAlgorithmWithInfo}.
     * 
     * @return a {@link Map} where players are associated with algorithms
     */
    public Map<Player, GameAlgorithmWithInfo> getAlgorithms() {
        return algorithms;
    }

    /**
     * Return all possible orders for {@link ArenaBenchmark#gameBeginOrder}.
     * 
     * @return a {@link Collection} with every possible unique {@link List}
     */
    public Collection<List<GameAlgorithmWithInfo>> getAllOrders() {
        return allGameOrders;
    }

    /**
     * Returns the currently first from {@link #scoreboard}. Can't be null
     * because the constructor already copies the map of
     * {@link ArenaBenchmark#algorithms}.
     * 
     * @return the first
     */
    public GameAlgorithmWithInfo getWinner() {
        return scoreboard.entrySet().stream()
                .sorted(Map.Entry
                        .<GameAlgorithmWithInfo, Integer> comparingByValue()
                        .reversed())
                .collect(Collectors.toList()).get(0).getKey();
    }

    /**
     * This method can return every possible combination of items in
     * {@link #gameBeginOrder}.
     * 
     * @return a {@link List} with different begin order.
     */
    public List<GameAlgorithmWithInfo> getNextGameOrder() {
        if (allGameOrders == null) {
            usedGameOrders = new ArrayList<>();
            allGameOrders = Collections2.permutations(gameBeginOrder);
        }

        List<GameAlgorithmWithInfo> nextGameOrder = null;

        for (List<GameAlgorithmWithInfo> order : allGameOrders) {
            if (!usedGameOrders.contains(order)) {
                nextGameOrder = order;
                usedGameOrders.add(order);
                break;
            }
            usedGameOrders = new ArrayList<>();
        }

        return nextGameOrder;

    }

    /**
     * Add one win for a specific {@link GameAlgorithmWithInfo} to the
     * scoreboard.
     * 
     * @param algorithm
     *            the winning algorithm.
     */
    private void updateScore(final GameAlgorithmWithInfo algorithm) {
        if (scoreboard.containsKey(algorithm)) {
            scoreboard.put(algorithm, scoreboard.get(algorithm) + 1);
        } else {
            scoreboard.put(algorithm, 1);
        }
    }

    /**
     * Prints the result after {@link #battlePhase(int)} ended.
     */
    public void printScoreboard() {
        scoreboard.entrySet().stream()
                .sorted(Map.Entry
                        .<GameAlgorithmWithInfo, Integer> comparingByValue()
                        .reversed())
                .forEachOrdered(
                        entry -> LOGGER.info("Place " + this.counter() + ": "
                                + entry.getKey().getAlgorithmClass()
                                        .getSimpleName()
                                + " (Wins: " + entry.getValue() + ")"));
    }

    /**
     * Helper method for {@link #printScoreboard()} the correct place of each
     * {@link GameAlgorithmWithInfo} since {@link LinkedHashMap} indexing works
     * different.
     * 
     * @return the counter
     */
    private int counter() {
        if (counter > scoreboard.size()) {
            counter = 0;
        }
        return ++counter;
    }

}
