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
package game.vectorracer.assets;

/**
 * A Simple 2D Vector class which bases on int's.
 *
 * @author haker@uni-bremen.de
 */
public class Vector {

    /**
     * The x component.
     */
    private int x;

    /**
     * The y component.
     */
    private int y;

    /**
     * Construct a 2D vector by components
     *
     * @param x
     *         the x component
     * @param y
     *         the y component
     */
    public Vector(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Add vector to this vector by adding it's components.
     *
     * @param o
     *         the other vector
     */
    public void add(final Vector o) {
        add(o.x, o.y);
    }

    /**
     * Add vector to this vector by adding it's components.
     *
     * @param dx
     *         the other vector x
     * @param dy
     *         the other vector y
     */
    private void add(int dx, int dy) {
        x += dx;
        y += dy;
    }

    /**
     * Retrieve the x component.
     *
     * @return the x component
     */
    public int getX() {
        return x;
    }

    /**
     * Retrieve the y component.
     *
     * @return the y component
     */
    public int getY() {
        return y;
    }

    /**
     * Construct a new clone with the components from this vector.
     *
     * @return the copy of the vector
     */
    public Vector copy() {
        return new Vector(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vector vector = (Vector) o;

        if (x != vector.x) {
            return false;
        }
        return y == vector.y;

    }
}
