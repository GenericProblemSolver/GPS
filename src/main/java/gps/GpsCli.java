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
package gps;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Variable;
import gps.exception.NoSolverAvailableException;
import gps.games.algorithm.analysis.IGameAnalysisResult;
import gps.games.wrapper.Action;
import gps.optimization.OptimizationReturn;
import gps.preprocessing.AnnotationProcessor;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * This class implements a command line interface to use GPS.
 *
 * The main purpose is, that you can specify a source file that contains
 * a problem the user wants to solve.
 *
 * @author malu
 */
public class GpsCli {

    /**
     * Starts gps with a given file.
     *
     * This needs a file given with the option -f or --file to run.
     *
     * @param args the given arguments
     */
    public static void main(String[] args) {
        ParserArguments arguments = new ParserArguments();
        CmdLineParser parser = new CmdLineParser(arguments);
        // input parsing
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            // show usage in case of an error
            System.err.println(e.getMessage());
            System.err.println("Usage:");
            parser.printUsage(System.err);
            return;
        }

        File file = arguments.file;

        if (file != null) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                System.err.println("No tools.jar found or no JDK installed.");
                return;
            }
            File gpsTmpDir = new File("");
            try {

                // path to gps folder in tmp
                String tmpDir = System.getProperty("java.io.tmpdir")
                        + System.getProperty("file.separator") + "gps";

                DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
                StandardJavaFileManager fileManager = compiler
                        .getStandardFileManager(diagnostics, null, null);
                // options for the compiler
                List<String> optionList = new ArrayList<>();
                // create gps directory in tmp folder to store compiled classes
                gpsTmpDir = new File(tmpDir);
                if (!gpsTmpDir.exists()) {
                    gpsTmpDir.mkdir();
                }
                // tell the compiler to store the compiled files in the created tmp dir
                optionList.add("-d");
                optionList.add(tmpDir);

                Iterable<? extends JavaFileObject> compilationUnits = fileManager
                        .getJavaFileObjects(file);
                JavaCompiler.CompilationTask task = compiler.getTask(null,
                        fileManager, diagnostics, optionList, null,
                        compilationUnits);
                boolean success = task.call();

                if (success) {
                    System.out.println("Successfully compiled the file.");
                    URLClassLoader classLoader = new URLClassLoader(
                            new URL[] { new File(tmpDir).toURI().toURL() });
                    String className = file.getName().substring(0,
                            file.getName().lastIndexOf('.'));

                    System.out.println("Trying to load class: " + className);
                    Class<?> loadedClass = classLoader.loadClass(className);
                    Object obj = loadedClass.newInstance();

                    GPS<Object> gps = new GPS<>(obj);

                    try {
                        switch (arguments.method) {
                        case MOVES:
                            Optional<List<Action>> moves = gps.moves();
                            if (moves.isPresent()) {
                                for (Action a : moves.get()) {
                                    System.out.println(a.toString());
                                }
                            }
                            break;
                        case STATESEQUENCE:
                            Optional<List<Object>> stateSequence = gps
                                    .stateSequence();
                            if (stateSequence.isPresent()) {
                                for (Object o : stateSequence.get()) {
                                    System.out.println(o.toString());
                                }
                            }
                            break;
                        case TERMINALSTATE:
                            Optional<Object> terminalState = gps
                                    .terminalState();
                            if (terminalState.isPresent()) {
                                System.out.println(
                                        terminalState.get().toString());
                            }
                            break;
                        case BESTMOVE:
                            Optional<Action> bestMove = gps.bestMove();
                            if (bestMove.isPresent()) {
                                System.out.println(bestMove.get().toString());
                            }
                            break;
                        case ISWINNABLE:
                            Optional<Boolean> isWinnable = gps.isWinnable();
                            if (isWinnable.isPresent()) {
                                System.out.println(isWinnable.get());
                            }
                            break;
                        case ISFINISHED:
                            boolean isFinished = gps.isFinished();
                            System.out.println(isFinished);
                            break;
                        case GAMEANALYSIS:
                            Optional<IGameAnalysisResult> gameAnalysis = gps
                                    .gameAnalysis();
                            if (gameAnalysis.isPresent()) {
                                System.out
                                        .println(gameAnalysis.get().toString());
                            }
                            break;
                        case MAXIMIZE:
                            Optional<OptimizationReturn> maxResult = gps
                                    .maximize();
                            if (maxResult.isPresent()) {
                                System.out.println(maxResult.get().toString());
                            }
                            break;
                        case MINIMIZE:
                            Optional<OptimizationReturn> minResult = gps
                                    .minimize();
                            if (minResult.isPresent()) {
                                System.out.println(minResult.get().toString());
                            }
                            break;
                        case SATISFY:
                            Optional<Map<Variable, Constant>> byteResult = gps
                                    .satisfyingModel();
                            if (byteResult.isPresent()) {
                                for (Map.Entry<Variable, Constant> e : byteResult
                                        .get().entrySet()) {
                                    System.out.println("Variable: "
                                            + e.getKey().toString() + " Value: "
                                            + e.getValue().toString());
                                }
                            }
                            break;
                        }
                    } catch (NoSolverAvailableException e) {
                        System.err.println(
                                "Either you have no solver installed or you are "
                                        + "trying to use a method not suitable for"
                                        + " the solver determined from the annotations "
                                        + "in your class.");
                    }

                } else {
                    System.out.println("Failed to compile.");
                    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics
                            .getDiagnostics()) {
                        System.out.format("Error on line %d in %s%n",
                                diagnostic.getLineNumber(),
                                diagnostic.getSource().toUri());
                        System.out.println(diagnostic.toString());
                    }
                }
                fileManager.close();
            } catch (IOException | ClassNotFoundException
                    | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                removeDirectory(gpsTmpDir);
            }

        } else {
            System.out.println("No file given");
        }

    }

    /**
     * Removes a directory.
     *
     * This method checks if a directory is not empty and removes the
     * directory and all files contained in it.
     * @param dir the directory to delete
     */
    private static void removeDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File aFile : files) {
                    removeDirectory(aFile);
                }
            }
        }
        dir.delete();
    }
}

/**
 * Stores the parsed arguments.
 */
class ParserArguments {
    public File file = null;

    @Option(name = "-f", aliases = {
            "--file" }, required = true, usage = "Path to the java source file.")
    public void setFile(File f) {
        if (f.exists()) {
            file = f;
        }
    }

    @Option(name = "-m", aliases = {
            "--method" }, required = true, usage = "GPS Method that should be called.")
    public StartType method;

    public enum StartType {
        MOVES, STATESEQUENCE, TERMINALSTATE, BESTMOVE, ISWINNABLE, ISFINISHED, GAMEANALYSIS, MAXIMIZE, MINIMIZE, SATISFY;
    }
}
