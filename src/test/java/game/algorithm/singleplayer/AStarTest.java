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

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import game.gempuzzle.GemPuzzle;
import game.hanoi.Hanoi;
import gps.GPS;
import gps.ResultEnum;
import gps.common.AlgorithmUtility;
import gps.games.GamesModule;
import gps.games.algorithm.heuristic.NoHeuristic;
import gps.games.algorithm.singleplayer.AStar;
import gps.games.algorithm.singleplayer.common.datastruct.ToEvalTree;
import gps.games.wrapper.Game;

public class AStarTest {

    private Hanoi h;

    private GemPuzzle gp;

    private Game<Hanoi> g;

    private Game<GemPuzzle> gg;

    private AStar<Hanoi> as;

    private AStar<GemPuzzle> asg;

    /**
     * Tests if {@link AStar} can solve Hanoi with 4 disks without using a
     * heuristic.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void HanoiFourDisk()
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        h = new Hanoi(4);
        g = new Game<>(GPS.wrap(h));
        as = AlgorithmUtility.instantiateAlgorithm(AStar.class,
                new Object[] { new GamesModule<>(g) },
                new Object[] { NoHeuristic.instance(), ToEvalTree.class });

        assertTrue(as.getOptions().length > 0);

        assertTrue(as.isApplicable(ResultEnum.TERMINAL));
        assertTrue(as.isWinnable().get());

        assertFalse(as.moves().get().isEmpty());
        assertTrue(as.isFinished());
    }

    /**
     * Tests if {@link AStar} can solve a {@link GemPuzzle} without using a
     * heuristic.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void GemPuzzleWithoutHeuristic()
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        gp = new GemPuzzle(3, Long.MAX_VALUE);
        gg = new Game<>(GPS.wrap(gp));
        asg = AlgorithmUtility.instantiateAlgorithm(AStar.class,
                new Object[] { new GamesModule<>(gg) },
                new Object[] { NoHeuristic.instance(), ToEvalTree.class });

        assertTrue(asg.getOptions().length > 0);

        assertTrue(asg.isApplicable(ResultEnum.WINNABLE));
        assertTrue(asg.isWinnable().get());

        assertFalse(asg.moves().get().isEmpty());
        assertTrue(asg.isFinished());
    }

    /**
     * Tests if {@link AStar} can solve a {@link GemPuzzle} using a heuristic.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void GemPuzzleWithHeuristic()
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        gp = new GemPuzzle(3, Long.MAX_VALUE);
        System.out.println(gp);
        gg = new Game<>(GPS.wrap(gp));
        asg = AlgorithmUtility.instantiateAlgorithm(AStar.class,
                new Object[] { new GamesModule<>(gg) },
                new Object[] { NoHeuristic.instance(), ToEvalTree.class });

        assertTrue(asg.isApplicable(ResultEnum.WINNABLE));
        assertTrue(asg.isWinnable().get());

        assertFalse(asg.moves().get().isEmpty());
        assertTrue(asg.isFinished());
    }
}
