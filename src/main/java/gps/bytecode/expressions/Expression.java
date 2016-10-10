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

import java.util.List;

/**
 * abstract superclass for all expression types
 * 
 * @author marlonfl
 *
 */
public abstract class Expression {
    public abstract Class<?> getType();

    public abstract void accept(IExpressionVisitor visitor);

    public abstract void setType(Class<?> type);

    /**
     * Returns the function calls used in this expression.
     */
    public abstract List<FunctionCall> getFunctionCalls();
}
