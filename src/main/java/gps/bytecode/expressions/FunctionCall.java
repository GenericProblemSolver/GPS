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

import gps.bytecode.symexec.Context;
import gps.bytecode.symexec.FieldRef;
import gps.bytecode.symexec.SEFunction;
import gps.util.Tuple;

/**
 * someone has to implement this
 * 
 * @author caspar
 *
 */
public class FunctionCall extends Expression {
    /*
     * The function that is called.
     */
    private final SEFunction targetFunction;

    /**
     * Context from which to take the parameters for the call
     */
    private final Context context;

    public FunctionCall(SEFunction pTargetFunction, Context ctx) {
        targetFunction = pTargetFunction;
        context = ctx;
    }

    public SEFunction getTargetFunction() {
        return targetFunction;
    }

    @Override
    public Class<?> getType() {
        return targetFunction.getReturnType();
    }

    @Override
    public void setType(Class<?> type) {
    }

    protected List<Expression> getStackParameters() {
        ArrayList<Expression> params = new ArrayList<>();
        for (int i = 0; i < context.stack.size()
                && i < targetFunction.stackParams.size(); ++i) {
            params.add(context.stack.get(context.stack.size() - i - 1));
        }
        return params;
    }

    protected List<Expression> getLocalParameters() {
        ArrayList<Expression> params = new ArrayList<>();
        for (Tuple<Integer, Context> localVarParT : targetFunction.localParams
                .keySet()) {
            Integer localVarPar = localVarParT.getX();
            Context fromContext = localVarParT.getY();
            if (fromContext == context.previousFrame
                    && context.locals[localVarPar] != null) {
                params.add(context.locals[localVarPar]);
            } else {
                params.add(targetFunction.localParams.get(localVarParT));
            }
        }
        return params;
    }

    protected List<Expression> getStaticParameters() {
        ArrayList<Expression> params = new ArrayList<>();
        for (FieldRef ref : targetFunction.staticParams.keySet()) {
            if (context.statics.get(ref) != null) {
                params.add(context.statics.get(ref));
            } else {
                params.add(targetFunction.staticParams.get(ref));
            }
        }
        return params;
    }

    /**
     * Returns an Iterable of all the Parameter expressions that are applied to
     * the targetfunction
     * 
     * @return
     */
    public List<Expression> getParameters() {
        ArrayList<Expression> params = new ArrayList<Expression>();
        params.addAll(getStackParameters());
        params.addAll(getLocalParameters());
        params.addAll(getStaticParameters());
        params.add(context.heap);
        params.add(context.heapsize);

        return params;
    }

    /**
     * A function call that handles the stack to local car conversion of a jvm
     * invoke
     * 
     * @author mfunk@tzi.de
     */
    public static class InvokeFunctionCall extends FunctionCall {
        public Expression[] params;

        public InvokeFunctionCall(SEFunction pTargetFunction, Context ctx,
                Expression[] params) {
            super(pTargetFunction, ctx);
            this.params = params;
        }

        @Override
        public List<Expression> getLocalParameters() {
            ArrayList<Expression> params = new ArrayList<>();

            for (Tuple<Integer, Context> localVarParT : super.targetFunction.localParams
                    .keySet()) {
                Integer localVarPar = localVarParT.getX();
                Context retCtx = localVarParT.getY();
                if (retCtx == super.context
                        && localVarPar < this.params.length) {
                    params.add(this.params[localVarPar]);
                } else if (retCtx == super.context.previousFrame
                        && super.context.locals[localVarPar] != null) {
                    params.add(super.context.locals[localVarPar]);
                } else {
                    params.add(
                            super.targetFunction.localParams.get(localVarParT));
                }
            }
            return params;
        }
    }

    /**
     * A function call that puts a value on the stack of the called function,
     * matching jvm return
     * 
     * @author mfunk@tzi.de
     */
    public static class ReturnFunctionCall extends FunctionCall {
        public final Expression returnvalue;

        public ReturnFunctionCall(SEFunction pTargetFunction, Context ctx,
                Expression returnvalue) {
            super(pTargetFunction, ctx);
            this.returnvalue = returnvalue;
        }

        @Override
        public List<Expression> getStackParameters() {
            ArrayList<Expression> params = new ArrayList<>();

            boolean retVal = returnvalue != null;
            // Fill in stack parameters
            for (int i = 0; i < super.context.stack.size()
                    && i < super.targetFunction.stackParams.size()
                            - (retVal ? 1 : 0); ++i) {
                params.add(super.context.stack
                        .get(super.context.stack.size() - i - 1));
            }

            if (retVal) {
                params.add(returnvalue);
            }

            return params;
        }

    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("( " + targetFunction.getName() + " ");
        for (Expression e : getParameters()) {
            b.append(e.toString() + " ");
        }
        b.append(" ");
        return b.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetFunction, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FunctionCall)) {
            return false;
        }
        FunctionCall e = (FunctionCall) o;
        if (e.hashCode() != this.hashCode()) {
            return false;
        }
        if (!e.targetFunction.equals(this.targetFunction)) {
            return false;
        }
        if (!e.context.equals(this.context)) {
            return false;
        }
        return true;
    }

    @Override
    public void accept(IExpressionVisitor visitor) {
        visitor.visitFunctionCall(this);
    }

    @Override
    public List<FunctionCall> getFunctionCalls() {
        ArrayList<FunctionCall> functionCalls = new ArrayList<>();
        functionCalls.add(this);
        return functionCalls;
    }
}
