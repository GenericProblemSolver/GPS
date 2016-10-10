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
package bytecode.invokes.iatfs;

import gps.annotations.Constraint;

import java.util.HashSet;

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class TestHashSet {

    static class P {
        int i;
    }

    HashSet<P> set3 = new HashSet<>();

    public TestHashSet() {
        set3.add(new P());
    }

    @Constraint
    boolean test() {
        HashSet<P> set = new HashSet<>();
        P p = new P();
        set.add(p);
        set.add(p);
        return set.contains(p) && set.size() == 1;
    }

    @Constraint
    boolean test2(int n) {
        HashSet<P> set2 = new HashSet<>();
        for (int i = 0; i < n; ++i) {
            P p = new P();
            set2.add(p);
        }
        return set2.size() == 2;
    }

    @Constraint
    boolean test3(int n) {
        return set3.size() == n;
    }
}