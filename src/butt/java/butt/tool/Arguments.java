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

import java.io.File;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import butt.MainBUTT;
import gps.util.Tuple;

/**
 * Class that manages the arguments that has been passed to the program.
 * 
 * This class provides arguments that have been passed to the tool.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class Arguments {
    @Parameter(names = { "--target",
            "-t" }, description = "The csv filename of the created benchmarks")
    private String csvFilename = null;

    @Parameter(names = { "--evaluate",
            "-e" }, description = "The csv filename of the created training data.")
    private String trainingDataCsv = null;

    @Parameter(names = { "--benchmark",
            "-b" }, description = "The benchmark id to execute. If omitted or negative all benchmarks are run.")
    private Integer benchmarkNo = null;

    @Parameter(names = { "--append",
            "-a" }, description = "Append to the csv file instead of creating a new one")
    private boolean appendToCsv = false;

    @Parameter(names = { "--seq",
            "-s" }, description = "Run all benchmarks in a single jvm")
    private boolean runInCurrentJvm = false;

    @Parameter(names = { "--no-benchmark",
            "-nb" }, description = "Do not run benchmarks")
    private boolean noBenchmark = false;

    @Parameter(names = { "--no-learning",
            "-nl" }, description = "Do not learn the neuronal network")
    private boolean noLearning = false;

    @Parameter(names = { "--no-evaluation",
            "-ne" }, description = "Do not evaluate the benchmarks")
    private boolean noEvaluation = false;

    @Parameter(names = { "--headless",
            "-hl" }, description = "Do launch in headless mode")
    private boolean noGui = false;

    @Parameter(names = { "--processes",
            "-p" }, description = "Maximum number of simultaneously calculationg processes")
    private Integer maxProcesses = 1;

    @Parameter(names = { "--time-limit",
            "-tl" }, description = "Maximum time for learning the neuronal network in seconds")
    private Integer maxLearningTimeInSeconds = 10;

    @Parameter(names = { "--benchmark-time",
            "-tb" }, description = "Time each benchmark is run in milliseconds. Choose non positive if no limit is set and to stop imedeatly after a solution has been found.")
    private Long benchmarkTime = 5000L;

    @Parameter(names = { "--status-update-time",
            "-st" }, description = "Status update time interval in milliseconds. In a periodical interval print benchmark data. Defaults to 500. If non-positive the output is supressed")
    private Integer statusUpdateIntervalMillis = 50;

    @Parameter(names = { "--filter-algorithm",
            "-fa" }, description = "Only consider algorithms (class name) that match the given regular expression")
    private String filterAlgorithmRexExp = ".*";

    @Parameter(names = { "--filter-result",
            "-fr" }, description = "Only consider result types (enum element name) that match the given regular expression")
    private String filterResultRexExp = ".*";

    @Parameter(names = { "--filter-problem",
            "-fp" }, description = "Only consider problems whose names match the given regular expression")
    private String filterProblemRexExp = ".*";

    @Parameter(names = { "--butt-jar",
            "-j" }, description = "The location of the but jar that is launched when a process should be spawned. If not present the location of the current jar is used. This is only possible if the application is launched from a jar.")
    private String buttJar = null;

    @Parameter(names = { "--divide",
            "-d" }, description = "Only calculate every n-th benchmark. Useful if running the benchmarks in a cloud. Default is 1.")
    private String benchmarkDivider = "1/1";

    @Parameter(names = { "--help",
            "-h" }, help = true, description = "Display this help text")
    private boolean help = false;

    /**
     * Singleton instance for the class
     */
    public static final Arguments instance = new Arguments();

    /**
     * Singleton class does not provide constructor.
     */
    private Arguments() {

    }

    /**
     * The filename where the benchmarks are located.
     * 
     * @return The String representing the filename.
     */
    public static Optional<String> getTargetFilename() {
        return Optional.ofNullable(instance.csvFilename);
    }

    /**
     * The filename where the evaluation is supposed to be stored.
     * 
     * @return The String representing the filename.
     */
    public static Optional<String> getEvaluationFilename() {
        return Optional.ofNullable(instance.trainingDataCsv);
    }

    /**
     * The benchmark index of the benchmark to run.
     * 
     * @return An Integer representing the benchmark index for
     *         {@link #benchmarks}. If not set, all benchmarks are supposed to
     *         run.
     */
    public static Optional<Integer> getBenchmarkNo() {
        return Optional.ofNullable(instance.benchmarkNo);
    }

    /**
     * A flag that determines whether the benchmark is supposed to append to an
     * possible existing file.
     * 
     * @return {@code true} if appending is desired, {@code false} otherwise.
     */
    public static boolean isAppendToCsv() {
        return instance.appendToCsv;
    }

    /**
     * A flag that determines whether the benchmarks are supposed to run in the
     * current JVM or whether a new JVM should be spawned for them.
     * 
     * @return {@code true} if the benchmarks are to run in this JVM,
     *         {@code false} if a new JVM should be spawned.
     */
    public static boolean isRunInCurrentJvm() {
        return instance.runInCurrentJvm;
    }

    /**
     * A flag that determines whether the benchmarking should be skipped.
     * 
     * This might be useful if only an evaluation of already benchmarked
     * algorithms is desired.
     * 
     * @return {@code true} if no benchmarks are supposed to run, {@code false}
     *         otherwise.
     */
    public static boolean isNoBenchmark() {
        return instance.noBenchmark;
    }

    /**
     * A flag that determines whether the learning should be skipped.
     * 
     * This might be useful if only an evaluation of already benchmarked
     * algorithms is desired and no neuronal network file.
     * 
     * @return {@code true} if no generation of a neuronal network is desired,
     *         {@code false} otherwise.
     */
    public static boolean isNoLearning() {
        return instance.noLearning;
    }

    /**
     * A flag that determines whether the evaluation should be skipped.
     * 
     * This might be useful if only an training of already evaluated algorithms
     * is desired and no new evaluation.
     * 
     * @return {@code true} if no generation of a evaluation csv is desired,
     *         {@code false} otherwise.
     */
    public static boolean isNoEvaluation() {
        return instance.noEvaluation;
    }

    private static final Pattern benchmarkDividerPattern = Pattern
            .compile(".*?([0-9]+).+?([0-9]+).*?");

    private Tuple<Integer, Integer> benchmarkDividerTuple = null;

    /**
     * Get the benchmark divider that has been passed to the application.
     * 
     * @return The tuple. The first value represents the part and the second
     *         represents the total amount of parts.
     */
    public static Tuple<Integer, Integer> getBenchmarkDivider() {
        if (instance.benchmarkDividerTuple != null) {
            return instance.benchmarkDividerTuple;
        }
        Matcher m = benchmarkDividerPattern.matcher(instance.benchmarkDivider);
        if (!m.matches()) {
            throw new RuntimeException("Divider Pattern does not match");
        }
        instance.benchmarkDividerTuple = new Tuple<>(
                Integer.valueOf(m.group(1)), Integer.valueOf(m.group(2)));
        return instance.benchmarkDividerTuple;
    }

    /**
     * Is {@code true} if the BUTT should run in headless mode.
     * 
     * @return {@code true} if should run in headless mode. {@code false}
     *         otherwise.
     */
    public static boolean isNoGui() {
        return instance.noGui;
    }

    /**
     * Get the maximum number of jvm processes that are allowed to run
     * simultaneously. Defaults to 1.
     * 
     * @return The maximum number of concurrently running benchmarks.
     */
    public static Integer getMaxProcesses() {
        return instance.maxProcesses;
    }

    /**
     * Returns the status update time interval in milliseconds. In a periodical
     * interval print benchmark data. Defaults to 500. If non-positive the
     * output is suppressed.
     * 
     * @return The time in milliseconds.
     */
    public static Integer getStatusUpdateIntervalMillis() {
        return instance.statusUpdateIntervalMillis;
    }

    /**
     * Return the regular expression that matches all algorithm classes that are
     * considered by the tools.
     * 
     * @return The String containing an regular expression
     */
    public static String getFilterAlgorithmRexExp() {
        return instance.filterAlgorithmRexExp;
    }

    /**
     * Return the regular expression that matches all result types that are
     * considered by the tools. This result types are matched with the enum
     * element names.
     * 
     * @return The String containing an regular expression
     */
    public static String getFilterResultRexExp() {
        return instance.filterResultRexExp;
    }

    /**
     * Return the regular expression that matches all problem names of problems
     * that are considered by the tools. The problem names are the names of the
     * problem that are printed in the CSV file.
     * 
     * @return The String containing an regular expression
     */
    public static String getFilterProblemRexExp() {
        return instance.filterProblemRexExp;
    }

    /**
     * Return the file of the butt jar. Prefers the jar that this jvm has been
     * initialized with. If the JVM is not running a jar than the filename
     * provided in the argument is considered.
     * 
     * @return The file. Is never {@code null}.
     */
    public static File getButtJar() {
        File jarFile;
        try {
            jarFile = new File(MainBUTT.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // if not a jar try filename by user
        if (!jarFile.getName().endsWith(".jar")) {
            if (instance.buttJar == null) {
                throw new RuntimeException(
                        "Not Running a .jar file. Use -s argument to start the program or -j to manually add a filename for the jar");
            }
            jarFile = new File(instance.buttJar);
            if (!jarFile.canRead()) {
                throw new RuntimeException(
                        "The filename specified in the -j param is not readable");
            }
        }

        return jarFile;
    }

    /**
     * Get the maximum time in seconds for the learning phase of the neuronal
     * network. Defaults to 10.
     * 
     * @return The time limit in seconds.
     */
    public static Integer getMaxLearningTime() {
        return instance.maxLearningTimeInSeconds;
    }

    /**
     * Get the timelimit in milliseconds that each benchmark will run. It will
     * get resumed after solutions have been found until this timelimit has been
     * passed.
     * 
     * @return The time limit in milliseconds.
     */
    public static Optional<Long> getBenchmarkTime() {
        return Optional.ofNullable(
                instance.benchmarkTime <= 0 ? null : instance.benchmarkTime);
    }

    /**
     * Parse the passed arguments and populate the {@link Arguments} class.
     * 
     * @param args
     *            The arguments as passed to the main method.
     */
    public static void parse(final String[] args) {
        final JCommander commander = new JCommander(instance, args);
        if (instance.help) {
            commander.usage();
            System.exit(0); // don't do anything just display the help text
        }
    }
}
