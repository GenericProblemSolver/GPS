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

import java.util.concurrent.ThreadLocalRandom;

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;
import gps.annotations.Variable;

/**
 * An implementation of a graph coloring problem that does not use many features of Java.
 * 
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class SimpleGraphColoringProblem {

    /**
     * Adjacency matrix of the graph.
     */
    boolean[][] connections;

    /**
     * Number of colors allowed in the coloring.
     */
    int numbOfColors;

    /**
     * Number of nodes of the graph.
     */
    int numbOfNodes;

    /**
     * Coloring of the graph.
     */
    @Variable
    int[] coloring;

    public SimpleGraphColoringProblem() {
        this(15, 5, 0.5);
    }

    public SimpleGraphColoringProblem(int pNumbOfNodes, int pNumbOfColors,
            double difficulty) {
        numbOfNodes = pNumbOfNodes;
        numbOfColors = pNumbOfColors;

        connections = new boolean[pNumbOfNodes][pNumbOfNodes];

        coloring = new int[numbOfNodes];

        int[] colors = new int[pNumbOfNodes];

        //Generate colors randomly
        for (int i = 0; i < pNumbOfNodes; i++) {
            colors[i] = ThreadLocalRandom.current().nextInt(0, pNumbOfColors);
        }

        //Create connections that are compatible with this coloring
        for (int i = 0; i < numbOfNodes; i++) {
            for (int j = 0; j < i; j++) {
                if (colors[i] != colors[j]) {
                    if (ThreadLocalRandom.current()
                            .nextDouble(1) < difficulty) {
                        connections[i][j] = true;
                    }
                }
                connections[j][j] = false;
            }
        }
        for (int i = 0; i < numbOfNodes; i++) {
            connections[i][i] = false;
        }
        for (int i = 0; i < numbOfNodes; i++) {
            for (int j = i + 1; j < numbOfNodes; j++) {
                connections[i][j] = connections[j][i];
            }
        }

    }

    @Constraint
    public boolean isValidColoring() {
        if (coloring.length != numbOfNodes) {
            return false;
        }
        for (int color : coloring) {
            if (color < 0 || color >= numbOfColors) {
                return false;
            }
        }
        for (int i = 0; i < numbOfNodes; i++) {
            for (int j = 0; j < numbOfNodes; j++) {
                if (connections[i][j] && coloring[i] == coloring[j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
