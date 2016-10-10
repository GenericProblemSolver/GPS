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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import butt.gui.BenchmarkController;
import butt.gui.benchmark.treetable.CheckBoxTreeTableRow;
import butt.tool.benchmark.IRunnableBenchmark;
import butt.tool.benchmark.common.Benchmarks;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

/**
 * This class populates the table view that contains all the problems /
 * algorithm / result type combinations that are selectable with a checkbox.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class TableViewSetup {

    /**
     * The list of all items as Benchmark objects.
     */
    final static List<Benchmark> items = new ArrayList<>();

    /**
     * The Column definitions. The first argument represents the index of the
     * column definition in this array.
     * 
     * The column types are containing the column header and a mapper function
     * that provides an equals method for elements and a Benchmark Producer.
     */
    final static ColumnType columns[] = new ColumnType[] {
            new ColumnType(0, "Problem", (p, s) -> p.getProblem().equals(s),
                    (b, s) -> new Benchmark(b.getRunnerId(), s,
                            b.getResultType(), b.getAlgorithm()),
                    b -> b.getProblem(),
                    (TreeTableColumn.CellDataFeatures<Benchmark, String> param) -> new ReadOnlyStringWrapper(
                            param.getValue().getValue().getProblem())),
            new ColumnType(1, "Algorithm", (p, s) -> p.getAlgorithm().equals(s),
                    (b, s) -> new Benchmark(b.getRunnerId(), b.getProblem(),
                            b.getResultType(), s),
                    b -> b.getAlgorithm(),
                    (TreeTableColumn.CellDataFeatures<Benchmark, String> param) -> new ReadOnlyStringWrapper(
                            param.getValue().getValue().getAlgorithm())),
            new ColumnType(2, "Result Type",
                    (p, s) -> p.getResultType().equals(s),
                    (b, s) -> new Benchmark(b.getRunnerId(), b.getProblem(), s,
                            b.getAlgorithm()),
                    b -> b.getResultType(),
                    (TreeTableColumn.CellDataFeatures<Benchmark, String> param) -> new ReadOnlyStringWrapper(
                            param.getValue().getValue().getResultType())) };

    /**
     * Set the specified column as the first column.
     * 
     * @param tableView
     *            The table view whose columns should be swapped.
     * @param n
     *            The index of the column that should be swapped to index 0.
     * 
     * @throws NullPointerException
     *             if tableView is {@code null}.
     */
    public static void setFirstColumn(final TreeTableView<Benchmark> tableView,
            final int n) {
        ObservableList<TreeTableColumn<Benchmark, ?>> cols = tableView
                .getColumns();
        for (int i = 0; i < cols.size(); i++) {
            if (((ColumnType) (cols.get(i).getUserData())).id == n) {
                @SuppressWarnings("unchecked")
                TreeTableColumn<Benchmark, ?> c[] = cols
                        .toArray(new TreeTableColumn[cols.size()]);
                // swap to first column
                TreeTableColumn<Benchmark, ?> ii = c[i];
                TreeTableColumn<Benchmark, ?> oo = c[0];
                c[0] = ii;
                c[i] = oo;
                cols.setAll(Arrays.asList(c));
                return;
            }
        }
    }

    /**
     * Get a list of all selected items.
     * 
     * @param tableView
     *            The table view from where to obtain these items.
     * @return The list.
     * 
     * @throws NullPointerException
     *             if tableView is {@code null}.
     */
    public static List<Benchmark> getSelectedItems(
            final TreeTableView<Benchmark> tableView) {
        final boolean states[] = new boolean[items.size()];
        if (tableView.getRoot() != null) {
            saveStates((CheckBoxTreeItem<Benchmark>) tableView.getRoot(),
                    states);
        }
        return IntStream.range(0, states.length).filter(i -> states[i])
                .mapToObj(i -> items.get(i)).collect(Collectors.toList());
    }

    /**
     * Setup the table view. This populates the table view with all benchmarks
     * that the user can select and run.
     * 
     * @param tableView
     *            The control of the table view.
     * @param controller
     *            The controller that is responsible for the setup.
     * 
     * @throws NullPointerException
     *             if tableView is {@code null} or controller is {@code null}.
     */
    public static void setUp(final TreeTableView<Benchmark> tableView,
            final BenchmarkController controller) {

        if (controller == null) {
            throw new NullPointerException("controller may not be null");
        }

        // use our custom checkbox row
        tableView.setRowFactory(f -> new CheckBoxTreeTableRow<>());

        // if the user reorders the columns by drag and drop make sure that the
        // column semantic stays the same.
        tableView.getColumns().addListener(
                (ListChangeListener<? super TreeTableColumn<Benchmark, ?>>) ((
                        s) -> reoderTable(tableView)));

        for (final ColumnType col : columns) {
            TreeTableColumn<Benchmark, String> ttc = new TreeTableColumn<>(
                    col.header);
            // the user data of each column is the ColumnType object.
            ttc.setUserData(col);
            ttc.setCellValueFactory(col.cellValueFactory);
            tableView.getColumns().add(ttc);
        }

        // make sure all items are cleared if setup already has been called
        // before.
        items.clear();

        // run multithreaded since this may be slow
        Thread t = new Thread(() -> {
            final List<IRunnableBenchmark<?>> runners = Benchmarks
                    .constructAllBenchmarks();
            List<Benchmark> myItems = IntStream.range(0, runners.size())
                    .mapToObj(i -> new Benchmark(i,
                            runners.get(i).getBenchmarkName(),
                            runners.get(i).getResultType().name(),
                            runners.get(i).getAlgorithm().getName()))
                    .collect(Collectors.toList());
            Platform.runLater(() -> {
                items.clear();
                items.addAll(myItems);
                reoderTable(tableView);
                controller.getReflectionProgress().setVisible(false);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * This implements the logic that is applied when the user selects a
     * checkbox that represents multiple benchmarks. This will (un)check all
     * children of the selected node too.
     * 
     * @param columns
     *            The columns in their shown order from left to right.
     * @param root
     *            The node that is processed by this invoke.
     * @param depth
     *            The depth of the node in the tree. 0 means topmost.
     * @param next
     *            Strings of the groupings. Meaning 0 is the topmost root
     *            string. These strings are fields from the benchmark class.
     *            However it is not known what index is what column type. This
     *            is obtained by using methods provided by the ColumnType class.
     * @return The list of checked items.
     * @throws NullPointerException
     *             if one of the arguments is null
     */
    private static List<CheckBoxTreeItem<Benchmark>> recursiveGrouping(
            final List<ColumnType> columns,
            final CheckBoxTreeItem<Benchmark> root, final int depth,
            final String[] next) {
        if (root == null) {
            throw new NullPointerException("root may not be null");
        }
        List<CheckBoxTreeItem<Benchmark>> ml = new ArrayList<>();
        if (depth >= columns.size()) {
            ml.add(root);
            return ml;
        }
        String nextClone[] = Arrays.copyOf(next, next.length);
        Map<String, CheckBoxTreeItem<Benchmark>> map = new HashMap<>();
        Stream<Benchmark> stream = items.stream();
        for (int i = 0; i < depth; i++) {
            final int ii = i;
            stream = stream
                    .filter(p -> columns.get(ii).pred.test(p, nextClone[ii]));
        }
        stream.forEach(item -> {
            nextClone[depth] = columns.get(depth).reader.apply(item);

            Benchmark b = item;
            for (int i = columns.size() - 1; i > depth; i--) {
                b = columns.get(i).benchmarkCreator.apply(b, null);
            }

            CheckBoxTreeItem<Benchmark> newNode = new CheckBoxTreeItem<Benchmark>(
                    b);
            if (map.putIfAbsent(nextClone[depth], newNode) == null) {
                root.getChildren().add(newNode);
                ml.addAll(recursiveGrouping(columns, newNode, depth + 1,
                        nextClone));
            }

        });
        return ml;
    }

    /**
     * Save the states of the selections in an array. The array must be as long
     * as there are children and children of the children in the table view.
     * Each benchmark is stored in the array. The index is represented by the id
     * of the runner.
     * 
     * @param node
     *            The root node from where to recursively discover all leafs.
     * @param state
     *            The byte array where to store the states. {@code true} meaning
     *            "selected" and {@code false} meaning "unselected". Note that
     *            there is no indeterminate state because only leafs are stored.
     * @throws NullPointerException
     *             if one of the arguments is null
     */
    private static void saveStates(final CheckBoxTreeItem<Benchmark> node,
            final boolean state[]) {
        if (node.isLeaf()) {
            state[node.getValue().getRunnerId()] = node.isSelected();
            return;
        }
        node.getChildren().forEach(
                n -> saveStates((CheckBoxTreeItem<Benchmark>) n, state));
    }

    /**
     * Reorder table according to the order of the columns of the table view.
     * 
     * @param tableView
     *            The table view.
     * @throws NullPointerException
     *             if the tableView parameter is {@code null}.
     */
    private static void reoderTable(final TreeTableView<Benchmark> tableView) {

        // nothing to do here
        if (items.isEmpty()) {
            tableView.setRoot(null);
            return;
        }

        // save check box states
        final boolean states[] = new boolean[items.size()];
        if (tableView.getRoot() != null) {
            saveStates((CheckBoxTreeItem<Benchmark>) tableView.getRoot(),
                    states);
        }

        // build a new root
        CheckBoxTreeItem<Benchmark> root = new CheckBoxTreeItem<Benchmark>(
                new Benchmark(-1, null, null, null));

        // get the columns
        List<ColumnType> columns = tableView.getColumns().stream()
                .map(m -> (ColumnType) m.getUserData())
                .collect(Collectors.toList());

        // reorder tree and return all new leaf nodes
        final List<CheckBoxTreeItem<Benchmark>> newLeafs = recursiveGrouping(
                columns, root, 0, new String[columns.size()]);

        root.setExpanded(true);
        tableView.setRoot(root);

        // reload checkbox states
        for (CheckBoxTreeItem<Benchmark> n : newLeafs) {
            n.setSelected(states[n.getValue().getRunnerId()]);
        }
    }
}
