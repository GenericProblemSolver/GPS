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
package butt.tool.benchmark.common;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import gps.ResultEnum;
import gps.common.BenchmarkField;
import gps.common.IBenchmark;

/**
 * Create a CSV file with the benchmark data as specified in RFC 4180
 * 
 * @author haker@uni-bremen.de
 *
 */
public class CSV implements Closeable {

    /**
     * The Stream where the data is streamed to.
     */
    private final PrintWriter out;

    /**
     * The part that is expected to be appended next.
     */
    private int appendPart = 1;

    /**
     * Construct a CSV Object.
     * 
     * @param fileName
     *            The filename for the csv file to be created.
     */
    public CSV(final String fileName) {
        this(fileName, false);
    }

    /**
     * The index of the problem name column name in the {@link #FIELDS} array.
     */
    public static final int FIELD_PROBLEM_IDX = 0;

    /**
     * The index of the result type column name in the {@link #FIELDS} array.
     */
    public static final int FIELD_RESULT_TYPE_IDX = 1;

    /**
     * The index of the algorithm column name in the {@link #FIELDS} array.
     */
    public static final int FIELD_ALGORITHM_IDX = 2;

    /**
     * The index of the algorithm class column name in the {@link #FIELDS}
     * array.
     */
    public static final int FIELD_ALGORITHM_CLASS_IDX = 3;

    /**
     * The index of the result column name in the {@link #FIELDS} array.
     */
    public static final int FIELD_RESULT_IDX = 4;

    /**
     * The index of the time field column name in the {@link #FIELDS} array.
     */
    public static final int FIELD_TIME_IDX = 5;

    /**
     * The index of the cancelled column name in the {@link #FIELDS} array.
     */
    public static final int FIELD_CANCELLED_IDX = 6;

    /**
     * The index of the analysis vectpr column name in the {@link #FIELDS}
     * array.
     */
    public static final int FIELD_VECTOR_IDX = 8;

    /**
     * The header of the CSV file in that order
     */
    public static final String[] FIELDS;

    static {
        String[] f = new String[] { "Problem", "Result Type", "Algorithm",
                "Class", "Result", "Time (ms)", "Cancelled", "Analysed",
                "Vector" };
        FIELDS = Stream
                .concat(Arrays.stream(f),
                        Arrays.stream(BenchmarkField.values())
                                .map(m -> m.toString()))
                .toArray(i -> new String[i]);
    }

    /**
     * The headline of the output file
     */
    public static final String HEADLINE = Arrays.stream(FIELDS)
            .reduce((a, b) -> a + ',' + b).get();

    /**
     * The field name for the depth of the terminal node of the best found
     * solution.
     */
    public static final String FIELD_TERMINAL_DEPTH = BenchmarkField.BEST_MOVE_DEPTH
            .toString();

    /**
     * The field name for the analysis vector.
     */
    public static final String FIELD_VECTOR = CSV.FIELDS[CSV.FIELD_VECTOR_IDX];

    /**
     * The field name for the problem.
     */
    public static final String FIELD_PROBLEM = FIELDS[CSV.FIELD_PROBLEM_IDX];

    /**
     * The field name for the result type.
     */
    public static final String FIELD_RESULT_TYPE = FIELDS[CSV.FIELD_RESULT_TYPE_IDX];

    /**
     * The field name for the algorithm class name.
     */
    public static final String FIELD_ALGORITHM_CLASS = FIELDS[CSV.FIELD_ALGORITHM_CLASS_IDX];

    /**
     * The field name for the algorithm class name.
     */
    public static final String FIELD_ALGORITHM = FIELDS[CSV.FIELD_ALGORITHM_IDX];

    /**
     * The field name for the algorithm cancelled flag.
     */
    public static final String FIELD_ALGORITHM_CANCELLED = FIELDS[CSV.FIELD_CANCELLED_IDX];

    /**
     * The field name for the time the algorithm was running.
     */
    public static final String FIELD_ALGORITHM_TIME = FIELDS[CSV.FIELD_TIME_IDX];

    /**
     * Construct a CSV Object.
     * 
     * @param fileName
     *            The filename for the csv file to be created.
     * 
     * @param append
     *            If {@code true} append to the csv file instead of creating a
     *            new one
     */
    public CSV(final String fileName, final boolean append) {
        try {
            out = new PrintWriter(new FileWriter(fileName, append));
        } catch (final IOException e) {
            throw new RuntimeException("cannot create filestream", e);
        }

        if (!append) {
            // print the header
            out.println(HEADLINE);
        }
    }

    /**
     * Append the first part of a row to the csv file.
     * 
     * @param problemName
     *            The name of the game.
     * @param algoName
     *            The name of the algorithm.
     * @param algoClassName
     *            The fully qualified name of the algorithm class.
     * @param result
     *            The result that is searched for
     * @throws NullPointerException
     *             if one of the arguments is {@code null}.
     * @throws RuntimeException
     *             if the part to append is not the first part.
     */
    private synchronized void append1(final String problemName,
            final String algoName, final String algoClassName,
            final ResultEnum result) {
        if (appendPart != 1) {
            throw new RuntimeException(
                    "try to append part 1 but expected part " + appendPart);
        }
        if (algoClassName == null) {
            throw new NullPointerException("algoClassName may not be null");
        }
        out.print(problemName.replaceAll(",", ";"));
        out.print(',');
        out.print(result.name().replaceAll(",", ";"));
        out.print(',');
        out.print(algoName.replaceAll(",", ";"));
        out.print(',');
        out.print(algoClassName);
        out.print(',');
        out.flush();
        appendPart++;
    }

