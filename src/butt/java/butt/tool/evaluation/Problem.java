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
package butt.tool.evaluation;

import java.util.Objects;

/**
 * Problem class that contains the name and the vector of it. Ignores the vector
 * for hash and equals calculations.
 * 
 * @author haker@uni-bremen.de
 */
public class Problem {
    @Override
    public int hashCode() {
        return Objects.hash(problemName); // ignore vector
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Problem other = (Problem) obj;
        if (problemName == null) {
            if (other.problemName != null) {
                return false;
            }
        } else {
            if (!problemName.equals(other.problemName)) {
                return false;
            }
        }
        return true;
    }

    public Problem(String probName, String vector) {
        this.problemName = probName;
        this.vector = vector;
    }

    public final String problemName;
    public final String vector;
};