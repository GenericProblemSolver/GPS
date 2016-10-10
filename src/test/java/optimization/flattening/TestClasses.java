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
package optimization.flattening;

/**
 * A few simple test classes used by the
 * {@link optimization.flattening.FlatteningTest},
 * {@link optimization.flattening.FlatObjectsTest} and
 * {@link optimization.algorithm.SearchOperatorProviderTest}.
 * 
 * @author mburri, sotisi
 *
 */
@SuppressWarnings("unused")
public class TestClasses {

    public class TestA {

        public TestA() {
            i = 1;
            c = 'a';
            d = 0.5;
            b = true;
        }

        private int i;

        private char c;

        private double d;

        private boolean b;
    }

    public class TestB {

        private TestA a;

        private float f;

        public TestB() {
            f = 0;
            a = new TestA();
        }

    }

    public class TestC {
        int[] array;

        public TestC() {
            array = new int[4];
            array[0] = 1;
            array[1] = 3;
            array[2] = 5;
            array[3] = 7;
        }
    }

    public class TestD {
        TestC t;

        public TestD() {
            t = new TestC();
        }
    }

}
