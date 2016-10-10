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
package gps.bytecode.backends;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.tools.javac.util.Pair;

import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Expression;
import gps.bytecode.expressions.OperationExpression;
import gps.bytecode.expressions.Operator;
import gps.bytecode.expressions.Operator.Reference;
import gps.bytecode.expressions.Operator.Undef;
import gps.bytecode.expressions.ProcessedFunctionCall;
import gps.bytecode.expressions.Variable;
import gps.bytecode.symexec.SEFunction;
import gps.bytecode.symexec.SatisfactionProblem;
import gps.bytecode.transforms.TransformationPasses;
import gps.util.CommandAvailability;

/**
 * Backend for solving Functional Constraint Satisfaction Problems with Leon.
 * 
 * Of course, you also need to install Leon, which is explained here.
 * https://leon.epfl.ch/doc/installation.html You'll also need to do something
 * like sudo ln -s
 * /home/caspar/Dokumente/Informatik/Informatik-Studium/GPS/leon/leon
 * /usr/local/bin so that bash finds the leon command. (This is not done
 * automatically while installing Leon.)
 * Add proper Exception handling in backends
 * 
 * For rudimentary testing, you can also copy the generated Leon/Scala code into
 * the online version of Leon: http://leon.epfl.ch/
 * 
 * @author oesterca
 */
public class LeonBackend implements IBytecodeBackend {

    /**
     * Scala code for an int-to-char-cast.
     */
    private static String intToChar;

    /**
     * Scala code for a char-to-int cast.
     */
    private static String charToInt;

    /**
     * Create intToChar and charToInt code. Basically the cast is done via a large pattern matching and if-then-else mechanism.
     */
    static {
        StringBuilder intToCharSB = new StringBuilder();
        intToCharSB.append("    def intToChar(n : BigInt) : Char= {\n");
        intToCharSB.append("        n match {\n");

        StringBuilder charToIntSB = new StringBuilder();
        charToIntSB.append("    def charToInt(c : Char) : BigInt= {\n");
        charToIntSB.append("        ");
        for (int i = 0; i < 256; i++) {
            //If it's biginteger i map to byte/char i, which we will define via hexcodes
            String bigint = "BigInt(" + i + ") ";
            String character = "\'\\u"
                    + Integer.toHexString((char) i | 0x10000).substring(1)
                    + "\'";
            intToCharSB.append(
                    "            case " + bigint + " => " + character + "\n");
            //Same from char to int
            charToIntSB.append(" if (c==" + character + "){\n");
            charToIntSB.append("            " + bigint + "\n");
            charToIntSB.append("        } else");
        }
        //default cases:
        intToCharSB.append("            case _ => \' \'\n");
        charToIntSB.append("{\n");
        charToIntSB.append("            BigInt(-42)\n");
        charToIntSB.append("        }\n");
        intToCharSB.append("        }\n");
        intToCharSB.append("    }");
        charToIntSB.append("    }");

        intToChar = intToCharSB.toString().replace("u005c", "\\");
        //\u005c is the backslash and therefore has to be escaped
        charToInt = charToIntSB.toString().replace("u005c", "\\");
    }

