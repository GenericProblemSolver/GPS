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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butt.gui.BenchmarkController;
import gps.common.BenchmarkField;
import gps.common.IBenchmark;
import gps.util.KryoHelper;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;

/**
 * This class handles the communication between a spawned process and the
 * current process.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class BenchmarkProcess {

    /**
     * A map of benchmark fields to the chart series. Is generated during the
     * runtime of a benchmark.
     */
    public final Map<BenchmarkField, XYChart.Series<Number, Number>> series = new HashMap<>();

    /**
     * Pattern that is applied to each line that is used for the interprocess
     * communication.
     */
    private final static Pattern ipcPattern = Pattern.compile("BM(\\d+):(.+)");

    /**
     * Construct a Benchmark Runner. This is a management object for a spawned
     * process. All communication with the spawned process is handled in by this
     * object.
     * 
     * @param pProcess
     *            The process.
     * @param pBenchmark
     *            The benchmark used to spawn the process.
     * @param pController
     *            The javafx controller for the benchmarks.
     */
    public BenchmarkProcess(final Process pProcess, final Benchmark pBenchmark,
            final BenchmarkController pController) {
        super();

        if (pProcess == null) {
            throw new IllegalArgumentException("pProcess may not be null");
        }

        if (pBenchmark == null) {
            throw new IllegalArgumentException("pBenchmark may not be null");
        }

        if (pController == null) {
            throw new IllegalArgumentException("pController may not be null");
        }

        this.process = pProcess;
        this.benchmark = pBenchmark;

        // configure the alert window (that displays the console stream of the
        // spawned process)
        alertWindow.setTitle("Process Listener");
        alertWindow.setHeaderText("This window represents a running benchmark");
        alertWindow.setContentText(
                "Here is the output from stdout of the spawned process:");
        alertWindow.initModality(Modality.NONE);

        // this is where the text goes
        text = new TextArea();
        text.setEditable(false);

        // textArea.setWrapText(true);

        text.setMaxWidth(Double.MAX_VALUE);
        text.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(text, Priority.ALWAYS);
        GridPane.setHgrow(text, Priority.ALWAYS);

        alertWindow.getDialogPane().setExpandableContent(text);

        // spawn a demon thread that listens to the output stream of the spawned
        // process.
        Thread t = new Thread(() -> {
            String line;
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(pProcess.getInputStream()));
            IBenchmark ibm = null;
            long nanos = 0;
            try {
                while ((line = in.readLine()) != null) {
                    // if read end signal, we kill the child process
                    if (line.equals("END")) { // kill on end signal
                        pProcess.destroy();
                    }
                    // apply the pattern
                    Matcher m = ipcPattern.matcher(line);
                    if (m.matches()) {
                        line = ":" + line;
                        nanos = Long.valueOf(m.group(1));
                        try {
                            // reassemble the object that has been send
                            ibm = KryoHelper.stringToObject(m.group(2),
                                    gps.common.Benchmark.class);
                        } catch (RuntimeException e) {
                            line = line + "\n:failed to deserialize:"
                                    + e.getMessage();
                        }
                    }
                    final String l = line;
                    final IBenchmark bm = ibm;
                    final long time = nanos;
                    // all tasks that involve javafx must happen in the javafx
                    // thread. Therefore we must schedule them.
                    Platform.runLater(() -> {
                        if (bm != null) { // in case we read a benchmark
                                              // snapshot from the child process.
                            for (BenchmarkField field : bm.getUsedFields()) {
                                // find existing series
                                Series<Number, Number> ser = series.get(field);
                                if (ser == null) { // if not exists already...
                                                       // create a new one
                                    ser = new XYChart.Series<Number, Number>();
                                    ser.setName(pBenchmark.getAlgorithm() + ":"
                                            + pBenchmark.getProblem()
                                            + pBenchmark.getResultType());
                                    series.put(field, ser); // add to map
                                    if (pController.isSelectedView(field)) {
                                        // add to chart
                                        pController.getLineChart().getData()
                                                .add(ser);
                                    }
                                }
                                // add to series
                                Optional<? extends Number> val = bm
                                        .getField(field);
                                if (val.isPresent()) {
                                    ser.dataProperty().get()
                                            .add(new XYChart.Data<Number, Number>(
                                                    time, val.get()));
                                }
                            }
                        }
                        // also append the child process output to the text area
                        text.appendText(l + "\n");
                    });
                }
                Platform.runLater(() -> {
                    // executed if process ended
                    text.appendText("PROCESS ENDED");
                    if (!pProcess.isAlive()) {
                        // display the exit code
                        text.appendText(" exit code: " + pProcess.exitValue());
                    }
                });

            } catch (IOException e) {
                throw new RuntimeException("cannot forward STDOUT", e);
            }
        });
        t.setDaemon(true);
        t.start(); // launch the demon thread. It is terminated when the child
                   // process dies.
    }

    /**
     * The process this runner is listening to.
     */
    public final Process process;

    /**
     * The benchmark the process is doing.
     */
    public final Benchmark benchmark;

    /**
     * The text area of the process output stream. This is used by
     * {@link #alertWindow}.
     */
    public final TextArea text;

    /**
     * A window that shows the console output of the spawned process.
     */
    public final Alert alertWindow = new Alert(AlertType.INFORMATION);
}
