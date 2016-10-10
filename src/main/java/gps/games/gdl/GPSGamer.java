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

import java.util.List;
import java.util.Optional;

import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.factory.GdlFactory;
import org.ggp.base.util.gdl.factory.exceptions.GdlFormatException;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.symbol.factory.exceptions.SymbolFormatException;

import gps.IWrappedProblem;
import gps.games.GamesModule;
import gps.games.wrapper.Action;

/**
 * This class provides a basis for making a general game player out of our GPS
 * 
 * 
 * 
 * 
 * @author Sven
 *
 */
public class GPSGamer extends StateMachineGamer {

    private IWrappedProblem<StanfordGDLGame> game;
    private StateMachine sm;

    /**
     * basic constructor to initialize this general game player for a new game
     * 
     * @param gdl
     *            A String containing a whole Game-Description in GDL
     * 
     * @throws SymbolFormatException
     * @throws GdlFormatException
     * 
     */
    public GPSGamer(final String gdl)
            throws GdlFormatException, SymbolFormatException {
        List<Gdl> description = GdlFactory.createList(gdl);
        sm = getInitialStateMachine();
        sm.initialize(description);
        game = new StanfordGDLGame(sm, getRole());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#
     * getInitialStateMachine()
     */
    @Override
    public StateMachine getInitialStateMachine() {
        return new ProverStateMachine();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#
     * stateMachineMetaGame(long)
     */
    @Override
    public void stateMachineMetaGame(long timeout)
            throws TransitionDefinitionException, MoveDefinitionException,
            GoalDefinitionException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#
     * stateMachineSelectMove(long)
     */
    @Override
    public Move stateMachineSelectMove(long timeout)
            throws TransitionDefinitionException, MoveDefinitionException,
            GoalDefinitionException {
        GamesModule<StanfordGDLGame> solver = new GamesModule<StanfordGDLGame>(
                game);
        Optional<Action> maybe = solver.bestMove();
        if (maybe.isPresent()) {
            return (Move) maybe.get().get();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineStop
     * ()
     */
    @Override
    public void stateMachineStop() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#
     * stateMachineAbort()
     */
    @Override
    public void stateMachineAbort() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ggp.base.player.gamer.Gamer#preview(org.ggp.base.util.game.Game,
     * long)
     */
    @Override
    public void preview(Game g, long timeout) throws GamePreviewException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ggp.base.player.gamer.Gamer#getName()
     */
    @Override
    public String getName() {
        return "Generic Problem Player v0.1";
    }

}
