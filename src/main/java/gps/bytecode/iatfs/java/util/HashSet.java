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
package gps.bytecode.iatfs.java.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Proof of concept for a standard library replacement
 * @author mfunk@tzi.de
 *
 * @param <T>
 */
public class HashSet<T> implements Set<T> {

    Object[] data;
    int size;

    public HashSet() {
        data = new Object[10];
        size = 0;
    }

    public HashSet(java.util.HashSet<T> s) {
        this();
        addAll(s);
    }

    @Override
    public boolean add(T e) {
        if (contains(e)) {
            return false;
        }
        data[size] = e;
        size += 1;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T t : c) {
            add(t);
        }
        return true;
    }

    @Override
    public void clear() {
        size = 0;
    }

    @Override
    public boolean contains(Object o) {
        for (int i = 0; i < size; ++i) {
            if (data[i] == o) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<T> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Object[] toArray() {
        return data;
    }

    @Override
    public <E> E[] toArray(E[] a) {
        return null;
    }

}
