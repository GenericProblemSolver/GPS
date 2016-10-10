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
package gps.games.gdl;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gps.IWrappedProblem;
import gps.games.wrapper.Action;
import gps.games.wrapper.Player;

/**
 * This class implements the interface for our internal Problem representation.
 * mainly the methods that are important for the games solver are implemented
 * 
 * @author Sven
 *
 */
public class StanfordGDLGame implements IWrappedProblem<StanfordGDLGame> {

    /**
     * standard slf4j Logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(StanfordGDLGame.class);

    /**
     * in this mode only one role is actually controlled by the the GPSSolver.
     * 
     * the moves of the other roles are chosen randomly from the legal moves
     * available to each role
     */
    public static final PlayerSwitchingMode KEEP_SAME_PLAYER = PlayerSwitchingMode.keep;

    /**
     * in this mode the Solver picks a move for each role, before the next state
     * is calculated
     */
    public static final PlayerSwitchingMode ALTERNATE_PlAYERS = PlayerSwitchingMode.alternate;

    /**
     * Enum to contain all possible modes for how the players are alternated
     * between turns.
     * 
     * @author Sven
     *
     */
    public static enum PlayerSwitchingMode {
        keep, alternate
    };

    /**
     * <p>
     * determines how playerswitching at the end of turns is handled
     * </p>
     * 
     * <p>
     * default value: {@link #KEEP_SAME_PLAYER}
     * </p>
     * 
     * @see {@link #KEEP_SAME_PLAYER} {@link #ALTERNATE_PlAYERS}
     */
    private PlayerSwitchingMode playerSwitchingMode = KEEP_SAME_PLAYER;

    /**
     * an automaton that represents the game as states and transitions.
     * 
     * a state in this statemachine consists of all the axioms (which are always
     * true) and the sentences that are true in the given state
     */
    private StateMachine stateMachine;

    private MachineState currentState;

    private List<Role> players;

    private List<Move> currentMoves;

    private Integer currentPlayerIndex;

    /**
     * basic constructor that receives an initialized {@link StateMachine} and
     * fills the fields with default values
     * 
     * @param pStateMachine
     *            an initialized {@link StateMachine}
     * @see StateMachine#initialize(List)
     */
    public StanfordGDLGame(final StateMachine pStateMachine) {
        stateMachine = pStateMachine;
        currentState = stateMachine.getInitialState();
        currentPlayerIndex = 0;
        players = stateMachine.getRoles();
        currentMoves = new ArrayList<Move>(players.size());
    }

    /**
     * sets the current player to a specific Role
     * 
     * @param pStateMachine
     * @param role
     */
    public StanfordGDLGame(final StateMachine pStateMachine, final Role role) {
        this(pStateMachine);
        currentPlayerIndex = stateMachine.getRoles().indexOf(role);

    }

    /**
     * sets the player switching mode
     * 
     * @param pStateMachine
     * @param pSwitchMode
     * @see #playerSwitchingMode
     */
    public StanfordGDLGame(final StateMachine pStateMachine,
            final PlayerSwitchingMode pSwitchMode) {
        this(pStateMachine);
        playerSwitchingMode = pSwitchMode;
    }

    /**
     * sets the player switching mode sets the current player to a specific Role
     * 
     * @param pStateMachine
     * @param pSwitchMode
     * @see #playerSwitchingMode
     */
    public StanfordGDLGame(final StateMachine pStateMachine, final Role role,
            final PlayerSwitchingMode pSwitchMode) {
        this(pStateMachine, role);
        playerSwitchingMode = pSwitchMode;
    }

    /**
     * 
     * 
     * @return the current {@link #playerSwitchingMode}
     */
    public PlayerSwitchingMode getPlayerSwitchingMode() {
        return playerSwitchingMode;
    }

    /**
     * sets the current {@link #playerSwitchingMode} to the given mode
     * 
     * @param playerSwitchingMode
     *            the new {@link #playerSwitchingMode}
     */
    public void setPlayerSwitchingMode(
            PlayerSwitchingMode playerSwitchingMode) {
        this.playerSwitchingMode = playerSwitchingMode;
    }

    /*
     * Interface methods
     */

