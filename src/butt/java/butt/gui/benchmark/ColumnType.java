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
package butt.gui.benchmark;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.util.Callback;

/**
 * The column type class. This is the user data that is stored in each column.
 * The column type is used by the benchmark table view setup class (
 * {@link butt.gui.benchmark.TableViewSetup}).
 * 
 * @author haker@uni-bremen.de
 *
 */
class ColumnType {

    /**
     * Construct a column type.
     * 
     * @param id
     *            The id of the column type.
     * @param header
     *            The name of the column that is shown to the user in the header
     *            of the column.
     * @param pred
     *            A predicate that is used for equality checking of a string and
     *            a benchmark according the the benchmark string. So if the
     *            column type is algorithm then the algorithm field is compared
     *            to the string.
     * @param benchmarkCreator
     *            Method that produces a benchmark by copying the given
     *            benchmark but setting the string as the current type. Meaning
     *            that if the current type is algorithm then the string of the
     *            algorithm will be the string provided to the method.
     * @param reader
     *            Get the string of the benchmark that conforms the column type.
     *            Meaning that for the column algorithm the algorithm name
     *            should be returned.
     * @param cellValueFactory
     *            The Callback that is called when something changes.
     */
    public ColumnType(final int id, final String header,
            final BiPredicate<Benchmark, String> pred,
            final BiFunction<Benchmark, String, Benchmark> benchmarkCreator,
            final Function<Benchmark, String> reader,
            final Callback<CellDataFeatures<Benchmark, String>, ObservableValue<String>> cellValueFactory) {
        super();
        this.id = id;
        this.pred = pred;
        this.benchmarkCreator = benchmarkCreator;
        this.reader = reader;
        this.header = header;
        this.cellValueFactory = cellValueFactory;
        if (header == null) {
            throw new IllegalArgumentException("header may not be null");
        }
        if (pred == null) {
            throw new IllegalArgumentException("pred may not be null");
        }
        if (benchmarkCreator == null) {
            throw new IllegalArgumentException(
                    "benchmarkCreator may not be null");
        }
        if (reader == null) {
            throw new IllegalArgumentException("reader may not be null");
        }
        if (cellValueFactory == null) {
            throw new IllegalArgumentException(
                    "cellValueFactory may not be null");
        }
    }

    /**
     * The id of the column type.
     */
    final int id;

    /**
     * A predicate that is used for equality checking of a string and a
     * benchmark according the the benchmark string. So if the column type is
     * algorithm then the algorithm field is compared to the string.
     */
    final BiPredicate<Benchmark, String> pred;

    /**
     * Method that produces a benchmark by copying the given benchmark but
     * setting the string as the current type. Meaning that if the current type
     * is algorithm then the string of the algorithm will be the string provided
     * to the method.
     */
    final BiFunction<Benchmark, String, Benchmark> benchmarkCreator;

    /**
     * Get the string of the benchmark that conforms the column type. Meaning
     * that for the column algorithm the algorithm name should be returned.
     */
    final Function<Benchmark, String> reader;

    /**
     * The name of the column that is shown to the user in the header of the
     * column.
     */
    final String header;

    /**
     * The Callback that is called when something changes.
     */
    final Callback<CellDataFeatures<Benchmark, String>, ObservableValue<String>> cellValueFactory;
}
