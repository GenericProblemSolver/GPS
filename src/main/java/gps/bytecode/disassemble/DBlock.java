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
package gps.bytecode.disassemble;

import java.util.ArrayList;
import java.util.List;

import com.sun.tools.classfile.Instruction;

/**
 * 
 * This class represents a block of Instructions without any conditional
 * branches. Instructions in a single block can be dealt with linearly.
 * 
 * @author jowlo@uni-bremen.de
 *
 */
public class DBlock {

    public final List<Instruction> instructions = new ArrayList<Instruction>();

    /**
     * DMethod this block of instructions originated from.
     */
    public DMethod parentMethod;

    public DBlock(DMethod pParentMethod) {
        parentMethod = pParentMethod;
    }

    /**
     * Debug method
     */
    public void print() {
        Instruction s = instructions.get(0);
        System.out.println("Block at " + s.getPC());
        for (Instruction i : instructions) {
            System.out.print(i.getMnemonic());

            if (ClassDisassembler.isBranch(i)) {
                System.out.print(" " + ClassDisassembler.getJmp(i));
            }
            System.out.println();
        }
        System.out.println();
    }

}
