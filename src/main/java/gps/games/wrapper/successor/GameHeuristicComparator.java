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
package gps.games.wrapper.successor;

import gps.games.wrapper.IHeuristic;
import gps.games.wrapper.IHeuristicPlayer;
import gps.games.wrapper.ISingleplayerHeuristic;
import gps.games.wrapper.Player;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A comparator for INodes. Sorts the Nodes by the natural order of the values
 * provided by the heuristic method. Also considers if a game has reached a
 * terminal state and uses the utility method in this case.
 *
 * @author haker@uni-bremen.de, alueck@uni-bremen.de
 */
public class GameHeuristicComparator
        implements Comparator<INode<?>>, Serializable {

    private static final long serialVersionUID = 4533945999520004718L;

    private Player player;

    private final IHeuristic heuristic;

    /**
     * Constructs a new GameHeuristicComparator with the given {@link
     * IHeuristic}. This constructor should be used for singleplayer games.
     *
     * @param pHeuristic
     *         heuristic that will be used for the comparison
     */
    public GameHeuristicComparator(final IHeuristic pHeuristic) {
        heuristic = pHeuristic;
    }

    /**
     * Constructs a new GameHeuristicComparator with the given {@link
     * IHeuristic} and {@link Player}. The heuristic values for the comparison
     * are computed from the given {@link Player}`s viewpoint. So this
     * constructor should be used for multiplayer games.
     *
     * @param pHeuristic
     *         heuristic that will be used for the comparison
     * @param pPlayer
     *         player from whose viewpoint the heuristic should be computed
     */
    public GameHeuristicComparator(final IHeuristic pHeuristic,
            final Player pPlayer) {
        heuristic = pHeuristic;
        player = pPlayer;
    }

    public void setPlayer(final Player player) {
        this.player = player;
    }

    /**
     * Compares the two given nodes and returns values according to {@link
     * Comparator#compare(Object, Object)}.
     * <p>
     * Throws a {@link RuntimeException}, if the game, which should be compared,
     * offers a heuristic method with player argument (i.e. is a multiplayer
     * game), but {@link #heuristic} does not contain a {@link
     * IHeuristicPlayer}. Also throws a {@link RuntimeException}, if the game is
     * a multiplayer game, but {@link #player} is {@code null}.
     *
     * @param o1
     *         node whose game`s heuristic value should be compared to the other
     *         node`s game
     * @param o2
     *         other node
     *
     * @return value less than zero, if the heuristic evaluation of the game of
     * o1 is smaller than o2's, zero if both are equal and a value greater zero,
     * if the heuristic evaluation value of o1`s game is greater than o2`s
     */
    @Override
    public int compare(INode<?> o1, INode<?> o2) {
        //multiplayer game
        if (o1.getGame().hasUserHeuristicPlayerMethod()
                && o2.getGame().hasUserHeuristicPlayerMethod()) {
            if (!(heuristic instanceof IHeuristicPlayer)) {
                throw new RuntimeException(
                        "Heuristic must be of type IHeuristicPlayer, if the "
                                + "game implements a heuristic method with "
                                + "player argument.");
            }
            if (player == null) {
                throw new RuntimeException(
                        "Player cannot be null, if the game offers a utility "
                                + "method with player argument.");
            }
            IHeuristicPlayer playerHeuristic = (IHeuristicPlayer) heuristic;
            if (o1.getGame().hasUtilityPlayerMethod()
                    && o2.getGame().hasUtilityPlayerMethod()) {
                if (o1.getGame().isTerminal() && o2.getGame().isTerminal()) {
                    return Double.compare(
                            o1.getGame().getUtility(player).doubleValue(),
                            o2.getGame().getUtility(player).doubleValue());
                }
                if (o1.getGame().isTerminal() && !o2.getGame().isTerminal()) {
                    return Double.compare(
                            o1.getGame().getUtility(player).doubleValue(),
                            playerHeuristic.eval(o2.getGame(), player));
                }
                if (!o1.getGame().isTerminal() && o2.getGame().isTerminal()) {
                    return Double.compare(
                            playerHeuristic.eval(o1.getGame(), player),
                            o2.getGame().getUtility(player).doubleValue());
                }
            }
            return Double.compare(playerHeuristic.eval(o1.getGame(), player),
                    playerHeuristic.eval(o2.getGame(), player));
        }
        //singleplayer game
        ISingleplayerHeuristic spHeuristic = (ISingleplayerHeuristic) heuristic;
        if (o1.getGame().hasUtilityMethod()
                && o2.getGame().hasUtilityMethod()) {
            if (o1.getGame().isTerminal() && o2.getGame().isTerminal()) {
                return Double.compare(o1.getGame().getUtility().doubleValue(),
                        o2.getGame().getUtility().doubleValue());
            }
            if (o1.getGame().isTerminal() && !o2.getGame().isTerminal()) {
                return Double.compare(o1.getGame().getUtility().doubleValue(),
                        spHeuristic.eval(o2.getGame()));
            }
            if (!o1.getGame().isTerminal() && o2.getGame().isTerminal()) {
                return Double.compare(spHeuristic.eval(o1.getGame()),
                        o2.getGame().getUtility().doubleValue());
            }
        }
        return Double.compare(spHeuristic.eval(o1.getGame()),
                spHeuristic.eval(o2.getGame()));
    }
}
