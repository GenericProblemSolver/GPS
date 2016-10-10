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
package gps.games.wrapper;

/**
 * Simple Wrapper Class for Objects that are returned from Problem Classes.
 * 
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The Type of the unwrapped object.
 */
public class SimpleWrapper<T> {
    /**
     * The original, unwrapped object
     */
    private final T obj;

    /**
     * wrap an object into a wrapper class.
     * 
     * @param pObj
     *            The player to wrap. Or {@code null}.
     */
    public SimpleWrapper(T pObj) {
        obj = pObj;
    }

    /**
     * Retrieve the original, unwrapped object that has been used to instantiate
     * this object. Can be {@code null} if {@code null} was used during
     * instantiation.
     * 
     * @return the unwrapped object or {@code null}.
     */
    public T get() {
        return obj;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }

        final Object o;
        if (other instanceof SimpleWrapper) {
            o = ((SimpleWrapper<?>) other).get();
        } else {
            o = other;
        }
        if (o == null) {
            return obj == null;
        }
        return o.equals(obj);
    }

    @Override
    public int hashCode() {
        if (obj == null) {
            return 0;
        }
        return obj.hashCode();
    }
}