    /**
     * Translates a SatisfactionProblem in an internal representation into a
     * "Scala Core" String that Leon ( http://leon.epfl.ch/ ) expects as input.
     * 
     * @param problem
     *            SatisfactionProblem that is to be translated.
     * @return a String representing the SatisfactionProblem
     * Add proper Exception handling in backends
     */
    public static Pair<HashMap<Variable, Integer>, String> toLeon(
            SatisfactionProblem problem) {

        //Apply Transformation
        TransformationPasses.transform(problem); //should not be used because this would also do PartialEvalutation, which we don't want. Instead, we want:
        /*TransformationPasses.applySinglePass(problem,
                new TransformationPasses.BooleanTransform());
        for (int i = 0; i < 10; ++i) {
            TransformationPasses.applySinglePass(problem,
                    new TransformationPasses.RemoveUnusedParameters());
            TransformationPasses.applySinglePass(problem,
                    new TransformationPasses.InlineConstants());
            TransformationPasses.applySinglePass(problem,
                    new TransformationPasses.InlineId());
            TransformationPasses.applySinglePass(problem,
                    new TransformationPasses.InlineTailCall());
            TransformationPasses.applySinglePass(problem,
                    new TransformationPasses.ConstantEval());
        }
        TransformationPasses.applySinglePass(problem,
                new TransformationPasses.ReplaceCmp());
        TransformationPasses.applySinglePass(problem,
                new TransformationPasses.TypeUndefParams());
        TransformationPasses.applySinglePass(problem,
                new TransformationPasses.ReplaceBooleanToInt());*/
        //Unfortunately, this causes many tests to fail...

        StringBuilder code = new StringBuilder();
        code.append("import leon.lang._\n" + "import leon.lang.synthesis._\n"
                + "import leon.annotation._\n" + "import leon._\n\n"
                + "object GPS {\n");
        List<SEFunction> functions = BackendUtil.getOrderedFunctions(problem);
        for (SEFunction function : functions) {
            code.append(toLeon(function));
            code.append("\n\n");
        }

        //specify the solution via the choose construct

        code.append("    def sol : (");

        //Fill in the type of the solution, assuming that all constraints have the same parameters
        LinkedHashMap<SEFunction, LinkedHashMap<Variable, Variable>> nonHeapPars = new LinkedHashMap<>(); //Maps functions onto maps from their old Variables to their new Variables
        //HashMap<Variable, SEFunction> nonHeapPars = new HashMap<>();
        //To make sure that we don't use a heap variable more than once we remember which ones are already in use
        LinkedHashMap<Variable, Variable> usedHeapVars = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> typeCounts = new LinkedHashMap<>();

        StringBuilder arrayModeTypeDecls = new StringBuilder();

        HashMap<Variable, Integer> arrayModeVariablePositions = new HashMap<>();
        HashMap<Variable, Integer> singleModeVariablePositions = new HashMap<>();

        for (SEFunction constraint : problem.constraints) {
            if (!constraint.getParameters().isEmpty()) {
                nonHeapPars.put(constraint, new LinkedHashMap<>());
            }
            for (Variable par : constraint.getParameters()) {
                Variable nameSpacedPar = Z3JavaApiBackend
                        .globalParameterValue(problem, constraint, par);
                if (usedHeapVars.containsKey(nameSpacedPar)) {
                    continue;
                }
                if (!typeCounts
                        .containsKey(typeToLeon(nameSpacedPar.getType()))) {
                    typeCounts.put(typeToLeon(nameSpacedPar.getType()), 0);
                    arrayModeTypeDecls.append("Array["
                            + typeToLeon(nameSpacedPar.getType()) + "]");
                    arrayModeTypeDecls.append(", ");
                }
                if (nameSpacedPar.toString().startsWith("h")) {//These seem to be heap variables which need to be the same in all constraints
                    //code.append(typeToLeon(par.getType())); Still an option if the array thing is inefficient
                    Variable nPar = new Variable(
                            typeToLeon(nameSpacedPar.getType()) + "s("
                                    + typeCounts.get(
                                            typeToLeon(nameSpacedPar.getType()))
                                    + ")",
                            nameSpacedPar.getType());
                    usedHeapVars.put(nameSpacedPar, nPar);
                } else {
                    //code.append(typeToLeon(par.getType()));
                    //System.out.println(nameSpacedPar);

                    Variable nPar = new Variable(
                            typeToLeon(nameSpacedPar.getType()) + "s("
                                    + typeCounts.get(
                                            typeToLeon(nameSpacedPar.getType()))
                                    + ")",
                            nameSpacedPar.getType());
                    nonHeapPars.get(constraint).put(nameSpacedPar, nPar);
                }
                typeCounts.put(typeToLeon(nameSpacedPar.getType()),
                        typeCounts.get(typeToLeon(nameSpacedPar.getType()))
                                + 1);

            }
        }

        boolean arrayMode = sumOfVals(typeCounts) > 5;
        if (arrayMode) {
            code.append(arrayModeTypeDecls);
            code.delete(code.length() - 2, code.length());
        } else {
            for (SEFunction sefunct : nonHeapPars.keySet()) {
                for (Variable oldVar : nonHeapPars.get(sefunct).keySet()) {
                    code.append(typeToLeon(oldVar.getType()));
                    code.append(", ");

                }
            }

            for (Variable oldVar : usedHeapVars.keySet()) {
                code.append(typeToLeon(oldVar.getType()));
                code.append(", ");
            }
            if (nonHeapPars.isEmpty() && usedHeapVars.isEmpty()) {
                //add dummy parameter
                code.append("Int");
            } else {
                //Delete last ", "
                code.delete(code.length() - 2, code.length());
            }

        }

        code.append(") = {\n");
        code.append("        choose{(");

        if (arrayMode) {
            for (String type : typeCounts.keySet()) {
                code.append(type + "s : Array[" + type + "]");
                code.append(", ");
            }

            code.delete(code.length() - 2, code.length());

            code.append(") => (");

            //List sizes of arrays
            for (String type : typeCounts.keySet()) {
                code.append(type + "s.length==" + typeCounts.get(type));
                code.append("&& ");
            }
        } else {
            int singleModeCounter = 0;

            for (SEFunction sefunct : nonHeapPars.keySet()) {
                for (Variable oldVar : nonHeapPars.get(sefunct).keySet()) {
                    singleModeVariablePositions.put(oldVar,
                            singleModeCounter++);
                    //System.out.println("var: " + oldVar);
                    Variable newVar = nonHeapPars.get(sefunct).get(oldVar);
                    code.append(newVar.toString().replace("(", "").replace(")",
                            ""));
                    code.append(": ");
                    code.append(typeToLeon(newVar.getType()));
                    code.append(", ");

                }
            }

            for (Variable oldVar : usedHeapVars.keySet()) {
                singleModeVariablePositions.put(oldVar, singleModeCounter++);
                //System.out.println("var:" + oldVar);

                Variable newVar = usedHeapVars.get(oldVar);
                code.append(
                        newVar.toString().replace("(", "").replace(")", ""));
                code.append(": ");
                code.append(typeToLeon(newVar.getType()));
                code.append(", ");
            }
            if (nonHeapPars.isEmpty() && usedHeapVars.isEmpty()) {
                //add dummy parameter
                code.append("dummy : Int");
            } else {
                //Delete last ", "
                code.delete(code.length() - 2, code.length());
            }
            code.append(") => (");
        }

        //List constraints
        for (SEFunction constraint : problem.constraints) {
            code.append(constraint.getName());
            if (!constraint.getParameters().isEmpty()) {
                //Insert parameters:
                code.append("(");
                for (Variable par : constraint.getParameters()) {
                    Variable nameSpacedPar = Z3JavaApiBackend
                            .globalParameterValue(problem, constraint, par);
                    if (par.toString().startsWith("h")) {
                        code.append(
                                arrayMode ? usedHeapVars.get(nameSpacedPar)
                                        : usedHeapVars.get(nameSpacedPar)
                                                .toString().replace("(", "")
                                                .replace(")", ""));
                    } else {
                        code.append(arrayMode
                                ? nonHeapPars.get(constraint).get(nameSpacedPar)
                                : nonHeapPars.get(constraint).get(nameSpacedPar)
                                        .toString().replace("(", "")
                                        .replace(")", ""));
                    }
                    code.append(", ");
                }
                //Delete last ", "
                code.delete(code.length() - 2, code.length());
                code.append(")");
            }
            code.append(" &&");
        }
        // Delete last " &&"
        code.delete(code.length() - 3, code.length());

        code.append(")}\n    }\n");
        code.append("\n");
        code.append(intToChar);
        code.append("\n");
        code.append("\n");
        code.append(charToInt);
        code.append("\n");
        code.append("}");

        //System.out.println(code);

        return new Pair<>(arrayMode ? arrayModeVariablePositions
                : singleModeVariablePositions, code.toString());
    }

