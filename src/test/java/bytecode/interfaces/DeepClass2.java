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
package bytecode.interfaces;

import gps.annotations.Optimize;
import gps.annotations.Variable;

public class DeepClass2 {

    @Variable(Variable.VariableDepth.Deep)
    public Class2 c1 = new Class2(0, 0);

    @Variable(Variable.VariableDepth.Deep)
    public Class2 c2 = new Class2(0, 0);

    @Optimize
    public double opt() {
        return (c1.i + c2.i) * (c1.z + c2.z);
    }

}
