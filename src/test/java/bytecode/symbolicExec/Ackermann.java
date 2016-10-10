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
package bytecode.symbolicExec;

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

/**
 * Implements the Ackermann function https://en.wikipedia.org/wiki/Ackermann_function without recursion/function calls.
 * 
 * @author caspar
 */
@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class Ackermann {

    int[] stack = new int[10];

    @Constraint
    public boolean ackermann(int m, int n) {
        int size = 0;
        int rs = 0;
        main: while (true) {
            OneCall: while (true) {
                if (m == 0) {
                    rs = n + 1;
                    break OneCall;
                } else if (m > 0 && n == 0) {
                    m = m - 1;
                    n = 1;
                    continue OneCall;
                } else if (m > 0 && n > 0) {
                    //save m to stack
                    stack[size] = m;
                    size++;
                    if (size >= stack.length) {
                        return false;
                        //                        int[] newStack = new int[size + 10];
                        //                        for (int i = 0; i < size; i++) {
                        //                            newStack[i] = stack[i];
                        //                        }
                        //                        stack = newStack;
                    }
                    n--;
                    continue OneCall;
                }
            }
            if (size == 0) {
                return rs == 4;
            }
            size--;
            m = stack[size] - 1;
            n = rs;
            continue main;
        }

    }

    public static void main(String[] args) {
    }
}
