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
package gps.bytecode.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gps.bytecode.symexec.SEFunction;

/**
 * Class representing a single atomic Variable inside an Expression
 * 
 * @author marlonfl
 *
 */
public class Variable extends Expression {

    /**
     * The name of the variable
     */
    private final String name;

    /**
     * type of the variable, e.g. int or boolean
     */
    private Class<?> type;

    public Variable(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    /**
     * This version creates a namespaced variable. Variables with the same name but from different SEFunctions don't collide
     * @param f
     * @param name
     * @param type
     */
    public Variable(SEFunction f, String name, Class<?> type) {
        this.name = f.getShortName() + "_" + name;
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public void accept(IExpressionVisitor visitor) {
        visitor.visitVariableExpression(this);
    }

    @Override
    public List<FunctionCall> getFunctionCalls() {
        return new ArrayList<>();
    }

    /**
     * Returns a new variable that can be used in a global context without collision
     * @param f
     * @return
     */
    public Variable intoNamespacedVariable(SEFunction f) {
        return new Variable(f, this.name, this.type);
    }

    /**
     * Equal if names are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Variable)) {
            return false;
        }
        Variable other = (Variable) obj;
        return other.name.equals(this.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
