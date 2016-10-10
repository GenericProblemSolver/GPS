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
package game.pathfinder;

import org.junit.Test;

import gps.GPS;
import gps.attribute.AttributeGraph;
import gps.games.MemorySavingMode;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;

/**
 * Tests a simple Pathfinding problem.
 * 
 * @author haker@uni-bremen.de
 * @author wahler@tzi.de
 */
public class PathfinderTest {

    /**
     * Displays the interpreted Maze as an Image
     * 
     * @param arg
     */
    public static void main(String arg[]) {
        new Maze("pathfinding/maze.bmp").showMessageBox();
    }

    @Test
    public void testPathfinding() {
        Maze m = new Maze("pathfinding/maze.bmp");
        new GPS<>(m);
    }

    /**
     * Tests if this algorithm can terminate. Is necessary as the
     * {@link AttributeGraph} for a {@link Maze} game is quite complex.
     */
    @Test
    public void testTerminating() {
        Maze m = new Maze("pathfinding/maze.bmp");
        Game<Maze> g = new Game<>(GPS.wrap(m));
        Action a = g.getActions().get(0);
        g.getNewGame(a, MemorySavingMode.ATTRIBUTE_GRAPH);
    }
}
