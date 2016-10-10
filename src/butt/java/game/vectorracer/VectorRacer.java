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
package game.vectorracer;

import game.vectorracer.assets.Player;
import game.vectorracer.assets.Track;
import game.vectorracer.assets.Vector;
import gps.annotations.Action;
import gps.annotations.Move;
import gps.annotations.TerminalTest;
import gps.util.IButtSampleProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An implementation for the game vector racer.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class VectorRacer implements IButtSampleProblem {
    /**
     * List of players participating in the game.
     */
    public List<Player> players = new ArrayList<>();

    /**
     * Player who can move this turn.
     */
    public int currentPlayer;

    /**
     * The track.
     */
    public Track track;

    /**
     * List of winners in ascending order. First winner is at index 0 etc.
     */
    public List<Player> winners = new ArrayList<>();

    /**
     * Constructs a new Vector Racer game.
     * 
     * @param pPlayers
     *            The amount of players that participate in this race.
     * @param pTrack
     *            The filename of the track to load.
     */
    public VectorRacer(final int pPlayers, final String pTrack) {
        track = new Track(pTrack);
        for (int i = 0; i < pPlayers; i++) {
            players.add(new Player(track.getStartPosition(), i));
        }
    }

    /**
     * Get the player whose turn is going on.
     * 
     * @return the current player
     */
    @gps.annotations.Player
    public Player getCurrentPlayer() {
        if (players.size() >= currentPlayer && currentPlayer >= 0) {
            return players.get(currentPlayer);
        }
        return null;
    }

    /**
     * Change to the next player's turn.
     */
    private void nextPlayer() {
        if (players.isEmpty()) {
            currentPlayer = -1;
            return;
        }
        currentPlayer++;
        currentPlayer %= players.size();
    }

    /**
     * Remove the current player from the turn list. Players that have been
     * removed will never get a turn again.
     */
    private void removePlayer() {
        if (players.size() >= currentPlayer) {
            players.remove(currentPlayer);
            if (players.isEmpty()) {
                currentPlayer = -1;
            } else {
                currentPlayer %= players.size();
            }
            return;
        }
        throw new RuntimeException(
                "Cannot remove player: current player do not exist in player list");
    }

    /**
     * An array of all possible moves a player can perform.
     */
    private static final Vector actions[] = { new Vector(-1, -1),
            new Vector(0, -1), new Vector(1, -1), new Vector(-1, 0),
            new Vector(0, 0), new Vector(1, 0), new Vector(-1, 1),
            new Vector(0, 1), new Vector(1, 1) };

    /**
     * Get all action a player can perform.
     * 
     * @return the actions
     */
    @Action
    public Vector[] getActions() {
        return actions;
    }

    /**
     * Perform a action for the current player. Accelerates or decelerates the
     * current player. The components of the vector must be between -1 and 1
     * (incl)
     * 
     * After move the turn changes to the next players turn.
     * 
     * If the player crosses a wall or goal node the player is removed from the
     * turn list.
     * 
     * @param v
     *            the move.
     */
    @Move
    public void move(Vector v) {
        final Player p = getCurrentPlayer();

        if (p == null) {
            return;
        }

        final Vector oldPos = p.getPos();
        p.move(v);

        Vector m = track.collisionTest(oldPos, p.getVelocity(),
                track.getWallGoalTest());
        if (m == null) {
            nextPlayer();
        } else {
            // collision occurred
            if (track.isGoal(m)) {
                p.setPassedGoal();
                winners.add(p);
            }
            if (track.isWall(m)) {
                p.setCrashed();
            }
            removePlayer();
        }
    }

    /**
     * Checks whether the race finished. A race is finished when no player can
     * do any move due to either reaching the goal or crashing into a wall.
     * 
     * @return {@code true} if the game finished. {@code false} otherwise.
     */
    @TerminalTest
    public boolean isFinished() {
        return players.isEmpty();
    }

    /**
     * Checks whether some player has won the race.
     * 
     * @return {@code true} if the a player won the game by reaching a goal node
     *         {@code false} otherwise.
     */
    public boolean hasWinner() {
        return !winners.isEmpty();
    }

    /**
     * Gets the winner of the race or {@code null} if there exists no winner.
     * 
     * @return the winner.
     */
    public Player getWinner() {
        if (hasWinner()) {
            return winners.get(0);
        }
        return null;
    }

    /**
     * Returns the track for this race.
     * 
     * @return the track.
     */
    public Track getTrack() {
        return track;
    }

    /**
     * Gets a list of the players that are still participating in the race.
     * Players are considered participating if they are not crashed into a wall
     * or crossed a goal node.
     * 
     * @return the list.
     */
    public List<Player> getParticipatingPlayers() {
        return new ArrayList<>(players);
    }

    @Override
    public Object getIdentifier() {
        return "Vc" + Objects.hash(this.currentPlayer, this.players.size(),
                this.track.getPath());
    }
}