    /**
     * <p>
     * applies the given action to the current state for the current player
     * {@link #getPlayer}
     * </p>
     * 
     * <p>
     * updates the player according to which player switching mode was selected
     * </p>
     * 
     * @param pAction
     * 
     * @see {@link #KEEP_SAME_PLAYER} {@link #ALTERNATE_PlAYERS}
     */
    @Override
    public void applyAction(final Action pAction) {
        try {
            logger.debug("doing " + pAction.get().toString());
            switch (playerSwitchingMode) {
            case keep:
                currentState = stateMachine.getRandomNextState(currentState,
                        players.get(currentPlayerIndex), (Move) pAction.get());
                return;
            case alternate:

                if (currentMoves.size() == currentPlayerIndex) {
                    currentMoves.add(currentPlayerIndex, (Move) pAction.get());
                } else {
                    currentMoves.set(currentPlayerIndex, (Move) pAction.get());
                }

                currentPlayerIndex++;
                if (currentPlayerIndex == players.size()) {
                    currentState = stateMachine.getNextState(currentState,
                            currentMoves);
                    currentPlayerIndex = 0;
                }
                return;
            default:
                logger.error(
                        "unknown playerSwitchingMode: " + playerSwitchingMode);
                throw new IllegalStateException(
                        "unknown playerSwitchingMode: " + playerSwitchingMode);
            }
        } catch (MoveDefinitionException | TransitionDefinitionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasApplyActionMethod() {
        return true;
    }

    /**
     * @return null
     */
    @Override
    public List<Runnable> getRunnableMoves() {
        return null;
    }

    /**
     * <p>
     * Returns a list containing every move that is legal for the current
     * player, which can be retrieved by using
     * {@link StanfordGDLGame#getPlayer()}, in the given state.
     * </p>
     */
    @Override
    public List<Action> getActions() {
        try {
            List<Move> legals = stateMachine.getLegalMoves(currentState,
                    players.get(currentPlayerIndex));
            List<Action> actions = new ArrayList<>();
            for (Move m : legals) {
                actions.add(new Action(m));
            }
            return actions;
        } catch (MoveDefinitionException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * @return the currentState
     */
    public MachineState getCurrentState() {
        return currentState;
    }

    /**
     * @param currentState
     *            the currentState to set
     */
    public void setCurrentState(MachineState currentState) {
        this.currentState = currentState;
    }

    @Override
    public boolean hasActionMethod() {
        return true;
    }

    @Override
    public boolean isTerminal() {
        return stateMachine.isTerminal(currentState);
    }

    @Override
    public boolean hasTerminalMethod() {
        return true;
    }

    @Override
    public Number heuristic() {
        try {
            return stateMachine.getGoal(currentState,
                    players.get(currentPlayerIndex));
        } catch (GoalDefinitionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Number heuristic(Player pPlayer) {
        try {
            return stateMachine.getGoal(currentState, (Role) pPlayer.get());
        } catch (GoalDefinitionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean hasHeuristicMethod() {
        return true;
    }

    @Override
    public boolean hasHeuristicPlayerMethod() {
        return true;
    }

    @Override
    public Player getPlayer() {
        return new Player(players.get(currentPlayerIndex));
    }

    @Override
    public boolean hasPlayerMethod() {
        return true;
    }

    @Override
    public Number getUtility() {
        try {
            return stateMachine.getGoal(currentState,
                    players.get(currentPlayerIndex));
        } catch (GoalDefinitionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Number getUtility(Player pPlayer) {
        try {
            return stateMachine.getGoal(currentState, (Role) pPlayer.get());
        } catch (GoalDefinitionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean hasUtilityMethod() {
        return true;
    }

    @Override
    public boolean hasUtilityPlayerMethod() {
        return true;
    }

    @Override
    public boolean hasObjectiveFunction() {
        return false;
    }

    @Override
    public double objectiveFunction(Object[] params) {
        return 0;
    }

    /**
     * 
     */
    @Override
    public Object[] getDefaultParams() {
        return null;
    }

    @Override
    public List<Object[]> neighbor(Object[] params) {
        return null;
    }

    @Override
    public boolean hasNeighborFunction() {
        return false;
    }

    @Override
    public StanfordGDLGame getSource() {
        return this;
    }

    @Override
    public Object[] getAttributes() {
        return new Object[0];
    }

    @Override
    public void setAttribute(int index, Object val) {

    }

    @Override
    public boolean isAttributeFinal(int index) {
        return false;
    }

    @Override
    public void setThresholdForObjectiveFunction(double pThresh) {

    }

    @Override
    public boolean canBeGreaterThan() {
        return false;
    }

    @Override
    public void setMaximize(byte pMax) {

    }

}
