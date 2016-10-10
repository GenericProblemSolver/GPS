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
package bytecode.disassemble;

import gps.annotations.Constraint;

public class TestClassNested {

    static class Nested1 {
        @Constraint
        int test() {
            int a = 1;
            int b = 2;
            int c = 4;
            return c + (a + b);
        }
    }

    static class Nested2 {
        static class Nested3 {
            @Constraint
            int test() {
                int a = 1;
                int b = 2;
                int c = 4;
                return c + (a + b);
            }
        }
    }

}