    /**
     * temporary Result String that can be overwritten by another call to
     * {@link #append2(String)}.
     */
    private String tmpResult = "";

    /**
     * Append the second part of a row to the csv file.
     * 
     * This method does not realy append anything to the file. It caches the
     * string to append. Therefore this method may be called multiple times
     * before {@link #append3(IBenchmark, long, boolean, boolean, Double[])} is
     * called.
     * 
     * @param result
     *            The result
     * 
     * @throws NullPointerException
     *             if one of the arguments is {@code null}.
     * @throws RuntimeException
     *             if the part to append is not the second part.
     */
    private synchronized void append2(String result) {
        if (result == null) {
            throw new NullPointerException("result may not be null");
        }
        if (appendPart != 2) {
            if (appendPart != 3) {
                // We allow part 3 because we buffer part 2 so we can overwrite
                // it.
                throw new RuntimeException(
                        "try to append part 2 but expected part " + appendPart);
            }
        }
        // Do not write immediately
        // out.print(result.replaceAll(",", ";"));
        tmpResult = result;
        appendPart = 3;
    }

    /**
     * Returns the part that has to be appended next.
     * 
     * @return The part to append. Is a number between 1 and 3 (including both)
     */
    public int getPartToAppend() {
        return appendPart;
    }

    /**
     * Append a row to the csv file.
     * 
     * @param bm
     *            The benchmark used by the algorithm. May be {@code null}.
     * @param millis
     *            The time that passed during the benchmark. If {@code null}
     * @param cancelled
     *            Mark, whether the benchmark has been cancelled by the
     *            interrupt bit.
     * @param usedAnalysisResults
     *            Mark whether the algorithm was provided with the analysis
     *            results.
     * @param vector
     *            The analysis vector
     * 
     * @throws NullPointerException
     *             if the vector argument is {@code null}.
     * @throws RuntimeException
     *             if the part to append is not the third part.
     */
    private synchronized void append3(final IBenchmark bm, final long millis,
            final boolean cancelled, final boolean usedAnalysisResults,
            final Double[] vector) {
        if (appendPart != 3) {
            throw new RuntimeException(
                    "try to append part 3 but expected part " + appendPart);
        }
        if (vector == null) {
            throw new NullPointerException("vector must not be null");
        }
        // write the buffered part 2 now
        out.print(tmpResult.replaceAll(",", ";"));

        out.print(',');
        out.print(millis);
        out.print(',');
        out.print(cancelled);
        out.print(',');
        out.print(usedAnalysisResults);
        out.print(',');
        out.print("{" + Arrays.stream(vector)
                .map(m -> m == null ? "0" : Double.toString(m))
                .reduce((a, b) -> a + ";" + b).orElse("") + "}");
        for (BenchmarkField field : BenchmarkField.values()) {
            out.print(',');
            if (bm != null
                    && Arrays.asList(bm.getUsedFields()).contains(field)) {
                Optional<? extends Number> op = bm.getField(field);
                if (op.isPresent()) {
                    out.print(op.get());
                } else {
                    out.print("EMPTY");
                }

            } else {
                out.print("NOT USED");
            }
        }
        out.println();
        out.flush();
        appendPart = 1;
    }

    /**
     * Append a row to the csv file.
     * 
     * @param problemName
     *            The name of the game.
     * @param algoName
     *            The name of the algorithm. * @param algoClassName The fully
     *            qualified name of the algorithm class.
     * @param resultType
     *            The result that is searched for
     * @param result
     *            The result that the algorithm came up with
     * @param bm
     *            The benchmark used by the algorithm.
     * @param millis
     *            The time that passed during the benchmark.
     * @param cancelled
     *            Mark, whether the benchmark has been cancelled by the
     *            interrupt bit.
     * @param usedAnalysisResults
     *            Mark whether the algorithm was provided with the analysis
     *            results.
     * @param vector
     *            The analysis vector
     * 
     * @throws RuntimeException
     *             if the part to append is not the first part.
     * @throws NullPointerException
     *             if the vector argument, the bm argument or any other string
     *             argument is {@code null}.
     */
    public synchronized void append(final String problemName,
            final String algoName, final String algoClassName,
            final ResultEnum resultType, final String result,
            final IBenchmark bm, final long millis, final boolean cancelled,
            final boolean usedAnalysisResults, final Double[] vector) {
        if (appendPart != 1) {
            throw new RuntimeException(
                    "try to append an entire row (parts 1,2,3) but expected part "
                            + appendPart);
        }
        append1(problemName, algoName, algoClassName, resultType);
        append2(result);
        append3(bm, millis, cancelled, usedAnalysisResults, vector);
    }

    /**
     * Close the used stream.
     */
    @Override
    public void close() {
        out.close();
    }
}
