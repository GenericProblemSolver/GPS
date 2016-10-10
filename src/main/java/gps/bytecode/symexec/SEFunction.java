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
package gps.bytecode.symexec;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import gps.bytecode.disassemble.DBlock;
import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Expression;
import gps.bytecode.expressions.FunctionCall;
import gps.bytecode.expressions.FunctionCall.InvokeFunctionCall;
import gps.bytecode.expressions.FunctionCall.ReturnFunctionCall;
import gps.bytecode.expressions.Operator.Undef;
import gps.bytecode.expressions.Variable;
import gps.util.Tuple;

/**
 *
 * A representation of a symbolically executed code block
 * 
 * @author jowlo@uni-bremen.de
 *
 */
public class SEFunction {

    /**
     * List containing all other SEFunctions that jumped in here, so they are
     * notified, how many parameters this SEFunctions retrieves from the stack.
     * 
     * By saving all parents we can propagate these changes through the whole
     * graph of SEFunction.
     */
    final List<SEFunction> observers = new ArrayList<>();

    /**
     * Local parameters of this SEFunction, indexed by index and jvm context
     * they originate from
     */
    public final Map<Tuple<Integer, Context>, Variable> localParams = new HashMap<>();

    public final ArrayList<Variable> stackParams = new ArrayList<>();

    public final Map<Integer, Variable> heapParams = new HashMap<>();

    public final Map<FieldRef, Variable> staticParams = new HashMap<>();

    final Context executionContext;

    Expression expression;

    public boolean recursiveFlag = true;

    // needed to get static objects from the classfile. Only needed in the first
    // function because of this
    public HeapContext hc;

    /**
     * true if this is the first SEFunction in a Constraint. Needed for
     * accurately setting static variables
     */
    public boolean firstFunction = false;

    /**
     * The code block that this function was created from.
     */
    final DBlock dBlock;

    private final String name;

    public SEFunction(DBlock pDBlock, SymbolicExec symexec,
            Context invokedFrom) {
        dBlock = pDBlock;

        name = dBlock.parentMethod.getName() + "_"
                + dBlock.instructions.get(0).getPC();
        executionContext = new Context(symexec, this, invokedFrom);
    }

    /**
     * Constructor for generated SEFunctions. Do not use unless you know what
     * you are doing
     * 
     * @param name
     */
    public SEFunction(String name, Expression e, List<Variable> params) {
        this.name = name;
        executionContext = null;
        dBlock = null;
        expression = e;
        this.stackParams.addAll(params);
    }

    /**
     * Notify this SEFunction of Need of stack/context variables **from** this
     * SEFunction.
     * 
     * @param notification
     *            The SEFunction that has changed or null if all called
     *            functions should be checked.
     */
    public void notify(SEFunction notification) {

        if (expression == null) { // If this function is not yet initialized,
                                      // there is nothing to do
            return;
        }

        boolean changesMade = false;
        for (FunctionCall fcall : expression.getFunctionCalls()) {
            SEFunction target = fcall.getTargetFunction();
            if (!target.equals(notification) && notification != null) {
                continue;
            }

            // Add missing stack parameters
            int numberOfRequiredStackParams = target.stackParams.size();
            final int numberOfAvailableStackParams = executionContext.stack
                    .size();
            // Don't request a stackparam if a returnfunctioncall handles it
            if (fcall instanceof ReturnFunctionCall) {
                if (((ReturnFunctionCall) fcall).returnvalue != null) {
                    numberOfRequiredStackParams -= 1;
                }
            }
            for (int i = numberOfAvailableStackParams; i < numberOfRequiredStackParams; i++) {
                // TODO: fix Undef
                Expression var = requestStackParameter(Undef.class);
                // add new variable to the bottom of the stack
                executionContext.stack.add(0, var);
                changesMade = true;
            }

            // Add missing local variable parameters
            for (Tuple<Integer, Context> paramt : target.localParams.keySet()) {
                Integer param = paramt.getX();
                Context retCtx = paramt.getY();
                // Don't request more parameters if these are handled by an
                // InvokeFunctionCall
                if (fcall instanceof FunctionCall.InvokeFunctionCall) {
                    InvokeFunctionCall ifc = (InvokeFunctionCall) fcall;
                    if (retCtx == executionContext
                            && param < ifc.params.length) {
                        continue;
                    }
                }

                if (retCtx == executionContext.previousFrame) {
                    if (!localParams.containsKey(paramt)
                            && executionContext.locals[param] == null) {
                        requestLocalVariableParameter(
                                target.localParams.get(paramt).getType(),
                                param);
                        changesMade = true;
                    }
                } else {
                    if (!localParams.containsKey(paramt)) {
                        requestLocalVariableParameterFromContext(
                                target.localParams.get(paramt).getType(), param,
                                retCtx);
                        changesMade = true;
                    }
                }

            }

            // Add missing static parameters
            for (FieldRef ref : fcall.getTargetFunction().staticParams
                    .keySet()) {
                // If the requested static parameter is neither already
                // requested, nor created in this function, it has to be
                // requested
                if (!this.staticParams.containsKey(ref)
                        && executionContext.statics.get(ref) == null) {
                    requestStaticParameter(
                            fcall.getTargetFunction().staticParams.get(ref)
                                    .getType(),
                            ref);
                    changesMade = true;
                }

            }

        }

        if (changesMade) {
            notifyObservers();
        }
        return;
    }

