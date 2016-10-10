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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import butt.tool.benchmark.IRunnableBenchmark;
import butt.tool.benchmark.common.Benchmarks;
import butt.tool.benchmark.common.CSV;
import butt.tool.benchmark.common.ProcessQueue;
import butt.tool.benchmark.common.TimeInterval;
import gps.util.KryoHelper;
import gps.util.Tuple;

/**
 * Benchmark tool. Allows to automatically benchmark all problems provided in
 * this tool. Each algorithm is benchmarked with every possible problem class
 * and all possible result type.
 * 
 * This data can be used by the {@link EvaluationTool} to generate training
 * data.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class BenchmarkTool {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger
            .getLogger(BenchmarkTool.class.getCanonicalName());

    /**
     * Run the benchmark tool.
     */
    public static void run() {
        final List<IRunnableBenchmark<?>> benchmarks = Benchmarks
                .constructAllBenchmarks().stream()
                .filter(p -> p.isApplicable(
                        Arguments.getFilterAlgorithmRexExp(),
                        Arguments.getFilterResultRexExp(),
                        Arguments.getFilterProblemRexExp()))
                .collect(Collectors.toList());

        final Optional<CSV> csv;
        if (Arguments.getTargetFilename().isPresent()) {
            csv = Optional.of(new CSV(Arguments.getTargetFilename().get(),
                    Arguments.isAppendToCsv()));
        } else {
            csv = Optional.empty();
        }

        // the range of the benchmark id's of the benchmark we process
        final int minBenchmarkId; // inclusive
        final int maxBenchmarkId; // exclusive

        {
            // configure the tasks
            Tuple<Integer, Integer> tuple = calculateTaskRange(
                    benchmarks.size(), Arguments.getBenchmarkDivider().getX(),
                    Arguments.getBenchmarkDivider().getY());

            minBenchmarkId = tuple.getX();
            maxBenchmarkId = tuple.getY();
        }

        try {
            List<IRunnableBenchmark<?>> bms = benchmarks;
            if (Arguments.getBenchmarkNo().isPresent()) {
                // run a single benchmark
                final IRunnableBenchmark<?> r;
                try {
                    r = bms.get(Arguments.getBenchmarkNo().get());
                } catch (IndexOutOfBoundsException e) {
                    throw new RuntimeException(
                            "Specify a valid benchmark number. Maximum is "
                                    + (benchmarks.size() - 1),
                            e);
                }
                final Thread t;
                TimeInterval ti = new TimeInterval();
                ti.start();
                if (Arguments.getStatusUpdateIntervalMillis() > 0) {
                    System.out.println("BM0:"
                            + KryoHelper.objectToString(r.getBenchmark()));
                    t = new Thread(() -> {
                        while (!Thread.currentThread().isInterrupted()) {
                            ti.end();
                            System.out.println(
                                    "BM" + ti.deltaNanos() + ":" + KryoHelper
                                            .objectToString(r.getBenchmark()));
                            try {
                                Thread.sleep(Arguments
                                        .getStatusUpdateIntervalMillis());
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        ti.end();
                        System.out.println("BM" + ti.deltaNanos() + ":"
                                + KryoHelper.objectToString(r.getBenchmark()));
                        System.out.println("END");
                        LOGGER.info("thead " + Thread.currentThread().getId()
                                + " ended");
                    });
                    t.setDaemon(true);
                    LOGGER.info("thead " + t.getId() + " is starting");
                    t.start();
                } else {
                    t = null;
                }
                if (Arguments.getStatusUpdateIntervalMillis() > 0) {
                    System.out.println(
                            "Running BM #" + Arguments.getBenchmarkNo().get()
                                    + "/" + benchmarks.size());
                }
                r.run(csv);
                if (t != null) {
                    t.interrupt();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                    }
                }
                return;
            }

            // run all benchmarks in single file
            if (Arguments.isRunInCurrentJvm()) {
                int i = 0;
                final TimeInterval ti = new TimeInterval();
                ti.start();
                for (int bmId = minBenchmarkId; bmId < maxBenchmarkId; bmId++) {
                    if (Thread.interrupted()) {
                        return;
                    }
                    IRunnableBenchmark<?> b = benchmarks.get(bmId);
                    System.out
                            .println(
                                    "Running BM #" + bmId + " of "
                                            + benchmarks.size() + " (" + i + "/"
                                            + (maxBenchmarkId - minBenchmarkId)
                                            + ") "
                                            + String.format("%.2f",
                                                    ((double) i * 100d
                                                            / (double) (maxBenchmarkId
                                                                    - minBenchmarkId)))
                                            + "% (eta:"
                                            + TimeInterval.string(ti.calcEta(i,
                                                    (maxBenchmarkId
                                                            - minBenchmarkId)))
                                            + "), running for " + TimeInterval
                                                    .string(ti.deltaNanos()));
                    i++;
                    b.run(csv);
                }
                return;
            }
        } finally {
            csv.ifPresent(c -> c.close());
        }

        // delete tmp files
        for (int i = 0; i < Arguments.getMaxProcesses(); i++) {
            new File(Arguments.getTargetFilename().get() + ".tmp" + i).delete();
        }

        // number of finished tasks
        int finished[] = new int[] { 0 };

        // launch all the benchmarks in a freshly spawned jvm
        try {
            final TimeInterval ti = new TimeInterval();
            ti.start();
            ProcessQueue.schedule(Arguments.getMaxProcesses(), (pid, i) -> {
                int bmId = i + minBenchmarkId;
                int fin = finished[0];
                System.out
                        .println(
                                "Running BM #" + bmId + " of "
                                        + benchmarks.size() + " (" + i + "/"
                                        + (maxBenchmarkId - minBenchmarkId)
                                        + ") "
                                        + String.format("%.2f",
                                                ((double) fin * 100d
                                                        / (double) (maxBenchmarkId
                                                                - minBenchmarkId)))
                                        + "% (eta:"
                                        + TimeInterval.string(ti.calcEta(fin,
                                                (maxBenchmarkId
                                                        - minBenchmarkId)))
                                        + "), running for "
                                        + TimeInterval.string(ti.deltaNanos()));
                return (bmId < maxBenchmarkId)
                        ? respawn(
                                new String[] { "-b", String.valueOf(bmId), "-a",
                                        "-tb",
                                        "" + Arguments
                                                .getBenchmarkTime().orElse(
                                                        5000L),
                                        "-st", "-1", "-hl", "-t",
                                        "\"" + Arguments.getTargetFilename()
                                                .get()
                                                + (Arguments
                                                        .getMaxProcesses() > 1
                                                                ? ".tmp" + pid
                                                                : "")
                                                + "\"" },
                                Redirect.INHERIT)
                        : null;
            }, (p) -> {
                p.destroy();
                finished[0]++;
                ti.end();
            });
        } catch (InterruptedException e) {
        }

        if (Arguments.getMaxProcesses() > 1
                && Arguments.getTargetFilename().isPresent()) {
            // merge csv files
            final PrintWriter out;
            try {
                out = new PrintWriter(new FileWriter(
                        Arguments.getTargetFilename().get(), true));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            boolean exception = false;
            try {
                for (int i = 0; i < Arguments.getMaxProcesses(); i++) {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new FileReader(
                                Arguments.getTargetFilename().get() + ".tmp"
                                        + i));
                        String s;
                        while ((s = reader.readLine()) != null) {
                            out.println(s);
                        }
                    } catch (IOException e) {
                        exception = true;
                    } finally {
                        try {
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (IOException e) {
                        }
                    }
                }
            } finally {
                out.close();
            }

            // delete temporary files
            if (!exception) {
                for (int i = 0; i < Arguments.getMaxProcesses(); i++) {
                    new File(Arguments.getTargetFilename().get() + ".tmp" + i)
                            .delete();
                }
            }
        }
    }

    /**
     * Calculate the range of benchmarks that are run.
     * 
     * @param maxJobs
     *            The maximum number of jobs available. This is the number of
     *            total benchmarks.
     * @param part
     *            The dividend. Must be greater than 0.
     * @param parts
     *            The divisor. Must be greater or equal to part.
     * @return The tuple with the first element being the minimum id and the
     *         second the maximum id.
     */
    public static Tuple<Integer, Integer> calculateTaskRange(int maxJobs,
            int part, int parts) {
        if (part <= 0) {
            throw new RuntimeException("part must be greater than 0");
        }
        if (parts <= 0) {
            throw new RuntimeException("parts must be greater than 0");
        }
        if (part > parts) {
            throw new RuntimeException("part must be equal or less than parts");
        }
        if (maxJobs < 0) {
            throw new RuntimeException("maxJobs must not be negative");
        }
        final int elements = maxJobs / parts;
        return new Tuple<>((part - 1) * elements,
                (part == parts) ? maxJobs : part * elements);
    }

    /**
     * Spawn the current jar in a new jvm.
     * 
     * Adapted from
     * http://stackoverflow.com/questions/4159802/how-can-i-restart-a -java-
     * application/4194224#4194224
     * 
     * @param arguments
     *            The arguments for the new process.
     * 
     * @return The newly spawned process
     * 
     * @throws RuntimeException
     *             if the current running program is not a jar
     * 
     * @throws RuntimeException
     *             if the current class path URI is not well formatted
     * 
     */
    public static Process respawn(String[] arguments, Redirect redirectOutput) {
        final String javaBin = System.getProperty("java.home") + File.separator
                + "bin" + File.separator + "java";

        File currentJar = Arguments.getButtJar();

        // build the command
        final ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-cp");
        // TODO tobi add all parents jvm arguments too
        command.add(currentJar.getPath());
        command.add("butt.MainBUTT");
        command.addAll(Arrays.asList(arguments));

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectOutput(redirectOutput);
        builder.redirectError(Redirect.INHERIT);
        try {
            return builder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Singleton class cannot be instantiated.
     */
    private BenchmarkTool() {

    }
}
