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
package preprocessing;

import org.junit.Test;

import gps.GPS;
import gps.annotations.Constraint;

/**
 * This tests the preprocessing for nested classes
 * 
 * @author haker@uni-bremen.de
 *
 */
public class TestNestedClasses {

    static class Nested1 {
        @Constraint
        void test() {
        }
    }

    static class Nested2 {
        static class Nested1 {
            @Constraint
            void test() {
            }
        }
    }

    @Test
    public void testNested2() {
        new GPS<>(new Nested2.Nested1());
    }

    @Test
    public void testNested1() {
        new GPS<>(new Nested1());
    }
}
