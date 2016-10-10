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
package optimization.benchmarks.gui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import optimization.benchmarks.BenchmarkResult;
import optimization.clustering.ClusteringProblem;
import optimization.layouting.Layouting;
import optimization.pp.PlaygroundProblem;
import optimization.tsp.TSP;

/**
 * JFrame that visualizes the optimization benchmarks.
 * 
 * @author mburri
 *
 */
@SuppressWarnings("serial")
public class OptBenchmarkVisualization extends JFrame {

    /**
     * The benchmarks that are to be visualized
     */
    List<BenchmarkResult<?>> benchmarks;

    /**
     * The column names of the table that is used for the visualization
     */
    private final static String[] COLUMN_NAMES = new String[] { "Problem",
            "Solver name", "Parameters", "Steps", "Time",
            "Value of found solution" };

    /**
     * Creates a new instance for the given list of BenchmarkResults
     * 
     * @param pB
     * 			the BenchmarkResults that are to be visualized
     */
    public OptBenchmarkVisualization(List<BenchmarkResult<?>> pB) {
        benchmarks = pB;
        this.setSize(1300, 800);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        addMainPanel();
    }

    /**
     * Adds the main content to the JFrame.
     */
    public void addMainPanel() {
        JPanel p = new JPanel(new GridLayout(1, 1));
        JTabbedPane tabPane = new JTabbedPane();

        for (JTable t : getTablesForProblems()) {
            tabPane.addTab(t.getName(), new JScrollPane(t));
        }
        p.add(tabPane);

        this.add(p);
    }

    /**
     * Gets corresponding tables for all specified problem classes.
     * 
     * @return
     * 		the tables to be displayed 
     */
    public List<JTable> getTablesForProblems() {
        List<JTable> ret = new ArrayList<JTable>();
        // TSP
        ret.add(getTableForClass(TSP.class, false));

        // Clustering
        ret.add(getTableForClass(ClusteringProblem.class, true));

        // PlaygroundProblem
        ret.add(getTableForClass(PlaygroundProblem.class, true));

        // Layouting
        ret.add(getTableForClass(Layouting.class, false));

        return ret;
    }

    /**
     * Utility method that gets the table for the given problem class.
     * 
     * @param clazz
     * 			the problem class
     * @param pso
     * 			whether or not the {@link gps.optimization.algorithm.particleSwarmOpt.ParticleSwarm}
     * 			solver can be used to solve the problem (for rendering purposes)
     * @return
     * 			the table for the given problem class
     */
    public JTable getTableForClass(final Class<?> clazz, final boolean pso) {
        List<String[]> content = new ArrayList<String[]>();
        for (BenchmarkResult<?> b : benchmarks) {
            if (b.getProblem().getClass().equals(clazz)) {
                if (!b.hasAnalyzerBenchmark()) {
                    String[] bmStringRow = new String[] {
                            b.getProblem().toString(),
                            b.getOptimizer().getClass().getSimpleName(),
                            b.getOptimizer().toString(),
                            String.valueOf(b.getUsedSteps()),
                            String.valueOf(b.getUsedTime()), String.valueOf(
                                    b.getSolution().getBestSolutionEval()) };
                    content.add(bmStringRow);
                } else {
                    String[] bmStringRow = new String[] {
                            b.getProblem().toString(), "Analyzer",
                            "<html>Scale: "
                                    + b.getAnalyzerInstance()
                                            .getObjectiveFunctionScale()
                                    + "<br>Expected: "
                                    + b.getAnalyzerB()
                                            .getExpectedObjectiveFunctionScaleMin()
                                    + " - "
                                    + b.getAnalyzerB()
                                            .getExpectedObjectiveFunctionScaleMax()
                                    + "</html>",
                            "<html>" + String.valueOf(b.getAnalyzerInstance()
                                    .getOptimalStepWidth()) + "<br>Expected: "
                                    + b.getAnalyzerB().getExpectedStepWidthMin()
                                    + " - "
                                    + b.getAnalyzerB().getExpectedStepWidthMax()
                                    + "</html>",
                            String.valueOf(b.getUsedTime()), String.valueOf(
                                    b.getSolution().getBestSolutionEval()) };
                    content.add(bmStringRow);
                }
            }
        }
        JTable table = new JTable(content.toArray(new Object[content.size()][]),
                COLUMN_NAMES);
        table.setName(clazz.getSimpleName());
        formatTable(table, pso);
        return table;
    }

    /**
     * Formats the given table.
     * 
     * @param table
     * 			the table to be formatted
     * @param pso
     * 			{@code true} if PSO can be used, {@code false} otherwise
     */
    public void formatTable(final JTable table, final boolean pso) {
        table.setEnabled(false);
        table.setRowHeight(30);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(45);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(20);
        table.getColumnModel().getColumn(4).setPreferredWidth(20);
        table.getColumnModel().getColumn(5).setPreferredWidth(50);
    }

}
