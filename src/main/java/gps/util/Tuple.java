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
package gps.util;

import java.util.Objects;

/**
 * This class represents a 2-tuple. This class is an immutable.
 *
 * @author alueck@uni-bremen.de
 * @author haker@uni-bremen.de
 */
public final class Tuple<X, Y> {

    /**
     * The x element. May be {@code null}.
     */
    private final X x;

    /**
     * The y element. May be {@code null}.
     */
    private final Y y;

    /**
     * Construct a tuple
     * 
     * @param x
     *            The x element. May be {@code null}.
     * @param y
     *            The y element. May be {@code null}.
     */
    public Tuple(final X x, final Y y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy constructor. Does copy the x and y references from the source tuple.
     * 
     * @param pSrc
     *            The source.
     */
    public Tuple(final Tuple<? extends X, ? extends Y> pSrc) {
        this.x = pSrc.x;
        this.y = pSrc.y;
    }

    /**
     * Retrieve the x element.
     * 
     * @return The x element. May be {@code null}.
     */
    public final X getX() {
        return x;
    }

    /**
     * Retrieve the y element.
     * 
     * @return The y element. May be {@code null}.
     */
    public final Y getY() {
        return y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tuple)) {
            return false;
        }
        Tuple<?, ?> other = (Tuple<?, ?>) obj;
        return Objects.equals(x, other.x) && Objects.equals(y, other.y);
    }
}
