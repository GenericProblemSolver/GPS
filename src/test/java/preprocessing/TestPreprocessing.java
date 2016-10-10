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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import gps.GPS;
import gps.annotations.Action;
import gps.annotations.Heuristic;
import gps.annotations.Move;
import gps.annotations.Optimize;
import gps.annotations.Player;
import gps.annotations.TerminalTest;
import gps.annotations.Utility;
import gps.annotations.Variable;
import gps.exception.NoSolverAvailableException;

/**
 * Tests various wrapping capabilities.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class TestPreprocessing {
    public class ProblemClassA {
        @Player
        public int getPlayer() {
            return 2;
        }

        @Action
        public Integer integer = 3;

        @Action
        public Integer intergerFunc() {
            return 4;
        }

        @Action
        public void doSomething() {
            return;
        }

        @Action
        public Integer[] integerArray = { 4, 5, 6 };

        @Action
        public Collection<Integer> integerCollection = (Collection<Integer>) Arrays
                .asList(integerArray);

        @Action
        public List<Integer> integerListFunc() {
            return integerCollection.stream().collect(Collectors.toList());
        }

        @TerminalTest
        public Boolean terminalTest = false;

        @Utility
        public float utility(Integer b) {
            return b.floatValue();
        }

        @Move
        public void applyAction(int b) {

        }
    }

    public class ProblemClassB {
        @gps.annotations.Player
        public Integer player;

        @TerminalTest
        public Boolean terminalTest() {
            return false;
        }

        @Utility
        public Double utility() {
            return 6.0d;
        }

        @Heuristic
        public short heur() {
            return 1;
        }
    }

    public class ProblemClassC {
        @Utility
        public short utility = 6;
    }

    public static class ProblemClassD {

        /**
         * Simple number primitive variable
         */
        @Variable
        public int i = 0;

        /**
         * number wrapper variable
         */
        @Variable
        public Float f = (float) 0;

        /**
         * Simple string variable
         */
        @Variable
        public String s = "";

        /**
         * List variable
         */
        @Variable
        public List<String> l = new ArrayList<String>();

        /**
         * Using HashMap to test inheritance
         */
        @Variable
        public HashMap<String, Integer> m = new HashMap<String, Integer>();

        /**
         * Simple enum variable
         */
        @Variable
        public ENUMTEST t = ENUMTEST.A;

        @Optimize
        public Double max(int i, Float o, String d, List<String> l,
                Map<String, Integer> m, ENUMTEST t) {
            return (double) 0;
        }

        public enum ENUMTEST {
            A, B;
        }
    }

    /**
     * Tests multiple Action annotations, terminalTest as boolean field,
     * getPlayer as method, utility with player parameter
     */
    @Test
    public void testWrappingA() {
        new GPS<>(new ProblemClassA());
    }

    /**
     * Tests terminalTest as Boolean function, getPlayer as field, heuristic as
     * function
     */
    @Test(expected = NoSolverAvailableException.class)
    public void testWrappingB() {
        new GPS<>(new ProblemClassB()).moves();
    }

    /**
     * Tests utility as field
     */
    @Test(expected = NoSolverAvailableException.class)
    public void testWrappingC() {
        new GPS<>(new ProblemClassC()).moves();
    }

    /**
     * Tests optimize as function with Double as return type, 
     * int, Float, String, List, HashMap as parameters
     */
    @Test
    public void testWrappingD() {
        new GPS<>(new ProblemClassD());
    }

    /**
     * Seems the annotation processors ignores annotations in anonymous classes.
     */
    @Ignore
    @Test(expected = NoSolverAvailableException.class)
    public void testInnerClass() {
        new GPS<>(new Object() {
            @gps.annotations.Player
            public Integer getPlayer() {
                return 2;
            }
        }).bestMove();
    }
}
