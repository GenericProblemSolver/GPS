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
package bytecode;

import gps.bytecode.backends.Z3StdoutBackend;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import gps.bytecode.backends.Backends;
import gps.bytecode.backends.IBytecodeBackend;
import gps.bytecode.symexec.SatisfactionProblem;
import gps.bytecode.symexec.SymbolicExec;

/**
 * This is a custom Junit TestRunner, that runs Classes as
 * ConstraintSatisfactionProblem "Tests"
 *
 * @author mfunk@tzi.de
 */
public class ConstraintProblemRunner extends Runner {

    private final Class<?> c;
    private final Description spec;
    private final Iterable<IBytecodeBackend> backends;

    public ConstraintProblemRunner(Class<?> c) {
        this.c = c;
        spec = Description.createSuiteDescription(c);
        backends = Backends.getAvailableBackends();
        for (IBytecodeBackend b : backends) {
            Description d = Description.createTestDescription(c, "with " + b);
            spec.addChild(d);
        }
    }

    @Override
    public Description getDescription() {
        return spec;
    }

    @Override
    public void run(RunNotifier notifier) {

        if (c.getAnnotationsByType(Ignore.class).length > 0) {
            notifier.fireTestIgnored(spec);
            notifier.fireTestFinished(spec);
            return;
        }

        for (IBytecodeBackend b : backends) {
            Description d = Description.createTestDescription(c, "with " + b);
            try {
                notifier.fireTestStarted(d);
                Object instance = c.newInstance();
                SatisfactionProblem problem = SymbolicExec
                        .constructFunctionalSatisfactionProblem(instance);
                b.solve(problem);
                System.out.println(instance.toString());

                SEFunctionConsistencyCheck.check(problem);

                System.out.println(problem.getSolution());

                // Do not run Satisfiability on StdoutBackend
                if (!(b instanceof Z3StdoutBackend)) {
                    if (problem.isSatisfiable()) {
                        if (c.getAnnotationsByType(
                                AssertUnsatisfiable.class).length > 0) {
                            notifier.fireTestFailure(
                                    new Failure(d, new AssertionError(
                                            "Problem is satisfiable")));
                        }
                    } else {
                        if (c.getAnnotationsByType(
                                AssertSatisfiable.class).length > 0) {
                            notifier.fireTestFailure(
                                    new Failure(d, new AssertionError(
                                            "Problem is not satisfiable")));
                        }
                    }
                }
                notifier.fireTestFinished(d);
            } catch (Throwable e) {
                notifier.fireTestFailure(new Failure(d, e));
            }
        }

    }

}
