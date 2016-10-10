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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import game.gempuzzle.GemPuzzle;
import game.hanoi.Hanoi;
import gps.GPS;
import gps.games.GamesModule;
import gps.games.algorithm.singleplayer.DepthFirst;
import gps.games.wrapper.Game;

public class DepthFirstTest {

    private Hanoi h;

    private Game<Hanoi> g;

    private DepthFirst<Hanoi> df;

    /**
     * Tests whether {@link DepthFirst} can solve {@link Hanoi} with 4 disks.
     */
    @Test
    public void HanoiFourDisk() {
        h = new Hanoi(4);
        g = new Game<>(GPS.wrap(h));
        df = new DepthFirst<>(new GamesModule<>(g));

        assertTrue(df.isWinnable().get());

        assertFalse(df.moves().get().isEmpty());
        assertTrue(df.isFinished());
    }

    /**
     * Tests if {@link DepthFirst} can solve a {@link GemPuzzle}.
     */
    @Test
    public void GemPuzzleThreeTimesThree() {
        GemPuzzle gp = new GemPuzzle(3);
        Game<GemPuzzle> gg = new Game<>(GPS.wrap(gp));
        DepthFirst<GemPuzzle> dfg = new DepthFirst<>(new GamesModule<>(gg));

        assertTrue(dfg.isWinnable().get());

        assertFalse(dfg.moves().get().isEmpty());
        assertTrue(dfg.isFinished());
    }

}
