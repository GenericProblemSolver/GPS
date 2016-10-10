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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;

import butt.tool.evaluation.CSV;
import butt.tool.evaluation.CSVReader;
import gps.ResultEnum;
import gps.common.AlgorithmUtility;
import gps.games.GamesModule;
import gps.games.algorithm.AbstractGameAlgorithm;
import gps.games.algorithm.analysis.IGameAnalysisResult;
import gps.games.util.AlgorithmComparator;

/**
 * Neuronal Network Tool. Is a tool for creating and training the neuronal
 * network that can be used to classify problems and find a suittable algorithm
 * for them.
 * 
 * This data used for training is created with {@link EvaluationTool}.
 * 
 * For each result type a neuronal network is constructed. This ensures that
 * each algorithm is considered in different disciplines. Therefore no algorithm
 * won't be discriminated due to the fact that it performed bad on a specific
 * result type.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class NeuronalNetworkTool {

    /**
     * The size of the input vector of the neuronal network.
     */
    private static final int INPUT_VECTOR_SIZE = IGameAnalysisResult.VECTOR_SIZE;

    /**
     * Run the neuronal network tool.
     */
    public static void run() {
        System.out.println("Stating training");

        // read the csv file with all the evaluated algorithms
        final List<Map<String, String>> lines;
        {
            CSVReader reader = new CSVReader(
                    Arguments.getEvaluationFilename().get());
            lines = reader.readAllLines();
            reader.close();
        }

        // generate a neuronale network for each result type
        for (final ResultEnum e : ResultEnum.values()) {
            // load all algorithms as name
            // since we can have multiple algorithm instantiations per class we
            // need to
            // get these variants too.
            // TODO tobi: this does not work.
            final String[] algorithms = AlgorithmUtility
                    .instantiateAllAlgorithms(AbstractGameAlgorithm.class, e,
                            GamesModule.createDummy())
                    .stream().sorted(new AlgorithmComparator())
                    .map(m -> m.getName().replace(",", ";"))
                    .toArray(i -> new String[i]);

            // The size of the output vector of the neuronal network.
            final int OUTPUT_VECTOR_SIZE = algorithms.length;

            // the set for all the training data
            final DataSet trainingData = new DataSet(INPUT_VECTOR_SIZE,
                    OUTPUT_VECTOR_SIZE);

            // find each distinct game
            lines.stream().map(m -> m.get(CSV.FIELD_PROBLEM)).distinct()
                    .map(game -> {
                        // Algorithm -> Score
                        final Map<String, Double> algo2Score = new HashMap<>();

                        // map each game to a data set row for the training
                        lines.stream().filter(p -> e.toString()
                                .equals(p.get(CSV.FIELD_RESULT_TYPE))
                                && game.equals(p.get(CSV.FIELD_PROBLEM)))
                                .forEach(c -> algo2Score.put(
                                        c.get(CSV.FIELD_ALGORITHM),
                                        Double.valueOf(
                                                c.get(CSV.FIELD_SCORE))));

                        Optional<String> vector = lines.stream()
                                .filter(p -> game
                                        .equals(p.get(CSV.FIELD_PROBLEM)))
                                .findAny().map(m -> m.get(CSV.FIELD_VECTOR));

                        if (!vector.isPresent()) {
                            throw new RuntimeException(
                                    "vector is empty. This can never happen since a line with the current problem already has been found.");
                        }

                        final double[] inputVector = new double[INPUT_VECTOR_SIZE];
                        // read the input vector
                        {
                            String doublesAsString[] = vector.get()
                                    .replace("{", "").replace("}", "")
                                    .replace(" ", "").split(";");
                            if (doublesAsString.length != INPUT_VECTOR_SIZE) {
                                throw new RuntimeException(
                                        "The vector size in the eval csv is invalid");
                            }
                            for (int i = 0; i < INPUT_VECTOR_SIZE; i++) {
                                inputVector[i] = Double
                                        .valueOf(doublesAsString[i]);
                            }
                        }

                        // calculate output vector
                        double[] outputVector = IntStream
                                .range(0, OUTPUT_VECTOR_SIZE)
                                .mapToDouble(i -> algo2Score
                                        .getOrDefault(algorithms[i], -1d))
                                .toArray();

                        return new DataSetRow(inputVector, outputVector);
                    }).forEach(r -> trainingData.addRow(r));

            trainingData.save(Arguments.getEvaluationFilename().get() + "-"
                    + e.toString() + ".tset");

            if (!trainingData.isEmpty()) {
                // do the training
                // construct the neuronal network
                final NeuralNetwork<?> net = new MultiLayerPerceptron(
                        INPUT_VECTOR_SIZE, Math.min(256, OUTPUT_VECTOR_SIZE),
                        Math.min(256, OUTPUT_VECTOR_SIZE), OUTPUT_VECTOR_SIZE);

                System.out.println("Constructing network (" + e.toString()
                        + "):" + INPUT_VECTOR_SIZE + ".." + OUTPUT_VECTOR_SIZE);

                // train the neuronal network
                net.learnInNewThread(trainingData);
                try {
                    Thread.sleep(Arguments.getMaxLearningTime() * 1000);
                    net.stopLearning();
                } catch (InterruptedException exc) {
                    net.stopLearning();
                }

                net.save(Arguments.getEvaluationFilename().get() + "-"
                        + e.toString() + ".nnet");
            } else {
                System.err.println("training data set is empty");
            }

        }

        System.out.println("Training done");
    }

    /**
     * Singleton class does not provide a constructor
     */
    private NeuronalNetworkTool() {

    }
}