    public void notifyObservers() {
        for (SEFunction o : observers) {
            o.notify(this);
        }
    }

    public List<Variable> getParameters() {
        ArrayList<Variable> vs = new ArrayList<>(stackParams);
        vs.addAll(localParams.values());
        vs.addAll(staticParams.values());
        vs.addAll(heapParams.values());
        return vs;
    }

    /**
     * Add an observer to this object. If this Function (or following) needs
     * further variables from its "invoker", the observer will be notified.
     * 
     * @param observer
     */
    public void register(SEFunction observer) {
        observers.add(observer);
    }

    /**
     * Answers a request of an invoked function for parameters.
     * 
     * @param cl
     *            Type of requested parameter.
     * @return expression
     */
    public Expression requestStackParameter(Class<?> cl) {
        Variable ret = new Variable("stack" + stackParams.size(), cl);
        stackParams.add(0, ret);
        // We don't notifyObservers(); here, but in the notify method
        return ret;
    }

    public Expression requestLocalVariableParameter(Class<?> cl, int index) {
        // We don't notifyObservers(); here, but in the notify method
        Context c = executionContext.previousFrame;
        return requestLocalVariableParameterFromContext(cl, index, c);
    }

    public Expression requestLocalVariableParameterFromContext(Class<?> cl,
            int index, Context c) {
        int h = Objects.hash(c) % 1000;
        Variable ret = new Variable("x" + index + "_" + h, cl);
        localParams.put(new Tuple<>(index, c), ret);
        // We don't notifyObservers(); here, but in the notify method
        return ret;
    }

    public Expression requestHeapParameter(Class<?> cl, int address) {
        if (heapParams.containsKey(address)) {
            return heapParams.get(address);
        }
        Variable ret = new Variable("heap" + address, cl);
        heapParams.put(address, ret);
        // We don't notifyObservers(); here, but in the notify method
        return ret;
    }

    public Expression requestStaticParameter(Class<?> cl, FieldRef ref) {
        if (staticParams.containsKey(ref)) {
            return staticParams.get(ref);
        } else if (firstFunction) {
            Expression ret = getStaticFieldValue(ref);
            executionContext.statics.put(ref, ret);
            return ret;
        } else {
            Variable ret = new Variable("static" + ref.hashCode(), cl);
            staticParams.put(ref, ret);
            return ret;
        }
    }

    public String getName() {
        return this.name;
    }

    public String getShortName() {
        if (executionContext != null) {
            return name.split("_")[0] + "_" + Integer.toHexString(
                    Objects.hash(name, executionContext.previousFrame));
        } else {
            return name.split("_")[0] + "_"
                    + Integer.toHexString(Objects.hash(name));
        }
    }

    public Class<?> getReturnType() {
        if (expression == null) {
            // This happens if this function has not been typed yet. Returning
            // Undef will cause the surrounding expression to deduce the type
            // from somewhere else
            return Undef.class;
        }
        return expression.getType();
    }

    public Expression asExpression() {
        return expression;
    }

    /**
     * THis is an ugly workaround, since we cannot construct new SEFunctions and
     * insert them into the problem, since functioncalls have an explicit
     * reference to one specific instance
     * 
     * @param e
     */
    public void setExpression(Expression e) {
        expression = e;
    }

    /**
     * Get the value of a (static) field by a given FieldRef.
     * 
     * @param ref
     * @return
     */
    public Expression getStaticFieldValue(FieldRef ref) {
        try {
            // get all declared fields of the class of the referenced field
            Class<?> declaringClass = Class.forName(ref.className);
            ArrayList<Field> fields = HeapContext.getAllFields(declaringClass);
            // search the referenced field by name
            for (Field f : fields) {
                if (ref.name.equals(f.getName())) {
                    // fields.get(null) is used for static fields
                    Object fieldValue = f.get(null);
                    // this is necessary because otherwise Integer and
                    // int would be seen as different types
                    Class<?> c = fieldValue.getClass();
                    switch (ref.type) {
                    case "I":
                        c = int.class;
                        break;
                    case "S":
                        c = short.class;
                        break;
                    case "D":
                        c = double.class;
                        break;
                    case "B":
                        c = byte.class;
                        break;
                    case "C":
                        c = char.class;
                        break;
                    case "Z":
                        c = boolean.class;
                        break;
                    case "J":
                        c = long.class;
                        break;
                    case "F":
                        c = float.class;
                        break;
                    default:
                        // The field contains an Object, this is put onto
                        // the heap, returning the address on the heap
                        return hc.addObjectToHeap(executionContext, fieldValue);
                    }
                    // otherwise the field contains a primitive type and a
                    // Constant with its value is returned
                    return new Constant(c, fieldValue);
                }
            }
        } catch (SecurityException | ClassNotFoundException
                | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(
                    "Error retrieving value of static field: "
                            + e.getMessage());
        }
        // TODO this should probably be changed to IncoherentBytecodeExcepion,
        // which lies on a different branch
        throw new RuntimeException("Field is null");
    }

}
