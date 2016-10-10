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
package butt.gui;

import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butt.gui.benchmark.Benchmark;
import butt.gui.benchmark.BenchmarkProcess;
import butt.gui.benchmark.TableViewSetup;
import butt.tool.BenchmarkTool;
import gps.common.BenchmarkField;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeTableView;
import javafx.util.Duration;

/**
 * Controller for benchmark gui
 * 
 * @author haker@uni-bremen.de
 *
 */
public class BenchmarkController implements Runnable {

    /**
     * threads that are running benchmarks
     */
    final List<BenchmarkProcess> benchmarkThreads = new ArrayList<>();

    /**
     * threads that were running benchmarks
     */
    final List<BenchmarkProcess> benchmarkData = new ArrayList<>();

    final Timeline diagramRefreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(0.5), (e) -> run()));

    /**
     * The treeView in the problem tab
     */
    @FXML
    private TreeTableView<Benchmark> benchmarkTreeTableView;

    /**
     * The line chart that displays the benchmark graph
     */
    @FXML
    private LineChart<Number, Number> lineChart;

    /**
     * The choice box control for the view. This is the choice box where the
     * user can set what benchmark field should be rendered.
     */
    @FXML
    private ChoiceBox<BenchmarkField> viewChoiceBox;

    /**
     * Combobox where the user can input a time space for benchmarks. The String
     * may be a Number and a Unit specifier like s, ms, m and d.
     */
    @FXML
    private ComboBox<String> timeCombobox;

    /**
     * Pattern for the time combobox. TODO tobi improve pattern
     */
    private static Pattern pattern = Pattern
            .compile("(?<val>\\d+)(?<unit>ms|s|m|h|d|)");

    /**
     * Checks whether the string is a valid time descriptor.
     * 
     * @param s
     *            The string
     * @return {@code true} if valid. {@code false} if the string does not match
     *         the pattern {@link #pattern}.
     */
    private static boolean isValidTimePattern(String s) {
        final Matcher m = pattern.matcher(s);
        return s.equals("NONE") || m.matches();
    }

    /**
     * Return the time specified by the user in the time combobox in
     * milliseconds.
     * 
     * @return The time in milliseconds.
     */
    private long getAlgotimeInMs() {
        final String selection = timeCombobox.getSelectionModel()
                .getSelectedItem();
        Matcher m = pattern.matcher(selection);
        if (!m.matches()) {
            return -1;
        }
        long val = Long.valueOf(m.group("val"));
        switch (m.group("unit")) {
        case "d":
            val *= 24;
        case "h":
            val *= 60;
        case "m":
            val *= 60;
        case "s":
        case "":
            val *= 1000;
        case "ms":
        }
        return val;
    }

    /**
     * Chechs whether the given field is currently selected.
     * 
     * @param field
     *            The BenchmarkField
     * @return {@code true} if selected. {@code false} otherwise.
     */
    public boolean isSelectedView(BenchmarkField field) {
        BenchmarkField item = viewChoiceBox.getSelectionModel()
                .getSelectedItem();
        return item != null && item.equals(field);
    }

    /**
     * Get the line chart control that displays the benchmark graph
     * 
     * @return The LineChart.
     */
    public LineChart<Number, Number> getLineChart() {
        return lineChart;
    }

    /**
     * Gets called by fxml loader once after every attribute has been
     * inisitalized
     */
    public void initialize() {
        lineChart.setCreateSymbols(false);
        TableViewSetup.setUp(benchmarkTreeTableView, this);
        diagramRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        viewChoiceBox.getItems().addAll(BenchmarkField.values());
        viewChoiceBox.getSelectionModel().selectFirst();
        viewChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ChangeListener<? super BenchmarkField>) ((observer, oldVal,
                        newVal) -> {
                    lineChart.getData().clear();
                    for (BenchmarkProcess b : benchmarkData) {
                        if (b.series.containsKey(newVal)) {
                            lineChart.getData().add(b.series.get(newVal));
                        }
                    }
                }));

        timeCombobox.getSelectionModel().selectedItemProperty()
                .addListener((ChangeListener<? super String>) ((observer,
                        oldVal, newVal) -> {
                    if (!isValidTimePattern(newVal)
                            && isValidTimePattern(oldVal)) {
                        timeCombobox.getSelectionModel().select(oldVal);
                    }
                }));
    }

    /**
     * Method that is inwoked by the fxml controler when the table view radio
     * button "problem" is clicked.
     * 
     * @param e
     *            The Action Event.
     */
    @FXML
    private void onTableViewRadioProblem(ActionEvent e) {
        TableViewSetup.setFirstColumn(benchmarkTreeTableView, 0);
    }

    /**
     * Method that is inwoked by the fxml controler when the table view radio
     * button "algorithm" is clicked.
     * 
     * @param e
     *            The Action Event.
     */
    @FXML
    private void onTableViewRadioAlgorithm(ActionEvent e) {
        TableViewSetup.setFirstColumn(benchmarkTreeTableView, 1);
    }

    /**
     * Method that is inwoked by the fxml controler when the table view radio
     * button "result" is clicked.
     * 
     * @param e
     *            The Action Event.
     */
    @FXML
    private void onTableViewRadioResult(ActionEvent e) {
        TableViewSetup.setFirstColumn(benchmarkTreeTableView, 2);
    }

    /**
     * Method that is inwoked by the fxml controler when the stop button is
     * clicked.
     * 
     * @param e
     *            The Action Event.
     */
    @FXML
    private void onStopButton(ActionEvent e) {
        // stop to refresh the diagram
        diagramRefreshTimeline.stop();

        // kill all processes
        benchmarkThreads.stream().forEach(p -> p.process.destroy());
        benchmarkThreads.clear();
    }

    /**
     * Method that is inwoked by the fxml controler when the run button is
     * clicked.
     * 
     * @param e
     *            The Action Event.
     */
    @FXML
    private void onRunButton(ActionEvent e) {
        onStopButton(e); // first, make sure everything is stopped

        // get the benchmarks whose checkboxes are checked
        List<Benchmark> selectedBenchmarks = TableViewSetup
                .getSelectedItems(benchmarkTreeTableView);

        // clear all necessary structures
        lineChart.getData().clear();
        benchmarkData.clear();

        selectedBenchmarks.forEach(bm -> {
            // for each selected benchmark start a new jvm
            final BenchmarkProcess p = new BenchmarkProcess(
                    BenchmarkTool.respawn(
                            new String[] { "-b",
                                    Integer.toString(bm.getRunnerId()), "-tb",
                                    Long.toString(getAlgotimeInMs()), "-hl" },
                            Redirect.PIPE),
                    bm, this);

            // Display a debug window
            // p.alertWindow.show();

            benchmarkData.add(p);
            benchmarkThreads.add(p);
        });

        // when the timeline progresses the line chart is refreshed.
        diagramRefreshTimeline.playFromStart();

        // sets the prograss indicator visible. It is turned invisible if the
        // benchmark is done or if the user stops it.
        progessInficator.setVisible(true);
    }

    /**
     * Called when the diagram is to be refreshed. Is called periodically from
     * javafx thread.
     */
    @Override
    public void run() {
        // check processes
        for (int i = 0; i < benchmarkThreads.size(); i++) {
            final BenchmarkProcess p = benchmarkThreads.get(i);
            if (!p.process.isAlive()) {
                // this process dies, so we remove it from the list
                benchmarkThreads.remove(i--);
                continue;
            }
        }

        // Nothing more to do, so we stop
        if (benchmarkThreads.isEmpty()) {
            progessInficator.setVisible(false);
            diagramRefreshTimeline.stop();
        }
    }

    /**
     * Get the progress indicator control.
     * 
     * @return the indicator.
     */
    public ProgressIndicator getReflectionProgress() {
        return reflectionProgress;
    }

    /**
     * The progress indicator. Is shown when a benchmark is running.
     */
    @FXML
    ProgressIndicator progessInficator;

    /**
     * This progress indicator is shown when the algorithms are in instantiation
     * phase.
     */
    @FXML
    private ProgressIndicator reflectionProgress;
}