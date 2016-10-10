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
package gps.games.wrapper;

import gps.IWrappedProblem;
import gps.attribute.AttributeGraph;
import gps.games.MemorySavingMode;
import gps.games.wrapper.successor.INode;
import gps.games.wrapper.successor.Node;
import gps.util.KryoHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Wraps the game interface provided by the preprocessing process to this class.
 * All algorithms can work with this class.
 *
 * @param <T>
 *         The type of the problem class
 *
 * @author haker@uni-bremen.de
 */
public class Game<T> {

    /**
     * The interface to the problem object.
     */
    private final IWrappedProblem<T> problem;

    /**
     * Construct a game object from a wrapped interface.
     *
     * @param pProblem
     *         the problem.
     */
    public Game(final IWrappedProblem<T> pProblem) {
        if (pProblem == null) {
            throw new IllegalArgumentException("pProblem must not be null");
        }
        problem = pProblem;
    }

    /**
     * Get the Node interface of the game state. The state is is considered the
     * root node of a depth 0. The direct successors are of depth 1 etc.
     */
    public INode<T> asRoot() {
        return new Node<T>(this);
    }

    /**
     * Checks whether the {@link Node#getSuccessors(MemorySavingMode)} method
     * can be called safely.
     *
     * @return {@code true} if the method is available {@code false}
     */
    public boolean hasSuccessorsMethod() {
        return (problem.hasActionMethod() && problem.hasApplyActionMethod())
                || !problem.getRunnableMoves().isEmpty();
    }

    /**
     * Apply an action directly to the current state of the game. Use {@link
     * #getActions()} to get all available actions for the current state. Use
     * {@link #hasActionMethod()} to check whether it is safe to use this
     * method.
     * <p>
     * If you want to get a new instance of the game when the action is applied
     * to this new game then use {@link #getNewGame(Action, MemorySavingMode)}
     * instead.
     *
     * @param pAction
     *         the action to perform.
     */
    public void applyAction(final Action pAction) {
        if (pAction.isDirectMove()) {
            problem.getRunnableMoves().get(pAction.getMoveMethodIdx()).run();
        } else {
            problem.applyAction(pAction);
        }
    }

    /**
     * Construct a new game and apply the given action to the new game. This
     * method improves memory usage over using clone() and applyAction().
     * <p>
     * If you do not need the current game state anymore use {@link
     * #applyAction(Action)}.
     *
     * @param pAction
     *         The action to apply to the copied game.
     * @param memorySavingMode
     *         The memory saving mode that is used when a copy of the game state
     *         is created.
     *
     * @return A new game instance with the action applied on.
     */
    public Game<T> getNewGame(final Action pAction,
            MemorySavingMode memorySavingMode) {
        Game<T> game = copy();
        game.applyAction(pAction);
        switch (memorySavingMode) {
        case ATTRIBUTE_GRAPH:
            memorySavingAttributeGraph(game);
            break;
        case PREPROCESSING:
            memorySavingPreprocessing(game);
            break;
        default:
            break;
        }
        return game;
    }

    private void memorySavingAttributeGraph(final Game<T> game) {
        AttributeGraph ag = new AttributeGraph(this.getProblem());
        ag.matchAllUncached(new AttributeGraph(game.getProblem()), (t, g) -> {
            final Object tv = t.getValue();
            final Object gv = g.getValue();
            // The standard equals implementation for Arrays only checks for
            // equal reference, so a check for content is needed
            if (tv.getClass().isArray() && gv.getClass().isArray()) {
                List<Object> tvl = Arrays.asList(tv);
                List<Object> gvl = Arrays.asList(gv);
                if (Arrays.deepEquals(tvl.toArray(), gvl.toArray())) {
                    g.setValue(tv);
                    return false;
                }
            }
            if (gv != null && tv.equals(gv)) {
                g.setValue(tv);
                return false;
            }
            return true;
        });
    }