    /**
     * Translate a function into Leon.
     * 
     * @param function
     *            A Function to be translated into Leon
     * @return a String that declares the given Function into Leon.
     */
    public static String toLeon(SEFunction function) {

        StringBuilder sb = new StringBuilder();
        sb.append("    def ");
        sb.append(nameToLeon(function.getName()));
        if (!function.getParameters().isEmpty()) {
            sb.append("(");
            for (Variable par : function.getParameters()) {
                sb.append(par.toString());
                sb.append(": ");
                sb.append(typeToLeon(par.getType()));
                sb.append(", ");
            }
            //Delete last ", "
            sb.delete(sb.length() - 2, sb.length());
            sb.append(")");
        }
        sb.append(" : ");
        sb.append(typeToLeon(function.getReturnType()));
        sb.append(" = {");
        sb.append("\n");
        sb.append("        ");
        sb.append(expressionToLeon(function.asExpression()));
        sb.append("\n    }");
        return sb.toString();
    }

    /**
     * Returns a String that represents the given type in Leon/Scala
     * 
     * @param type
     *            Java type that is to be translated into a Leon/Scala type
     * @return equivalent of the given type in Leon/Scala
     */
    public static String typeToLeon(Class<?> type) {
        if (type.equals(int.class) || type.equals(byte.class)
                || type.equals(short.class) || type.equals(long.class)
                || type.equals(Reference.class) || type.equals(Undef.class)) {
            return "BigInt";
        } else if (type.equals(double.class) || type.equals(float.class)) {
            return "Rational";
        } else if (type.equals(boolean.class)) {
            return "Boolean";
        } else if (type.equals(char.class)) {
            return "Char";
        }
        return "UnknownType" + type;

    }

