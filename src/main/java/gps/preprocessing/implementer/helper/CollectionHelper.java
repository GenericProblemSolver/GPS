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
package gps.preprocessing.implementer.helper;

import gps.preprocessing.AbstractImplementer;

/**
 * Method Implementer for the overloaded toCollection method. These overloaded
 * methods simlify the use of non-collection types by putting them into a
 * collection. However this does not work with arrays because only non-primitve
 * typed arrays would be supported. Better use {@link java.util.Arrays#asList()} for
 * this which supports some primitve types too.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class CollectionHelper extends AbstractImplementer {

    /**
     * Construct the Method Implementer
     */
    public CollectionHelper() {
        super();
    }

    /**
     * The name of the toCollection method
     */
    public static final String TO_COLLECTION = "toCollection";

    @Override
    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();

        sb.append("    protected <T> java.util.Collection<T> " + TO_COLLECTION
                + "(java.util.Collection<T> c) { return c; }\n"
                + "    protected <T> java.util.Collection<T> " + TO_COLLECTION
                + "(T obj) {\n"
                + "        java.util.ArrayList<T> l = new java.util.ArrayList<>(1);\n"
                + "        l.add(obj); return l;\n    }\n\n");

        return sb.toString();
    }

}
