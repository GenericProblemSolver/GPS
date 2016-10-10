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
package game.vectorracer.assets;

/**
 * Represents a participant for the Vector Racer game.
 *
 * @author haker@uni-bremen.de
 */
public class Player {

    /**
     * The current position of the player.
     */
    private Vector pos;

    /**
     * The current velocity of the player.
     */
    private Vector velocity;

    /**
     * A flag which gets set when the player crashed and cannot participate in
     * the game anymore.
     */
    private boolean isCrashed = false;

    /**
     * A Player id. Can be used to differentiate between players.
     */
    private final int playerId;

    /**
     * A flag which gets set when the player passed the goal and cannot
     * participate in the game anymore.
     */
    private boolean hasPassedGoal = false;

    /**
     * Construct a new vector racer participant.
     *
     * @param pPos
     *            The starting position for this player.
     * @param pPlayerId
     *            The ID for the player.
     */
    public Player(final Vector pPos, final int pPlayerId) {
        velocity = new Vector(0, 0);
        pos = pPos;
        playerId = pPlayerId;
    }

    /**
     * Set the crashed flag which indicates that the player crashed into a wall.
     */
    public void setCrashed() {
        isCrashed = true;
    }

    /**
     * Checks the crashed flag which indicates that the player crashed into a
     * wall.
     *
     * @return the flag
     */
    public boolean isCrashed() {
        return isCrashed;
    }

    /**
     * Accelerate the player by a given vector. The Vectors components must be
     * between -1 and 1 (incl).
     *
     * @param v
     *            the acceleration vector
     */
    public void move(Vector v) {
        if (Math.abs(v.getX()) > 1 || Math.abs(v.getY()) > 1) {
            throw new IllegalArgumentException(
                    "Components must be between -1 and 1 (inclusive)");
        }
        velocity.add(v);
        pos.add(velocity);
    }

    /**
     * Get the current position.
     *
     * @return the position
     */
    public Vector getPos() {
        return pos.copy();
    }

    /**
     * Get the current velocity vector for this player.
     *
     * @return the vector
     */
    public Vector getVelocity() {
        return velocity.copy();
    }

    /**
     * Retrieve the ID for the player which cannot be changed during the
     * lifetime of the player.
     *
     * @return the id
     */
    public int getId() {
        return playerId;
    }

    /**
     * Checks whether the player has passed a goal node.
     *
     * @return {@code true} if the player has passed the goal node, {@code
     * false} otherwise.
     */
    public boolean hasPassedGoal() {
        return hasPassedGoal;
    }

    /**
     * Sets the flag that indicates that the player has passed the goal.
     */
    public void setPassedGoal() {
        this.hasPassedGoal = true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player ");
        sb.append(getId());
        sb.append(" [c:");
        sb.append(isCrashed() ? '1' : '0');
        sb.append(" g:");
        sb.append(hasPassedGoal() ? '1' : '0');
        sb.append(" pos:");
        sb.append(getPos());
        sb.append(" speed:");
        sb.append(getVelocity());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Player)) {
            return false;
        }
        Player player = (Player) o;

        if (isCrashed != player.isCrashed) {
            return false;
        }
        if (playerId != player.playerId) {
            return false;
        }
        if (hasPassedGoal != player.hasPassedGoal) {
            return false;
        }
        if (pos != null ? !pos.equals(player.pos) : player.pos != null) {
            return false;
        }
        return velocity != null ? velocity.equals(player.velocity)
                : player.velocity == null;

    }
}