    private void memorySavingPreprocessing(final Game<T> game) {
        Object[] attributes = game.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            Object thisAttribute = getAttributes()[i];
            if (attribute instanceof Object[]) {
                if (Arrays.deepEquals((Object[]) attribute,
                        (Object[]) thisAttribute)) {
                    game.setAttribute(i, thisAttribute);
                }
            } else if (attribute.equals(thisAttribute)) {
                game.setAttribute(i, thisAttribute);
            }
        }
    }

    /**
     * Retrieve all actions that can be performed on the current state of the
     * game. Use {@link #applyAction(Action)} to perform an Action to the game.
     * Use {@link #hasActionMethod()} to check whether it is safe to use this
     * method.
     *
     * @return the list of possible actions.
     */
    public List<Action> getActions() {
        final List<Action> result = new ArrayList<>();
        result.addAll(problem.hasActionMethod() ? problem.getActions()
                : new ArrayList<>());
        result.addAll(IntStream.range(0, problem.getRunnableMoves().size())
                .mapToObj(i -> {
                    return new Action(i, "");
                }).collect(Collectors.toList()));
        return result;
    }

    /**
     * Checks whether the {@link #getActions()()} and {@link
     * #applyAction(Action)} method can be called safely.
     *
     * @return {@code true} if the methods are available {@code false} if one of
     * them is not available.
     */

    public boolean hasActionMethod() {
        return (problem.hasActionMethod() && problem.hasApplyActionMethod())
                || !problem.getRunnableMoves().isEmpty();
    }

    /**
     * Checks whether the current state is a terminal state. Use {@link
     * #hasTerminalMethod()} to check whether this method can be called safely.
     *
     * @return {@code true} if the current state is a terminal state {@code
     * false} otherwise.
     */
    public boolean isTerminal() {
        return (problem.hasActionMethod() && problem.getActions().isEmpty())
                || (problem.hasTerminalMethod() && problem.isTerminal());
    }

    /**
     * Checks whether the {@link #isTerminal()} method can be called safely.
     *
     * @return {@code true} if the method is available or {@code false} if not
     */
    public boolean hasTerminalMethod() {
        return problem.hasTerminalMethod() || problem.hasActionMethod();
    }

    /**
     * Returns the heuristic that has been specified in the problem class. Use
     * {@link #hasUserHeuristicMethod()} to check whether the heuristic can be
     * used safely.
     *
     * @return the heuristic
     */
    public ISingleplayerHeuristic getUserHeuristic() {
        return (pGame) -> pGame.problem.heuristic().doubleValue();
    }

    /**
     * Returns the heuristic method that has been specified in the problem
     * class. Use {@link #hasUserHeuristicMethod()} and {@link
     * #hasPlayerMethod()} to check whether the heuristic can be used safely.
     *
     * @return the multiplayer heuristic
     */
    public IHeuristicPlayer getUserHeuristicMultiplayer() {
        return (pGame, pPlayer) -> pGame.problem.heuristic(pPlayer)
                .doubleValue();
    }

    /**
     * Checks whether the {@link #getUserHeuristic()} method can be called safely.
     *
     * @return {@code true} if the method is available or {@code false} if not
     */
    public boolean hasUserHeuristicMethod() {
        return problem.hasHeuristicMethod();
    }

    /**
     * Checks whether the {@link #getUserHeuristicMultiplayer()} method can be called
     * safely.
     *
     * @return {@code true} if the method is available or {@code false} if not
     */
    public boolean hasUserHeuristicPlayerMethod() {
        return problem.hasHeuristicPlayerMethod();
    }

    /**
     * Calls player method of the problem class. Use {@link #hasPlayerMethod()}
     * to check whether this method can be called safely.
     * <p>
     * Game may not be in terminal state when this method is called.
     *
     * @return the returned player.
     */
    public Player getPlayer() {
        return problem.getPlayer();
    }

    /**
     * Checks whether the {@link #getPlayer()} method can be called safely.
     *
     * @return {@code true} if the method is available or {@code false} if not
     */
    public boolean hasPlayerMethod() {
        return problem.hasPlayerMethod();
    }

    /**
     * {@link #hasUtilityMethod()} to check whether this method can be called
     * safely.
     *
     * @return the number the problem class returned.
     */
    public Number getUtility() {
        return problem.getUtility();
    }

    /**
     * Calls the utility method provided by the problem class. Use {@link
     * #hasUtilityPlayerMethod()} to check whether this method can be called
     * safely.
     *
     * @param pPlayer
     *         a player reference can be passed.
     *
     * @return the number the problem class returned.
     */
    public Number getUtility(Player pPlayer) {
        return problem.getUtility(pPlayer);
    }

    /**
     * Checks whether the {@link #getUtility()} method can be called safely.
     *
     * @return {@code true} if the utility method is available or {@code false}
     * if not
     */
    public boolean hasUtilityMethod() {
        return problem.hasUtilityMethod();
    }

    /**
     * Checks whether the {@link #getUtility(Player))} method can be called
     * safely.
     *
     * @return {@code true} if the utility method is available or {@code false}
     * if not
     */
    public boolean hasUtilityPlayerMethod() {
        return problem.hasUtilityPlayerMethod();
    }

    public Game<T> copy() {
        return copy(this);
    }

    @Override
    public String toString() {
        return problem.getSource().toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other instanceof Game) {
            Game<?> o = (Game<?>) other;
            return problem.getSource().equals(o.problem.getSource());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return problem.getSource().hashCode();
    }

    /**
     * Returns a deep copy of the given Game object.
     *
     * @param pGame
     *         object to be copied
     *
     * @return deep copy of the given Game object
     */
    public static <E> Game<E> copy(final Game<E> pGame) {
        return KryoHelper.deepCopy(pGame);
    }

    /**
     * Returns the original problem and it's state for this instance.
     *
     * @return The problem.
     */
    public T getProblem() {
        return problem.getSource();
    }

    /**
     * Returns an array of all attributes that the problem object contains. Only
     * public attributes are considered by this method.
     *
     * @return The attributes.
     */
    public Object[] getAttributes() {
        return problem.getAttributes();
    }

    /**
     * Set a attribute to a specific value.
     *
     * @param index
     *         The Index of the attribute. Use {@link #getAttributes()} to
     *         retrieve the Array. The index of the element in this array goes
     *         here.
     * @param val
     *         The value to that the attribute is set to. The type must match
     *         the attribute type otherwise a
     *         {@link java.lang.ClassCastException}
     *         is thrown.
     */
    public void setAttribute(int index, Object val) {
        problem.setAttribute(index, val);
    }

    /**
     * Creates a new Game object from this Game object by invoking {@link
     * #getNewGame(Action, MemorySavingMode)} with the given {@link Action} and
     * returns an evaluation value for the new game state from the viewpoint of
     * the current player of the previous game state.
     * <p>
     * Returns {@link Optional#empty()} if the game has no heuristic method with
     * player argument.
     *
     * @param pAction
     *         the {@link Action} to execute on this game state, should not be
     *         null
     *
     * @return evaluation value of the new game state from the viewpoint of the
     * current player of the previous game state
     */
    public Optional<Number> multiplayerScore(final Action pAction) {
        if (pAction == null) {
            throw new IllegalArgumentException("Action cannot be null.");
        }
        if (!hasUserHeuristicPlayerMethod()) {
            return Optional.empty();
        }
        Player player = getPlayer();
        Game<?> newGame = getNewGame(pAction, MemorySavingMode.NONE);
        return hasUtilityPlayerMethod() && isTerminal()
                ? Optional.of(newGame.getUtility(player))
                : Optional.of(
                        getUserHeuristicMultiplayer().eval(newGame, player));
    }
}
