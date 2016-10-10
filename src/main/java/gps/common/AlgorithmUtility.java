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
package gps.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gps.ResultEnum;
import gps.util.KryoHelper;
import gps.util.reflections.ReflectionsHelper;

/**
 * Provides utility functions for determining applicable algorithms
 * 
 * @author haker@uni-bremen.de
 *
 */
public class AlgorithmUtility {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger
            .getLogger(AlgorithmUtility.class.getCanonicalName());

    static {
        LOGGER.setLevel(Level.FINER);
    }

    /**
     * Get all algorithm classes that are implemented in the GPS.
     * 
     * @param pClass
     *            The class that the algorithms must implement that are returned
     *            by this method. May not be {@code null}.
     * 
     * @return A Set of all classes of algorithms.
     */
    public static <T extends AbstractAlgorithm> List<Class<? extends T>> getAllAlgorithms(
            Class<T> pClass) {
        final Set<Class<? extends T>> subTypes = ReflectionsHelper
                .getSubTypesOfCached("gps.*", pClass);
        return subTypes.stream()
                .filter(p -> !Modifier.isAbstract(p.getModifiers())
                        && !p.isInterface())
                .sorted((x, y) -> x.getCanonicalName()
                        .compareTo(y.getCanonicalName()))
                .collect(Collectors.toList());
    }

    /**
     * Get all algorithm classes that are implemented in the GPS.
     * 
     * @return A Set of all classes of algorithms.
     */
    public static List<Class<? extends AbstractAlgorithm>> getAllAlgorithms() {
        return getAllAlgorithms(AbstractAlgorithm.class);
    }

    /**
     * Combine Option Sets so that all combination (permutations) are returned.
     * 
     * @param res
     *            The result list
     * @param input
     *            The option sets
     * @param current
     *            must be set to new Object[input.length]
     * @param k
     *            must be set to 0
     */
    private static void combine(List<Object[]> res,
            List<? extends Object>[] input, Object[] current, int k) {
        if (k == input.length) {
            final Object[] d = Arrays.copyOf(current, k);
            res.add(d);
            for (int i = 0; i < d.length; i++) {
                try {
                    d[i] = KryoHelper.deepCopy(d[i]);
                } catch (NoClassDefFoundError e) {
                    // Happens for lambda classes. We do not need to copy these
                    // lambdas anyway since they wont change. So we ignore this.
                }
            }
        } else {
            for (int j = 0; j < input[k].size(); j++) {
                current[k] = input[k].get(j);
                combine(res, input, current, k + 1);
            }
        }
    }