    /**
     * Translates an expression into a Leon expression / term.
     * 
     * @param expr
     *            expression that is to be translated
     * @return StringBuilder containing a String that represents the given
     *         expression
     */
    public static StringBuilder expressionToLeon(Expression expr) {
        StringBuilder sb = new StringBuilder();
        if (expr instanceof ProcessedFunctionCall) {
            ProcessedFunctionCall fcexpr = (ProcessedFunctionCall) expr;
            String name = nameToLeon(fcexpr.getTargetFunction().getName());

            if (fcexpr.getTargetFunction().getParameters().isEmpty()) {
                // Syntax for "constants"
                sb.append(name);
            } else {
                sb.append("( " + name);
                sb.append("(");

                for (Expression e : fcexpr.getParameters()) {
                    sb.append(expressionToLeon(e));
                    sb.append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
                sb.append("))");
            }
        } else if (expr instanceof Constant) {

            if (expr.getType() == double.class
                    || expr.getType() == float.class) {
                sb.append(doubleToLeonRational(expr.toString()));
            } else if (typeToLeon(expr.getType()).equals("BigInt")) {
                sb.append("BigInt(" + expr.toString() + ")");
            } else if (expr.getType() == char.class) {
                sb.append("\'" + expr.toString() + "\'");
            } else {
                /*System.out.println("Constant:");
                System.out.println("value:" + expr.toString());
                System.out.println("type:" + expr.getZ3Type());
                System.out.println("type:" + expr.getType());*/
                sb.append(expr.toString());
            }
        } else if (expr instanceof OperationExpression) {
            OperationExpression opexpr = (OperationExpression) expr;
            if (opexpr.getParameters().size() == 2) {
                sb.append("(");
                sb.append(expressionToLeon(opexpr.getParameters().get(0)));
                sb.append(" ");
                sb.append(operatorNameToLeon(opexpr.getOperator()));
                sb.append(" ");
                sb.append(expressionToLeon(opexpr.getParameters().get(1)));
                sb.append(")");
            } else if (opexpr.getOperator().equals(Operator.ITE)) {
                sb.append("(if (");
                sb.append(expressionToLeon(opexpr.getParameters().get(0)));
                sb.append(") ");
                sb.append(expressionToLeon(opexpr.getParameters().get(1)));
                sb.append(" else ");
                sb.append(expressionToLeon(opexpr.getParameters().get(2)));
                sb.append(")");
            } else if (opexpr.getParameters().size() == 1) {
                //This is a type cast
                //First check whether it is an unnecessary type cast

                if (opexpr.getOperator().isTypeCast()
                        && typeToLeon(opexpr.getOperator().getOutputTypes()[0])
                                .equals(typeToLeon(opexpr.getParameters().get(0)
                                        .getType()))) {
                    return expressionToLeon(opexpr.getParameters().get(0));
                }

                if ((opexpr.getOperator() == Operator.TO_DOUBLE
                        || opexpr.getOperator() == Operator.TO_FLOAT)
                        && opexpr.getParameters().get(0).getType()
                                .equals(int.class)) {
                    sb.append("Rational(");
                    sb.append(expressionToLeon(opexpr.getParameters().get(0)));
                    sb.append(",BigInt(1))");
                } else if (opexpr.getOperator() == Operator.TO_INT && (opexpr
                        .getParameters().get(0).getType().equals(double.class)
                        || opexpr.getParameters().get(0).getType()
                                .equals(float.class))) {
                    String parToBeCast = expressionToLeon(
                            opexpr.getParameters().get(0)).toString();
                    sb.append("((" + parToBeCast + ").numerator");
                    sb.append("/");
                    sb.append("(" + parToBeCast + ").denominator)");
                } else if (opexpr.getOperator() == Operator.TO_CHAR
                        && typeToLeon(opexpr.getParameters().get(0).getType())
                                .equals("BigInt")) {
                    sb.append("intToChar(");
                    sb.append(expressionToLeon(opexpr.getParameters().get(0))
                            .toString());
                    sb.append(")");
                } else if ((opexpr.getOperator() == Operator.TO_INT
                        || opexpr.getOperator() == Operator.TO_BYTE
                        || opexpr.getOperator() == Operator.TO_LONG
                        || opexpr.getOperator() == Operator.TO_SHORT)
                        && typeToLeon(opexpr.getParameters().get(0).getType())
                                .equals("Char")) {
                    sb.append("charToInt(");
                    sb.append(expressionToLeon(opexpr.getParameters().get(0))
                            .toString());
                    sb.append(")");
                } else if (opexpr.getOperator().toString().startsWith("to")) {
                    //System.out.println(opexpr.getOperator().toString());
                    sb.append(expressionToLeon(opexpr.getParameters().get(0)));
                    sb.append(".asInstanceOf[");
                    sb.append(typeToLeon(
                            (opexpr.getOperator().getOutputTypes()[0])));
                    sb.append("]");
                } else {
                    sb.append("(");
                    sb.append(operatorNameToLeon(opexpr.getOperator()));
                    sb.append(" ");
                    sb.append(expressionToLeon(opexpr.getParameters().get(0)));
                    sb.append(")");
                }
            }
        } else if (expr instanceof Variable) {
            sb.append(expr.toString());
        }
        return sb;
    }

    /**
     * Translate rational numbers in Leon notation.
     * 
     * @param x String representing a rational number.
     * @return String representing the rational number in Leon.
     */
    public static String doubleToLeonRational(String x) {
        StringBuilder resultsb = new StringBuilder("Rational(");
        String[] xprepostdecimalpt = x.split("\\."); //The . needs escaping, because split expects a regex
        if (xprepostdecimalpt[0].startsWith("0")) { //Omit leading zero
            xprepostdecimalpt[0] = xprepostdecimalpt[0].substring(1,
                    xprepostdecimalpt[0].length());
        }
        resultsb.append(xprepostdecimalpt[0]);
        resultsb.append(xprepostdecimalpt[1]);
        resultsb.append(",");
        resultsb.append("" + (int) Math.pow(10, xprepostdecimalpt[1].length()));
        resultsb.append(")");
        return resultsb.toString();
    }

    /**
     * Translates operator names from the internal standard to Leon.
     * 
     * @param op
     *            operator that is to be translated
     * @return String referring to given Operator in Leon / Scala
     */
    public static String operatorNameToLeon(Operator op) {
        if (op.equals(Operator.REM)) {
            return "%";
        } else if (op.equals(Operator.NOT)) {
            return "!";
        } else if (op.equals(Operator.EQUAL)) {
            return "==";
        } else if (op.equals(Operator.BAND)) {// yes, BAND stands for boolean AND, not bitwise AND. Seriously...
            return "&&";
        } else if (op.equals(Operator.OR)) {
            return "||";
        }
        return op.toString();
    }

    /**
     * Applies the external Leon program to the problem specified in the given
     * string.
     * 
     * @param problem
     * @return
     * @throws FileNotFoundException
     */
    public static String applyLeon(String problem) {
        //write the problem into a file
        String tempProblemFileName = ".problem_temp.scala";
        try (PrintWriter out = new PrintWriter(tempProblemFileName)) {
            out.println(problem);
        } catch (FileNotFoundException e1) {
            throw new IllegalStateException(
                    "Something weird happened while writing a file.");
        }

        // execute leon command:
        // http://stackoverflow.com/questions/3403226/how-to-run-linux-commands-in-java-code
        StringBuilder result = new StringBuilder();
        String s;
        Process p;
        try {
            p = Runtime.getRuntime()
                    .exec("leon " + tempProblemFileName + " --synthesis");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null) {
                result.append(s + "\n");
            }
            p.waitFor();
            p.destroy();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Leon execution did not terminate successfully.");
        }

        // Delete temporary file
        // https://docs.oracle.com/javase/tutorial/essential/io/delete.html
        Path tempProblemFilePath = Paths.get(tempProblemFileName);
        try {
            Files.delete(tempProblemFilePath);
        } catch (NoSuchFileException x) {
            throw new IllegalStateException(
                    "Weird!! We created a file and then it disappeared without our deleting it.");
        } catch (DirectoryNotEmptyException x) {
            throw new IllegalStateException(
                    "This should never happen because the target file is no directory.");
        } catch (IOException x) {
            throw new IllegalStateException(
                    "Weird! We created a file and then we did not have access rights to it.");
        }

        return result.toString();
    }

