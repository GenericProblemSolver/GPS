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
package butt.tool.evaluation;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Create a CSV file with the evaluation data as specified in RFC 4180
 * 
 * @author haker@uni-bremen.de
 *
 */
public class CSV implements Closeable {

    /**
     * The Stream where the data is streamed to.
     */
    private final PrintWriter out;

    public static final int FIELD_PROBLEM_IDX = 0;
    public static final int FIELD_RESULT_TYPE_IDX = 1;
    public static final int FIELD_ALGORITHM_CLASS_IDX = 2;
    public static final int FIELD_VECTOR_IDX = 3;
    public static final int FIELD_SCORE_IDX = 4;

    /**
     * The header of the CSV file in that order
     */
    public static final String[] FIELDS = new String[] { "Problem",
            "Result Type", "Algorithm Class", "Vector", "Score" };

    /**
     * The field name of the algorithm name column.
     */
    public static final String FIELD_PROBLEM = FIELDS[FIELD_PROBLEM_IDX];

    /**
     * The field name of the algorithm name column.
     */
    public static final String FIELD_ALGORITHM = FIELDS[FIELD_ALGORITHM_CLASS_IDX];

    /**
     * The field name of the result type column.
     */
    public static final String FIELD_RESULT_TYPE = FIELDS[FIELD_RESULT_TYPE_IDX];

    /**
     * The field name of the vector column.
     */
    public static final String FIELD_VECTOR = FIELDS[FIELD_VECTOR_IDX];

    /**
     * The field name of the score column.
     */
    public static final String FIELD_SCORE = FIELDS[FIELD_SCORE_IDX];

    /**
     * The headline of the output file
     */
    public static final String HEADLINE = Arrays.stream(FIELDS)
            .reduce((a, b) -> a + ',' + b).get();

    /**
     * Construct a CSV Object.
     * 
     * @param fileName
     *            The filename for the csv file to be created.
     */
    public CSV(final String fileName) {
        try {
            out = new PrintWriter(new FileWriter(fileName));
        } catch (final IOException e) {
            throw new RuntimeException("cannot create filestream", e);
        }
        out.println(HEADLINE);
    }

    /**
     * Append a row to the csv file.
     * 
     * @param gameName
     *            The name of the game.
     * @param algoClassName
     *            The fully qualified name of the algorithm class.
     * @param resultType
     *            The result that is searched for
     * @param vector
     *            the analysis vector for the given problem as string (semicolon separated values).
     * @param score
     *            The score that the evaluation tool came up with.
     */
    public void append(String gameName, String algoClassName, String resultType,
            String vector, Double score) {
        out.println(gameName + "," + resultType.toString() + "," + algoClassName
                + "," + vector + "," + score);
    }

    /**
     * Close the used stream.
     */
    @Override
    public void close() {
        out.close();
    }
}