    /**
     * Finds a compatible constructor.
     * 
     * @param algorithmClass
     *            The algorithm class to search the constructor.
     * 
     * @param paramClasses
     *            The arguments that the constructor must support.
     * 
     * @return A Constructor.
     * 
     * @throws RuntimeException
     *             if no constructor can be found.
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findCompatibleConstructor(
            Class<T> algorithmClass, Class<?>... paramClasses) {

        // find a compatible constructor
        final Optional<? extends Constructor<?>> optionalCon = Arrays
                .stream(algorithmClass.getDeclaredConstructors()).filter(p -> {
                    Class<?>[] args = p.getParameterTypes();
                    if (args.length == paramClasses.length) {
                        for (int i = 0; i < args.length; i++) {
                            // compatible if argument is passable
                            if (paramClasses[i] != null && !args[i]
                                    .isAssignableFrom(paramClasses[i])) {
                                return false; // some argument is
                                // not assignable
                            }
                        }
                        return true; // all arguments are fine
                    }
                    return false; // arguments number is not equal
                }).findAny();
        if (!optionalCon.isPresent()) {
            // no compatible constructor found
            throw new RuntimeException("Cannot find constructor for class "
                    + algorithmClass.getCanonicalName()
                    + " with compatible arguments: "
                    + Arrays.stream(paramClasses)
                            .map(m -> m == null ? "<null type>"
                                    : m.getCanonicalName())
                            .reduce("", (g, h) -> g + "," + h)
                    + "\nPlease implement it.");
        } else {
            // return found constructor
            return (Constructor<T>) optionalCon.get();
        }
    }

    /**
     * Instantiate all algorithms with the specified base arguments. The
     * arguments are cloned for each instantiation.
     * 
     * <p>
     * Each Algorithm must provide a constructor that accepts the base arguments
     * in their given order. If the algorithm provides options by overriding
     * {@link AbstractAlgorithm#getOptions} then the algorithm must also provide
     * constructors that are compatible with all combinations of the options.
     * Thus supporting the base arguments and additionally supporting the
     * options arguments. The base arguments are always the first arguments.
     * </p>
     * 
     * @param pClass
     *            The class that all algorithms must implement that are to be
     *            instantiated.
     * @param resultType
     *            The result type the algorithm must support. May be
     *            {@code null} if any result type is desired.
     * @param baseArgs
     *            The base arguments that are passed to the algorithms.
     * 
     * @return List of all algorithms that are instantiated with the specified
     *         game.
     */
    public static <T extends AbstractAlgorithm> List<? extends T> instantiateAllAlgorithms(
            Class<T> pClass, ResultEnum resultType, Object... baseArgs) {
        List<T> algos = new ArrayList<>();
        // Get classes of arguments
        final Class<?>[] baseArgClasses = Arrays.stream(baseArgs)
                .map(m -> m == null ? null : m.getClass())
                .toArray(i -> new Class<?>[i]);

        // For each algorithm class...
        getAllAlgorithms(pClass).stream().forEach(algorithmClass -> {
            try {
                Constructor<? extends T> con = findCompatibleConstructor(
                        algorithmClass, baseArgClasses);
                // Create new copy of all arguments and construct algorithm
                final T algoInstance = con.newInstance(
                        Arrays.stream(baseArgs).map(m -> KryoHelper.deepCopy(m))
                                .toArray(i -> new Object[i]));

                // Checking options
                final List<?>[] options = algoInstance.getOptions();
                if (options.length == 0) {
                    // This algorithm does not feature options. We can keep it.
                    if (resultType == null
                            || algoInstance.isApplicable(resultType)) {
                        // only add if algorithm supports desired result type
                        algos.add(algoInstance);
                    }
                } else {
                    // Algorithm provides options
                    final ArrayList<Object[]> optionSets = new ArrayList<Object[]>();

                    // Combine them so that all permutations are considered.
                    combine(optionSets, options, new Object[options.length], 0);

                    // For each options permutation...
                    for (final Object[] set : optionSets) {
                        // copy all arguments and store them in an array
                        final Stream<Object> argCopy = Arrays.stream(baseArgs);
                        // .map(m -> kryo.copy(m)); // actually do not copy base
                        // args

                        // create arguments for expected constructor
                        final Object[] params = Stream
                                .concat(argCopy, Arrays.stream(set))
                                .toArray(i -> new Object[i]);

                        // classes of the arguments
                        final Class<?>[] paramClasses = Arrays.stream(params)
                                .map(m -> m == null ? null : m.getClass())
                                .toArray(i -> new Class<?>[i]);

                        // log calling constructor to logger
                        if (LOGGER.isLoggable(Level.FINEST)) {
                            String msg = "calling constructor "
                                    + algorithmClass.getSimpleName() + "(";
                            for (int i = 0; i < params.length; i++) {
                                if (i > 0) {
                                    msg += ", ";
                                }
                                if (paramClasses[i] != null
                                        && params[i] != null) {
                                    msg += "[" + paramClasses[i].getSimpleName()
                                            + "] ";
                                    msg += params[i].toString();
                                } else {
                                    msg += "null";
                                }
                            }
                            msg += ")";
                            LOGGER.finest("calling constructor " + msg);
                        }
                        // instantiate class and add to list
                        final T instance = findCompatibleConstructor(
                                algorithmClass, paramClasses)
                                        .newInstance(params);
                        if (resultType == null
                                || instance.isApplicable(resultType)) {
                            algos.add(instance);
                        }
                    }
                }
            } catch (IllegalAccessException | InstantiationException
                    | InvocationTargetException ex) {
                throw new RuntimeException("Cannot instantiate algorithm class",
                        ex);
            }
        });
        LOGGER.finer("instantiate all algorithms done .." + algos.size()
                + " algorithms have been loaded");
        return algos;
    }

