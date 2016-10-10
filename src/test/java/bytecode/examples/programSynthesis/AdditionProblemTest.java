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
package bytecode.examples.programSynthesis;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Meta-Test for the Addition Program Synthesis problem.
 * 
 * @author caspar
 *
 */
public class AdditionProblemTest {

    @Test
    public void positiveTestProg1() {
        String program = ",>,[<+>-]<.";
        assertTrue(new AdditionProblem(program).test());
    }

    @Test
    public void positiveTestProg2() {
        String program = ",[->+<],[->+<]>.";
        assertTrue(new AdditionProblem(program).test());

    }

}