    /**
     * Solves the given satisfaction problem.
     */
    @Override
    public BackendResult solve(SatisfactionProblem problem) {
        if (problem.constraints.isEmpty()) {
            System.out.println(
                    "Problem has no constraints and is therefore trivial. Use of Leon is not required.");
            System.out.println(
                    "Quote of the day: I'll build a great, great bytecode and I'll have DeepSpace pay for that bytecode. Mark my words!");
            problem.assignSolution(new SatisfactionProblemSolution(true,
                    "no constraints, therefore trivial"));
            return BackendResult.SOLVED;
        }
        Pair<HashMap<Variable, Integer>, String> problemTranslation = toLeon(
                problem);
        String leonProblem = problemTranslation.snd;
        String leonResult = applyLeon(leonProblem);
        //System.out.println(leonResult);

        problem.assignSolution(extractSolutionFromLeonOutput(leonResult,
                problemTranslation.fst));
        return BackendResult.SOLVED;
    }

    /**
     * Extracts a satisfaction problem solution from the given leon output.
     */
    public static SatisfactionProblemSolution extractSolutionFromLeonOutput(
            String leonOutput, Map<Variable, Integer> map) {
        //System.out.println("Leon result:");
        //System.out.println(leonOutput);
        String[] leonResultArr = leonOutput.split("\n");
        SatisfactionProblemSolution SPsolution = null;
        if (leonOutput.contains(" is UNSAT") && leonOutput.contains("error")
                && leonOutput.contains("Impossible program")) { //better safe than sorry
            SPsolution = new SatisfactionProblemSolution(false, leonOutput);
        } else if (!leonOutput.contains(" == 1 is UNSAT!")
                && !leonOutput.contains("error")
                && !leonOutput.contains("Impossible program")
                && leonOutput.contains(" def sol(): ")
                && leonOutput.contains("======== sol =========")) {
            //Find the line which contains the solution

            if (map.isEmpty()) { //If the problem has no parameters there is not much to do
                return new SatisfactionProblemSolution(true, "");
            }

            int i;
            for (i = 0; i < leonResultArr.length; i++) {
                if (leonResultArr[i].contains(" def sol(): ")) {
                    break;
                }
            }

            //Eliminate leading bs
            String[] solutionLine = leonResultArr[i + 1].split("]");
            String sol = solutionLine[solutionLine.length - 1];

            sol = sol.replaceAll(" ", "");
            //System.out.println(map.size());
            if (map.keySet().size() > 1) {
                if (!(sol.startsWith("(") && sol.endsWith(")"))) {
                    throw new IllegalStateException();
                }
                sol = sol.substring(1, sol.length() - 1);
            }

            /*System.out.println("Solution string:");
            System.out.println(sol);*/

            String[] broken = sol.split(",");

            ArrayList<String> solutions = new ArrayList<>();

            for (int n = 0; n < broken.length; n++) {
                if (broken[n].startsWith("Rational")) {
                    solutions.add(broken[n] + "," + broken[++n]);
                } else {
                    solutions.add(broken[n]);
                }
            }

            boolean arrayMode = true; //TODO
            HashMap<Variable, Constant> variableToSolution = new HashMap<>();
            if (arrayMode) {
                //TODO
                //With Leon, array stuff does not seem to work at this point, so why bother
            } else {
                for (Variable mappedVariable : map.keySet()) {
                    Class<?> type = mappedVariable.getType();

                    variableToSolution.put(mappedVariable,
                            parseLeonOutputExpression(
                                    solutions.get(map.get(mappedVariable)),
                                    type));
                }
            }
            /*for (Variable var : variableToSolution.keySet()) {
                System.out.println(var + " to " + variableToSolution.get(var));
            }*/

            SPsolution = new SatisfactionProblemSolution(true, sol);
            SPsolution.variableValues = variableToSolution;
        } else if (leonOutput.contains("Exception")) {
            throw new IllegalStateException(
                    "Leon Exception, probably because of Array");
        } else {
            //In this case, it is unclear what happened
            throw new IllegalStateException(
                    "Somehow we could not see whether Leon saw the problem as solvable or not...");
        }
        return SPsolution;
    }