    /**
     * Instantiate an algorithm with the specified base arguments and options.
     * The arguments are cloned for the instantiation.
     * 
     * <p>
     * Each Algorithm must provide a constructor that accepts the base arguments
     * in their given order. If the algorithm provides options by overriding
     * {@link AbstractAlgorithm#getOptions} then the algorithm must also provide
     * constructors that are compatible with all combinations of the options.
     * Thus supporting the base arguments and additionally supporting the
     * options arguments. The base arguments are always the first arguments.
     * </p>
     * 
     * @param pClass
     *            The class that all algorithms must implement that are to be
     *            instantiated.
     * @param baseArgs
     *            The base arguments that are passed to the algorithms.
     * @param options
     *            The options that are passed to the algorithm. May be an empty
     *            array.
     * 
     * @return List of all algorithms that are instantiated with the specified
     *         game.
     */
    public static <T extends AbstractAlgorithm> T instantiateAlgorithm(
            Class<T> pClass, Object baseArgs[], Object options[]) {
        // Get classes of arguments
        final Class<?>[] baseArgClasses = Arrays.stream(baseArgs)
                .map(m -> m == null ? null : m.getClass())
                .toArray(i -> new Class<?>[i]);
        try {
            Constructor<? extends T> con = findCompatibleConstructor(pClass,
                    baseArgClasses);

            // Construct algorithm
            final T algoInstance = con.newInstance(baseArgs);

            // Checking options
            if (options == null || options.length == 0) {
                // This algorithm does not feature options. We can keep it.
                return algoInstance;
            } else {
                // copy all arguments and store them in an array
                final Stream<Object> argCopy = Arrays.stream(baseArgs);
                // .map(m -> kryo.copy(m)); // actually do not copy base args

                // create arguments for expected constructor
                final Object[] params = Stream
                        .concat(argCopy, Arrays.stream(options))
                        .toArray(i -> new Object[i]);

                // classes of the arguments
                final Class<?>[] paramClasses = Arrays.stream(params)
                        .map(m -> m == null ? null : m.getClass())
                        .toArray(i -> new Class<?>[i]);

                // instantiate class and add to list
                return findCompatibleConstructor(pClass, paramClasses)
                        .newInstance(params);
            }
        } catch (IllegalAccessException | InstantiationException
                | InvocationTargetException ex) {
            throw new RuntimeException("Cannot instantiate algorithm class",
                    ex);
        }
    }

    /**
     * Filter all applicable algorithms.
     * 
     * @param list
     *            The list of algorithms to work with
     * @param e
     *            The result type the algorithms are supposed to provide.
     * @return The list of algorithms that can solve the given result type.
     */
    public static <T extends AbstractAlgorithm> List<? extends T> filterApplicableAlgorithms(
            List<? extends T> list, final ResultEnum e) {
        return list.stream().filter(p -> p.isApplicable(e))
                .collect(Collectors.toList());
    }

    /**
     * Return all algorithms that are applicable for the given arguments and
     * solve the given result enum.
     * 
     * @param pClass
     *            the class that the algorithms must implement.
     * @param e
     *            The result type that the algorithm must calculate.
     * @param baseArgs
     *            The arguments that the algorithm must accept in it's
     *            constructor.
     * 
     * @return A list of applicable algorithms.
     */
    public static <T extends AbstractAlgorithm> List<? extends T> getApplicableAlgorithms(
            Class<T> pClass, final ResultEnum e, final Object... baseArgs) {
        return instantiateAllAlgorithms(pClass, e, baseArgs);
    }
}
