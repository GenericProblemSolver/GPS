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
package bytecode.invokes;

import gps.annotations.Constraint;
import gps.annotations.Variable;

import java.util.ArrayList;

import org.junit.runner.RunWith;
import org.junit.Ignore;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
@Ignore
public class ComplexInvoke {

    static class Vector {
        final int x;
        final int y;

        public Vector(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Vector add(Vector other) {
            return new Vector(x + other.x, y + other.y);
        }

        Vector sub(Vector other) {
            return new Vector(x - other.x, y - other.y);
        }
    }

    static class Rect {
        Vector size;

        @Variable
        int pos_x;

        @Variable
        int pos_y;

        public Rect(Vector s) {
            size = s;
        }

        boolean contains(Vector point) {
            return point.x >= pos_x && point.x < pos_x + size.x
                    && point.y >= pos_y && point.y < pos_y + size.y;
        }

        boolean contains(Rect other) {
            return contains(other.pos())
                    || contains(
                            other.pos().add(other.size).sub(new Vector(1, 1)))
                    || contains(
                            other.pos().add(new Vector(other.size.x - 1, 0)))
                    || contains(
                            other.pos().add(new Vector(0, other.size.y - 1)));
        }

        Vector pos() {
            return new Vector(pos_x, pos_y);
        }

        boolean collides(Rect other) {
            return other.contains(this) || this.contains(other);
        }
    }

    private Vector totalSize = new Vector(9, 7);
    private final Rect[] rectangles;

    public ComplexInvoke() {
        ArrayList<Rect> list = new ArrayList<>();

        list.add(new Rect(new Vector(5, 3)));
        list.add(new Rect(new Vector(4, 7)));
        list.add(new Rect(new Vector(4, 2)));
        list.add(new Rect(new Vector(2, 2)));

        rectangles = list.toArray(new Rect[1]);
    }

    @Constraint
    boolean checkCollision() {
        for (int i = 0; i < rectangles.length; ++i) {
            Rect ri = rectangles[i];
            for (int j = i + 1; j < rectangles.length; ++j) {
                if (ri.collides(rectangles[j])) {
                    return false;
                }
            }
            if (ri.pos_x < 0 || ri.pos_y < 0
                    || ri.pos_x + ri.size.x > totalSize.x
                    || ri.pos_y + ri.size.y > totalSize.y) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < totalSize.y; ++y) {
            for (int x = 0; x < totalSize.x; ++x) {
                boolean found = false;
                for (int n = 0; n < rectangles.length; ++n) {
                    if (rectangles[n].contains(new Vector(x, y))) {
                        sb.append("" + n);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }
        sb.append(checkCollision());
        return sb.toString();
    }
}
