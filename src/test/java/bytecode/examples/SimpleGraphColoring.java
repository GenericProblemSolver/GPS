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
package bytecode.examples;

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;
import gps.annotations.Variable;
import gps.bytecode.symexec.SymbolicExec;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class SimpleGraphColoring {

    static class Node {
        @Variable
        int color = -1;

        Node[] next;
    }

    Node[] nodes = new Node[40];

    int n = 2;

    public SimpleGraphColoring() {
        for (int i = 0; i < nodes.length; ++i) {
            nodes[i] = new Node();
            nodes[i].next = new Node[2];
        }
        //Generate ring structure
        for (int i = 0; i < nodes.length; ++i) {
            nodes[i].next[0] = nodes[(i + 1) % nodes.length];
            nodes[i].next[1] = nodes[((i - 1) + nodes.length) % nodes.length];
        }
    }

    @Constraint
    boolean colored() {
        for (int i = 0; i < nodes.length; ++i) {
            Node node = nodes[i];
            if (node.color < 1 || node.color > n) {
                return false;
            }
            for (int j = 0; j < node.next.length; ++j) {
                if (node.color == node.next[j].color) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        SimpleGraphColoring s = new SimpleGraphColoring();
        SymbolicExec.constructFunctionalSatisfactionProblem(s);
    }
}
