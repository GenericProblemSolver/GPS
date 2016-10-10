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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Variable;
import gps.bytecode.expressions.Operator.Undef;
import gps.bytecode.symexec.SatisfactionProblem;
import gps.util.CommandAvailability;

/**
 * Backend that used Z3StdoutBackend to generate valid Z3 output and outputs
 * into a spawned Z3 process
 * 
 * Tries to read a solution from the output of the Z3 process
 * 
 * @author mfunk@tzi.de
 *
 */
public class Z3StdoutEvalBackend implements IBytecodeBackend {

    @Override
    public BackendResult solve(SatisfactionProblem problem) {
        ProcessBuilder pb = new ProcessBuilder("z3", "-in");
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
            return BackendResult.ERROR;
        }
        InputStream in = p.getInputStream();
        // Create a Z3StdoutBackend that outputs to the process
        Z3StdoutBackend z3backend;
        try {
            z3backend = new Z3StdoutBackend(
                    new PrintStream(p.getOutputStream(), true, "UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return BackendResult.ERROR;
        }
        z3backend.solve(problem);
        try {
            p.getOutputStream().close();
        } catch (IOException e) {
            // if this happens, should we just let it happen since it doesn't
            // stop us from solving the problem?
            throw new RuntimeException("Could not close Z3 OutputStream");
        }
        // Read the whole output from the inputstream
        Scanner s = new Scanner(in, "UTF-8");
        s.useDelimiter("\\A"); // Black magic
        String str = s.next();

        // Try to construct a solution from the output
        boolean satisfiable = false;
        StringBuilder errors = new StringBuilder();
        StringBuilder solution = new StringBuilder();
        for (String line : str.split("\n")) {
            if (line.contains("(error")) {
                errors.append(line + "\n");
            } else if (line.equals("sat")) {
                satisfiable = true;
            } else if (line.equals("unsat")) {
                satisfiable = false;
            } else if (line.equals("unknown")) {
                satisfiable = false;
                System.out.println("Unknown solution: Z3 cant solve it");
            } else {
                solution.append(line + "\n");
            }
        }
        s.close();
        if (errors.length() != 0 && satisfiable) {
            return BackendResult.ERROR;
        }

        String oneline = solution.toString().replaceAll("\n", "")
                .replaceAll("- ", "-").replaceAll("[()]", "")
                .replaceAll("\\s+", " ").replaceAll("define-fun ", "\n");
        SatisfactionProblemSolution spsolution = new SatisfactionProblemSolution(
                satisfiable, oneline);

        for (String line : oneline.split("\n")) {
            String[] tokens = line.split(" ");
            if (tokens.length != 3) {
                continue;
            }
            spsolution.variableValues.put(new Variable(tokens[0], Undef.class),
                    z3ValueToConstant(tokens[2], tokens[1]));
        }

        problem.assignSolution(spsolution);

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException("Z3 process interrupted");
        }
        return BackendResult.SOLVED;
    }

    @Override
    public boolean isAvailable() {
        return CommandAvailability.isAvailable("z3");
    }

    Constant z3ValueToConstant(String value, String type) {
        try {
            switch (type) {
            case "Int":
                return new Constant(Long.parseLong(value));
            case "Real":
                return new Constant(Double.parseDouble(value));
            case "Bool":
                return new Constant(Boolean.parseBoolean(value));
            default:
                throw new RuntimeException("Unknown z3 type: " + type);
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number: " + value);
        }
    }

}