    /**
     * Parse Leon expression to a Constant of the given type.
     */
    public static Constant parseLeonOutputExpression(String leonexpr,
            Class<?> targetType) {
        if (leonexpr.startsWith("BigInt")) {
            BigInteger bigint = bigIntExtract(leonexpr);
            if (targetType == byte.class
                    && bigint.compareTo(BigInteger.valueOf(Byte.MIN_VALUE)) >= 0
                    && bigint.compareTo(
                            BigInteger.valueOf(Byte.MAX_VALUE)) <= 0) {
                return new Constant(bigint.intValueExact());
            }
            if (targetType == int.class
                    && bigint.compareTo(
                            BigInteger.valueOf(Integer.MIN_VALUE)) >= 0
                    && bigint.compareTo(
                            BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
                return new Constant(bigint.intValueExact());
            }
            if (targetType == long.class
                    && bigint.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0
                    && bigint.compareTo(
                            BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
                return new Constant(bigint.longValueExact());
            }
            if (targetType == BigInteger.class) {
                return new Constant(BigInteger.class, bigint);
            }
            throw new IllegalArgumentException();
        } else if (leonexpr.startsWith("Rational")) {
            //System.out.println(leonexpr);
            String enumeratorAndDenominator = leonexpr.substring(9,
                    leonexpr.length() - 1);
            //System.out.println(enumeratorAndDenominator);
            BigDecimal enumerator = new BigDecimal((BigInteger) bigIntExtract(
                    enumeratorAndDenominator.split(",")[0]));
            BigDecimal denominator = new BigDecimal((BigInteger) bigIntExtract(
                    enumeratorAndDenominator.split(",")[1]));
            //System.out.println(enumerator);
            //System.out.println(denominator);
            return new Constant(BigDecimal.class,
                    enumerator.divide(denominator, BigDecimal.ROUND_HALF_EVEN));
        }
        /*System.out.println("......");
        System.out.println(leonexpr);
        System.out.println(leonexpr.length());*/
        throw new IllegalStateException();
    }

    /**
     * Extract BigInteger from the given Leon String.
     */
    public static BigInteger bigIntExtract(String bigIntString) {
        //System.out.println(bigIntString);
        String integer = bigIntString.substring(7, bigIntString.length() - 1);
        //System.out.println(integer);
        BigInteger bigint = new BigInteger(integer);
        return bigint;
    }

    @Override
    public boolean isAvailable() {
        return CommandAvailability.isAvailable("leon");
    }

    /**
     * Leon does not allow names with a . or a - in them, so this method removes them.
     */
    public static String nameToLeon(String name) {
        return name.replaceAll("\\.", "pt").replaceAll("-", "minus");
    }

    /**
     * Sums the Integer values in the given Map.
     * @param map
     * @return sum of the values in the map
     */
    public static <E> int sumOfVals(Map<E, Integer> map) {
        int sum = 0;
        for (E key : map.keySet()) {
            sum += map.get(key);
        }
        return sum;
    }

    /**
     * Checks whether the given object is an array.
     */
    //http://stackoverflow.com/questions/2725533/how-to-see-if-an-object-is-an-array-without-using-reflection
    public static boolean isArray(Object obj) {
        return obj != null && obj.getClass().isArray();
    }

}
