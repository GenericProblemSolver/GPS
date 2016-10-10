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
package game.algorithm.singleplayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import game.gempuzzle.GemPuzzle;
import game.hanoi.Hanoi;
import gps.GPS;
import gps.games.GamesModule;
import gps.games.algorithm.singleplayer.BreadthFirst;
import gps.games.algorithm.singleplayer.DepthFirst;
import gps.games.wrapper.Game;

public class BreadthFirstTest {

    private Hanoi h;

    private Game<Hanoi> g;

    private BreadthFirst<Hanoi> bf;

    /**
     * Tests whether {@link DepthFirst} can solve Hanoi with 5 disks. As Breadth
     * first search is the optimal way to solve this puzzle it also checks if it
     * needs the minimal amount of moves.
     */
    @Test
    public void HanoiFiveDisk() {
        h = new Hanoi(5);
        g = new Game<>(GPS.wrap(h));
        bf = new BreadthFirst<>(new GamesModule<>(g));

        assertTrue(bf.isWinnable().get());

        assertFalse(bf.moves().get().isEmpty());
        assertTrue(bf.isFinished());
        assertEquals(bf.moves().get().size(), 31);
    }

    /**
     * Tests if {@link BreadthFirst} can solve a {@link GemPuzzle}.
     */
    @Test
    public void GemPuzzleThreeTimesThree() {
        GemPuzzle gp = new GemPuzzle(3);
        Game<GemPuzzle> gg = new Game<>(GPS.wrap(gp));
        BreadthFirst<GemPuzzle> bfg = new BreadthFirst<>(new GamesModule<>(gg));

        assertTrue(bfg.isWinnable().get());

        assertFalse(bfg.moves().get().isEmpty());
        assertTrue(bfg.isFinished());
    }

}
