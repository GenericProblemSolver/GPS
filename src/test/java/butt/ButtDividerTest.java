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
package butt;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import butt.tool.BenchmarkTool;
import gps.util.Tuple;

/**
 * 
 * @author haker@uni-bremen.de
 *
 */
public class ButtDividerTest {
    @Test
    public void testButtDivider() {
        for (int i = 1; i <= 10; i++) {
            Tuple<Integer, Integer> tp = BenchmarkTool.calculateTaskRange(98672,
                    i, 10);
            assertTrue(
                    tp.getY() - tp.getX() <= (98672 / 10) + (i == 10 ? 2 : 0));
        }
    }
}
