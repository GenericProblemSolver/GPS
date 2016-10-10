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
package game.classification;

import java.util.List;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

import game.hanoi.Hanoi;
import gps.GPS;
import gps.games.wrapper.Action;

/**
 * Tests for the classification of problems to algorithm
 * 
 * @author haker@uni-bremen.de
 *
 */
public class ClassifierTest {

    /**
     * Tests the classification of hanoi problem
     */
    @Ignore
    @Test
    public void testHanoiClassification() {
        final Hanoi p = new Hanoi(5);
        GPS<Hanoi> gps = new GPS<>(p);
        Optional<List<Action>> mvs = gps.moves();
        System.out.println("Num of moves:" + mvs.get().size());
    }
}
