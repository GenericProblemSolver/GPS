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
package gps;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import gps.bytecode.IBytecodeResult;
import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Variable;
import gps.exception.NoSolverAvailableException;
import gps.games.GamesModule;
import gps.games.IGameResult;
import gps.games.MemorySavingMode;
import gps.games.algorithm.analysis.IGameAnalysisResult;
import gps.games.wrapper.Action;
import gps.optimization.IOptimizationResult;
import gps.optimization.OptimizationModule;
import gps.optimization.OptimizationReturn;
import gps.util.reflections.ReflectionsHelper;

/**
 * The Generic Problem Solver.
 * 
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class
 */
public class GPS<T>
        implements IGameResult<T>, IOptimizationResult<T>, IBytecodeResult<T> {

    /**
     * All classes that extend the AbstractSolverModule class.
     */
    @SuppressWarnings("rawtypes")
    private static List<Class<? extends ISolverModule>> solverModuleClasses = new ArrayList<>();

    static {
        @SuppressWarnings("rawtypes")
        final Set<Class<? extends ISolverModule>> subTypes = ReflectionsHelper
                .getSubTypesOfCached("gps", ISolverModule.class);
        solverModuleClasses.addAll(subTypes);
    }

    /**
     * All solver modules that have been instantiated for the given problem.
     */
    private List<? extends ISolverModule<T>> solverModules;

    /**
     * Construct a generic problem solver that solves various problems specified
     * in the {@link gps.GPS} class.
     * 
     * @param pProblem
     *            The problem class.
     */
    @SuppressWarnings("unchecked")
    public GPS(final T pProblem) {
        IWrappedProblem<T> problem = wrap(pProblem);

        if (solverModuleClasses.size() == 0) {
            throw new NoSolverAvailableException("No solvers registered");
        }

        solverModules = solverModuleClasses.stream().map(solverClass -> {
            try {
                return (ISolverModule<T>) solverClass
                        .getDeclaredConstructor(IWrappedProblem.class)
                        .newInstance(problem);
            } catch (NoSuchMethodException | SecurityException
                    | InstantiationException | InvocationTargetException
                    | IllegalAccessException e) {
                throw new RuntimeException(
                        "Cannot instantiate solver " + solverClass.getName(),
                        e);
            }
        }).collect(Collectors.toList());
    }

    /**
     * Loads the wrapped class and instantiates it.
     * 
     * @param object
     *            The object to create an Interface for
     * @return The interface to the wrapped Object
     */
    @SuppressWarnings("unchecked")
    public static <T> IWrappedProblem<T> wrap(final T object) {
        IWrappedProblem<T> prob = null;
        try {
            Class<?> clazz = object.getClass();
            Package pack = clazz.getPackage();
            String name = "";
            if (pack != null && !pack.getName().isEmpty()) {
                name += clazz.getPackage().getName() + ".";
            }
            name += "C" + clazz.getCanonicalName().replaceAll("\\.", "P")
                    + "Wrapped";

            ClassLoader urlClassLoader = object.getClass().getClassLoader();

            Constructor constr = urlClassLoader.loadClass(name)
                    .getConstructor(clazz);
            prob = (IWrappedProblem<T>) constr.newInstance(object);

        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException
                | ClassNotFoundException | ClassCastException e) {
            if (!containsGPSAnnotations(object)) {
                throw new NoSolverAvailableException(
                        "No GPS-Annotations found within the class "
                                + object.getClass().getName() + ".",
                        e);
            }
            throw new RuntimeException("Failed to wrap problem class.", e);
        }
        return prob;
    }

    /**
     * Checks whether the given problem class is annotated with any of the gps
     * annotations from {@link gps.annotations} package.
     * 
     * @param object
     *            the problem to be solved
     * @return {@code true} if any annotation from the package
     *         {@link gps.annotations} was used in {@code object.class},
     *         otherwise {@code false}
     */
    private static <T> boolean containsGPSAnnotations(final T object) {
        final Class<? extends Object> clazz = object.getClass();
        for (Class<? extends Annotation> annotCla : ReflectionsHelper
                .getSubTypesOfCached("gps.annotations", Annotation.class)) {
            final Annotation[] usedAnnotations = clazz
                    .getAnnotationsByType(annotCla);
            if (usedAnnotations.length != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find all suitable solvers that are implementing the provided interface.
     * If no suitable solver module can be found a
     * {@link gps.exception.NoSolverAvailableException} is thrown.
     * 
     * @param solverInterface
     *            The class of the interface that all found solver modules must
     *            provide.
     * 
     * @return A List of all found solver modules that implement the given
     *         interface. Is never {@code null}.
     */
    private <E> List<E> findSuitableSolvers(Class<E> solverInterface) {
        @SuppressWarnings("unchecked")
        List<E> solvers = (List<E>) solverModules.stream()
                .filter(solverModule -> solverInterface
                        .isAssignableFrom(solverModule.getClass()))
                .collect(Collectors.toList());
        if (solvers.isEmpty()) {
            throw new NoSolverAvailableException(
                    "No suitable solver found that implements the required interface");
        }
        return solvers;
    }

    /**
     * Find all suitable solvers that are implementing the provided interface
     * and can solve for the given result type. If no suitable solver module can
     * be found a {@link gps.exception.NoSolverAvailableException} is thrown.
     * 
     * @param type
     *            The result type that the found solver must be able to solve.
     * 
     * @param solverInterface
     *            The class of the interface that all found solver modules must
     *            provide.
     * 
     * @return A List of all found solver modules that implement the given
     *         interface and is able to solve the given result type. Is never
     *         {@code null}.
     */
    private <E> List<E> findSuitableSolvers(ResultEnum type,
            Class<E> solverInterface) {

        @SuppressWarnings("unchecked")
        List<E> solvers = findSuitableSolvers(solverInterface).stream()
                .filter(solverModule -> {
                    return ((ISolverModule<T>) solverModule).canSolve(type);
                }).collect(Collectors.toList());

        if (solvers.isEmpty()) {
            throw new NoSolverAvailableException("No suitable solver found");
        }

        return solvers;
    }

    /**
     * Find the best suitable solver that is implementing the provided interface
     * and can solve for the given result type. If no suitable solver module can
     * be found a {@link gps.exception.NoSolverAvailableException} is thrown.
     * 
     * If multiple solver modules can be used the first is considered the best.
     * 
     * @param type
     *            The result type that the solver must be able to solve.
     * 
     * @param solverInterface
     *            The class of the interface that the solver module must
     *            provide.
     * 
     * @return The solver module that implement the given interface and is able
     *         to solve the given result type. Is never {@code null}.
     */
    private <E> E findBestSolver(ResultEnum type, Class<E> solverInterface) {
        return findSuitableSolvers(type, solverInterface).get(0);
    }

    /**
     * Find the best suitable solver that is implementing the provided
     * interface. If no suitable solver module can be found a
     * {@link gps.exception.NoSolverAvailableException} is thrown.
     * 
     * If multiple solver modules can be used the first is considered the best.
     * 
     * @param solverInterface
     *            The class of the interface that the solver module must
     *            provide.
     * 
     * @return The solver module that implement the given interface. Is never
     *         {@code null}.
     */
    private <E> E findBestSolver(Class<E> solverInterface) {
        return findSuitableSolvers(solverInterface).get(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<List<Action>> moves() {
        return findBestSolver(ResultEnum.MOVES, IGameResult.class).moves();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<List<T>> stateSequence() {
        return findBestSolver(ResultEnum.STATE_SEQ, IGameResult.class)
                .stateSequence();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<T> terminalState() {
        return findBestSolver(ResultEnum.TERMINAL, IGameResult.class)
                .terminalState();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Action> bestMove() {
        return findBestSolver(ResultEnum.BEST_MOVE, IGameResult.class)
                .bestMove();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Boolean> isWinnable() {
        return findBestSolver(ResultEnum.WINNABLE, IGameResult.class)
                .isWinnable();
    }

    @Override
    public boolean isFinished() {
        return findBestSolver(IGameResult.class).isFinished();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<IGameAnalysisResult> gameAnalysis() {
        return findBestSolver(IGameResult.class).gameAnalysis();
    }

    /**
     * Set or remove the depthlimit for calculations.
     * 
     * @param pMaxdepth
     *            The depthlimit in moves, set non-positive if no depthlimit
     *            should be respected.
     */
    public void setDepthlimit(int pMaxdepth) {
        findSuitableSolvers(GamesModule.class)
                .forEach(e -> e.setDepthlimit(pMaxdepth));
    }

    /**
     * Set the memory saving mode that is used when creating clones of game
     * states.
     * 
     * @param pMode
     *            The memory saving mode. May not be {@code null}
     */
    public void setMemorySavingMode(MemorySavingMode pMode) {
        findSuitableSolvers(GamesModule.class)
                .forEach(e -> e.setMemorySavingMode(pMode));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<OptimizationReturn> maximize() {
        return findBestSolver(ResultEnum.MAXIMIZED, IOptimizationResult.class)
                .maximize();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<OptimizationReturn> minimize() {
        return findBestSolver(ResultEnum.MINIMIZED, IOptimizationResult.class)
                .minimize();
    }

    /**
     * Sets whether or not the optimal solution is demanded. If a heuristically 
     * found solution is sufficient, the optimization process will probably 
     * take a shorter amount of time. 
     * 
     * @param exact
     * 			{@code true} if the optimal solution is demanded,
     * 			{@code false} otherwise
     */
    public void demandGlobalOptimum(final boolean exact) {
        findSuitableSolvers(OptimizationModule.class)
                .forEach(e -> e.setExact(exact));
    }

    /**
     * Sets the time that the main part of the optimization process is allowed 
     * to use.
     * 
     * @param ms
     * 			the amount of milliseconds
     */
    public void setMaxRuntimeForOptimization(final long ms) {
        findSuitableSolvers(OptimizationModule.class)
                .forEach(e -> e.setMaxRuntimeForOptimizers(ms));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Map<Variable, Constant>> satisfyingModel() {
        return findBestSolver(IBytecodeResult.class).satisfyingModel();
    }

}