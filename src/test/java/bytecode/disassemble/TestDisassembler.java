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
package bytecode.disassemble;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import bytecode.symbolicExec.PropositionalLogicConstraintExample;
import gps.bytecode.disassemble.ClassDisassembler;
import gps.bytecode.disassemble.DMethod;
import gps.bytecode.symexec.SatisfactionProblem;
import gps.bytecode.symexec.SymbolicExec;

public class TestDisassembler {

    // @Test
    public void testConstraintDetection0() {
        final SatisfactionProblem sp = SymbolicExec
                .constructFunctionalSatisfactionProblem(this);
        assertEquals(0, sp.constraints.size());
    }

    @Test
    public void testConstraintDetection1() {
        final TestClassWith1Constraint cl = new TestClassWith1Constraint();
        final SatisfactionProblem sp = SymbolicExec
                .constructFunctionalSatisfactionProblem(cl);
        assertEquals(1, sp.constraints.size());
    }

    @Test
    public void testConstraintDetection2() {
        final TestClassWith2Constraints cl = new TestClassWith2Constraints();
        final SatisfactionProblem sp = SymbolicExec
                .constructFunctionalSatisfactionProblem(cl);
        assertEquals(2, sp.constraints.size());
    }

    //@Test
    public void testInvokeDetection() {
        final TestClassWithInvoke cl = new TestClassWithInvoke();
        final SatisfactionProblem sp = SymbolicExec
                .constructFunctionalSatisfactionProblem(cl);
        assertEquals(4, sp.constraints.size());
    }

    @Test
    public void testConstraintNested1() {
        final TestClassNested cl = new TestClassNested();
        final SatisfactionProblem sp = SymbolicExec
                .constructFunctionalSatisfactionProblem(cl);
        assertEquals(0, sp.constraints.size());
    }

    @Test
    public void testConstraintNested2() {
        final TestClassNested.Nested1 cl = new TestClassNested.Nested1();
        final SatisfactionProblem sp = SymbolicExec
                .constructFunctionalSatisfactionProblem(cl);
        assertEquals(1, sp.constraints.size());
    }

    @Test
    public void testConstraintNested3() {
        final TestClassNested.Nested2.Nested3 cl = new TestClassNested.Nested2.Nested3();
        final SatisfactionProblem sp = SymbolicExec
                .constructFunctionalSatisfactionProblem(cl);
        assertEquals(1, sp.constraints.size());
    }

    /**
     * 
     * Test, whether this correctly assesses the number of blocks in the method
     * {@link PropositionalLogicConstraintExample}.constraint
     * 
     * boolean constraint(boolean, boolean, boolean); Code: 0: iload_1 1: ifeq 8
     * 
     * 4: iload_2 5: ifeq 18
     * 
     * 8: iload_3 9: ifeq 16
     * 
     * 12: iload_2 13: ifeq 18
     * 
     * 16: iconst_1 17: ireturn
     * 
     * 18: iconst_0 19: ireturn
     * 
     * @author caspar
     */
    // @Test
    public void testPropositionalLogicConstraintExample() {
        PropositionalLogicConstraintExample plce = new PropositionalLogicConstraintExample();
        List<DMethod> dMethods = ClassDisassembler.disassembleMethods(
                ClassDisassembler.getClassFile(plce.getClass().getName()));
        // TODO: this way of getting the method
        // PropositionalLogicConstraintExample.constraint is suboptimal, but at
        // the moment, methods don't have names
        assertEquals(6, dMethods.get(1).getBlockMap().size());
    }

}
