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
package butt.tool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import butt.tool.benchmark.common.CSV;
import butt.tool.benchmark.common.CSVReader;
import butt.tool.evaluation.Problem;
import gps.ResultEnum;

/**
 * Benchmark evaluation tool. Allows to automatically evaluate benchmarks. Each
 * algorithm is evaluated for a specific problem and result type. Thus
 * automatically generating training data for classifiers.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class EvaluationTool {

    private static Logger logger = Logger
            .getLogger(EvaluationTool.class.getCanonicalName());

    /**
     * Calculate a weight for each algorithm. A weight might be the time an
     * algorithm took to solve a problem. Or it can be something more specific
     * to a result type. However the lower the weight is the better is the
     * algorithm considered to solve the problem. A problem is made up by an
     * actual problem class and a desired result type.
     * 
     * @param prob
     *            The problem to evaluate the algorithms for.
     * @param resultType
     *            The desired result type as a String. Must be an String of an
     *            Element in {@link gps.ResultEnum}.
     * @param data
     *            All lines that have been read from the benchmark csv. Each
     *            line maps fields of the csv to content of that field. For all
     *            fields in the benchmark csv see
     *            {@link butt.tool.benchmark.common.CSV#FIELDS}.
     * @return A map that maps a Long to each algorithm. The smaller the Long
     *         value is the better performed the algorithm. There is no specific
     *         definition for the Long.
     */
    private static Map<String, Optional<Long>> calcAlgoWeight(
            final Problem prob, final String resultType,
            final List<Map<String, String>> data) {
        final Map<String, Optional<Long>> algoWeight = new HashMap<>();

        if (resultType == null) {
            return algoWeight;
        }

        // get as enum type
        final ResultEnum resultTypeAsEnum = ResultEnum.valueOf(resultType);

        IntStream.range(0, data.size())

                // only consider benchmark results that are relevant for the
                // evaluation
                .filter(p -> prob.problemName
                        .equals(data.get(p).get(CSV.FIELD_PROBLEM))
                        && resultType
                                .equals(data.get(p).get(CSV.FIELD_RESULT_TYPE)))

                .forEach(m -> algoWeight.put(
                        data.get(m).get(CSV.FIELD_ALGORITHM),
                        weight(prob, resultTypeAsEnum, data.get(m), m)));

        return algoWeight;
    }

    /**
     * Calculate the weight for a specific problem and result type with the
     * given fields from the benchmark csv.
     * 
     * @param prob
     *            The problem to evaluate the algorithms for.
     * @param resultType
     *            The desired result type.
     * @param csvFields
     *            The fields of the csv file as a map. See
     *            {@link butt.tool.benchmark.common.CSV#FIELDS} for the fields.
     *            Note that {@code null} and the empty string is permitted as
     *            element and that not all headers might be included.
     * @param lineNumber
     *            the line number that this method is processing.
     * @return The weight. Might be {@code null} if unweighed.
     */
    private static Optional<Long> weight(final Problem prob,
            final ResultEnum resultType, final Map<String, String> csvFields,
            int lineNumber) {
        switch (resultType) {
        case WINNABLE:
            // weight by time
            try {
                return csvFields.get(CSV.FIELD_PROBLEM).startsWith("EXCEPTION")
                        ? Optional.empty()
                        : Optional.of(Long.valueOf(
                                csvFields.get(CSV.FIELD_ALGORITHM_TIME)));
            } catch (final NumberFormatException e) {
                logger.warning("for some line (" + lineNumber
                        + ") with the result type " + resultType.toString()
                        + " no algorithm time has been set. Problem:"
                        + prob.problemName + " algo:"
                        + csvFields.get(CSV.FIELD_ALGORITHM));
                return Optional.empty();
            }
        case BEST_MOVE:
            // TODO here comes igors score
        case MOVES:
        case STATE_SEQ:
        case TERMINAL:
            // weight is calculated by terminal depth
            try {
                return csvFields.get(CSV.FIELD_PROBLEM).startsWith("EXCEPTION")
                        ? Optional.empty()
                        : Optional.of(Long.valueOf(
                                csvFields.get(CSV.FIELD_TERMINAL_DEPTH)));
            } catch (final NumberFormatException e) {
                logger.warning("for some line (" + lineNumber
                        + ") with the result type " + resultType.toString()
                        + " no terminal depth has been set. Problem:"
                        + prob.problemName + " algo:"
                        + csvFields.get(CSV.FIELD_ALGORITHM));
                return Optional.empty();
            }
        default:
            // default: weight is calculated by consumed time
            return csvFields.get(CSV.FIELD_PROBLEM).startsWith("EXCEPTION")
                    || Boolean.valueOf(
                            csvFields.get(CSV.FIELD_ALGORITHM_CANCELLED))
                                    ? Optional.empty()
                                    : Optional.of(Long.valueOf(csvFields
                                            .get(CSV.FIELD_ALGORITHM_TIME)));
        }
    }

    /**
     * Run the benchmark evaluation tool.
     */
    public static void run() {
        System.out.println("Staring evaluation");

        final List<Map<String, String>> data;
        {
            // we can get since this run method is only called if it is present
            final CSVReader csv = new CSVReader(
                    Arguments.getTargetFilename().get());
            data = csv.readAllLines().stream()
                    // .filter(p -> p.get(CSV.FIELD_PROBLEM)
                    // .matches(Arguments.getFilterProblemRexExp()))
                    // .filter(p -> p.get(CSV.FIELD_RESULT_TYPE)
                    // .matches(Arguments.getFilterResultRexExp()))
                    // .filter(p -> p.get(CSV.FIELD_ALGORITHM_CLASS)
                    // .matches(Arguments.getFilterAlgorithmRexExp()))
                    .collect(Collectors.toList());
            csv.close();
        }

        // game -> [result types]
        final Map<Problem, Set<String>> gameResultTypes = new HashMap<>();
        data.stream().forEach(c -> {
            Set<String> resultTypes = new HashSet<>();
            final Set<String> list;
            if ((list = gameResultTypes
                    .putIfAbsent(
                            new Problem(c.get(CSV.FIELD_PROBLEM),
                                    c.get(CSV.FIELD_VECTOR)),
                            resultTypes)) != null) {
                resultTypes = list;
            }
            resultTypes.add(c.get(CSV.FIELD_RESULT_TYPE));
        });

        butt.tool.evaluation.CSV out = new butt.tool.evaluation.CSV(
                Arguments.getEvaluationFilename().get());
        try {
            for (final Entry<Problem, Set<String>> e : gameResultTypes
                    .entrySet()) {
                final Problem prob = e.getKey();
                for (final String resultType : e.getValue()) {

                    // Algorithm -> Weight (lower = better)
                    final Map<String, Optional<Long>> algoWeight = calcAlgoWeight(
                            prob, resultType, data);

                    // total weight all algorithms took to solve the problem
                    LongSummaryStatistics stream = algoWeight.entrySet()
                            .stream().map(m -> m.getValue())
                            .filter(p -> p.isPresent()).mapToLong(m -> m.get())
                            .summaryStatistics();
                    final double maxWeight = stream.getMax();
                    final double minWeight = stream.getMin();
                    final double scala = maxWeight - minWeight;

                    // Algorithm -> Score
                    final Map<String, Double> algoScore = new HashMap<>();
                    algoWeight.entrySet().stream()
                            .forEach(m -> algoScore.put(m.getKey(),
                                    m.getValue()
                                            .isPresent()
                                                    ? 1d - ((((double) ((m
                                                            .getValue().get())
                                                            - minWeight))
                                                            / scala))
                                                    : -1d));

                    algoScore.entrySet()
                            .forEach(f -> out.append(prob.problemName,
                                    f.getKey(), resultType, prob.vector,
                                    f.getValue()));

                }
            }
        } finally {
            out.close();
        }
        System.out.println("Evaluation done");
    }

    /**
     * Singleton class cannot be instantiated.
     */
    private EvaluationTool() {

    }
}
