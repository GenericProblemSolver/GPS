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
 * A functional interface that is used by the collision detection in the vector
 * racer game.
 * 
 * @author haker@uni-bremen.de
 *
 */
interface INodeTest {

    /**
     * Test a node at the given position.
     * 
     * @param p
     *            the position of the node.
     * @return {@code true} if a collision should occur. {@code false} to let
     *         the algorithm continue checking for collisions.
     */
    boolean test(Vector p);
}
