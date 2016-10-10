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

import java.util.regex.Pattern;

import org.junit.Test;

/**
 * 
 * @author haker@uni-bremen.de
 *
 */
public class GUITest {
    @Test
    public void patternTest() {
        Pattern pattern = Pattern.compile("(?<val>\\d+)(?<unit>ms|s|m|h|d|)");
        assertTrue(pattern.matcher("100ms").matches());
        assertTrue(pattern.matcher("5s").matches());
        assertTrue(pattern.matcher("8m").matches());
        assertTrue(pattern.matcher("998h").matches());
        assertTrue(pattern.matcher("1h").matches());
        assertTrue(pattern.matcher("13").matches());
    }
}
