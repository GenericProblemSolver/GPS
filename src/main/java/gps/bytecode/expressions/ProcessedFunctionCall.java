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
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import gps.bytecode.symexec.SEFunction;

/**
 * A function call with a defined number of arguments, 
 * no recalculation happens here and we do not depend on a 
 * Context (compare FunctionCall)
 * @author mfunk
 *
 */
public class ProcessedFunctionCall extends Expression {
    private final SEFunction function;

    private final Collection<Expression> parameters;

    private final Class<?> type;

    public ProcessedFunctionCall(SEFunction sefunction,
            Collection<Expression> expression) {
        function = sefunction;
        this.parameters = expression;
        type = sefunction.getReturnType();
    }

    public SEFunction getTargetFunction() {
        return function;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    public List<Expression> getParameters() {
        return new ArrayList<Expression>(parameters);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("( " + function.getName() + " ");
        for (Expression e : getParameters()) {
            b.append(e.toString() + " ");
        }
        b.append(") ");
        return b.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(function, parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProcessedFunctionCall)) {
            return false;
        }
        ProcessedFunctionCall e = (ProcessedFunctionCall) o;
        if (e.hashCode() != this.hashCode()) {
            return false;
        }
        if (!e.function.equals(this.function)) {
            return false;
        }
        if (!e.parameters.equals(this.parameters)) {
            return false;
        }
        return true;
    }

    @Override
    public void accept(IExpressionVisitor visitor) {
        visitor.visitProcessedFunctionCall(this);
    }

    @Override
    public List<FunctionCall> getFunctionCalls() {
        return new ArrayList<>();
    }

    @Override
    public void setType(Class<?> type) {
        // Does nothing
    }
}
