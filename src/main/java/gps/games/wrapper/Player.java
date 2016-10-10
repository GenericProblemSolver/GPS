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

/**
 * Wrapper class for players.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class Player extends SimpleWrapper<Object> {

    /**
     * wrap an object into Player.
     * 
     * @param pPlayer
     *            The player to wrap.
     */
    public Player(Object pPlayer) {
        super(pPlayer);
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }

        if (!(other instanceof Player)) {
            return false;
        }

        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
