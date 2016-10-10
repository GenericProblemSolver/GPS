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
package bytecode.heap;

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class TestRuntimeType {

    static interface I {

    }

    static class R implements I {

    }

    static class P extends R {

    }

    static class Q extends R {

    }

    static class T implements I {

    }

    R tp = new P();
    R tq = new Q();

    @Constraint
    boolean test() {
        R p = new P();
        R q = new Q();
        return p instanceof P && !(q instanceof P);
    }

    @Constraint
    boolean test2() {
        return tp instanceof P && !(tq instanceof P);
    }

    @Constraint
    boolean test3(boolean b) {
        R r;
        if (b) {
            r = tp;
        } else {
            r = tq;
        }

        if (r instanceof P) {
            return true;
        } else {
            return false;
        }
    }

    @Constraint
    boolean test4() {
        P p = new P();
        Q q = new Q();
        return p instanceof R && q instanceof R;
    }

    @Constraint
    boolean test5() {
        I p = new P();
        return p instanceof R;
    }

    @Constraint
    boolean test6() {
        I p = new T();
        return !(p instanceof R) && p instanceof I && p instanceof T;
    }

}
