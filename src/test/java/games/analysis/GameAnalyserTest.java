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
package games.analysis;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

import game.connect4.ConnectGame;
import game.hanoi.Hanoi;
import gps.GPS;
import gps.IWrappedProblem;
import gps.games.GamesModule;
import gps.games.algorithm.analysis.GameAnalyser;
import gps.games.algorithm.analysis.IGameAnalysisResult;
import gps.games.wrapper.Game;

public class GameAnalyserTest {

    /*
     * Insert this in test to manually inspect values System.out.println(
     * "Avg Branching Factor: " + gra.getAvgBranchingFactor());
     * System.out.println("Avg Depth: " + gra.getAvgDepth());
     * System.out.println("AvgTime: " + gra.getAvgTime()); System.out.println(
     * "AvgNodeCount: " + gra.getCompletedNodeCount()); System.out.println(
     * "Player Number: " + gra.getPlayerNumber()); System.out.println(
     * "Termination Rate: " + gra.getTerminationRate()); System.out.println(
     * "Depth Range: " + gra.getDepthRange().getX() + ", " +
     * gra.getDepthRange().getY()); System.out .println("Utility Range: " +
     * (gra.getUtilityRange().isPresent() ? gra
     * .getUtilityRange().get().getX().doubleValue() : Double.NaN) + ", " +
     * (gra.getUtilityRange().isPresent() ? gra
     * .getUtilityRange().get().getY().doubleValue() : Double.NaN));
     */

    @Test
    public void simpleTest() {
        GPS<Hanoi> gps = new GPS<>(new Hanoi(3));
        IGameAnalysisResult gra = gps.gameAnalysis().get();

        if (gra.getAvgBranchingFactor() <= 0) {
            fail("Branching factor not set or broken");
        }
        if (gra.getAvgDepth() <= 0) {
            fail("Depth either not set or broken");
        }
        if (gra.getPlayerNumber().isPresent()) {
            fail("There should be no players in Hanoi");
        }
        if (!gra.getUtilityRange().equals(Optional.empty())) {
            fail("There should be no utility method for Hanoi");
        }

        // Testing / checking Hanoi-specific values
        if (gra.getAvgBranchingFactor() >= 6
                || gra.getAvgBranchingFactor() <= 3) {
            fail("Analysis result contained unexpected avg branching factor values for Hanoi");
        }
        if (gra.getAvgDepth() >= 1000 || gra.getAvgDepth() <= 0) {
            fail("Analysis result contained unexpected avg depth values for Hanoi");
        }
        if (gra.getTerminationRate() > 1 || gra.getTerminationRate() < 0) {
            fail("Analysis result contained unexpected termination rate");
        }
    }

    @Test
    public void multiPlayerAnalysisTest() {
        GPS<ConnectGame> gps = new GPS<>(ConnectGame.createConnect6());
        IGameAnalysisResult gra = gps.gameAnalysis().get();

        // ConnectGame specific values
        if (gra.getUtilityRange().equals(Optional.empty())) {
            fail("No utility was set");
        }
        if (gra.getPlayerNumber().get() != 2) {
            fail("There should be 2 players in connect game");
        }
        if (gra.getUtilityRange().get().getX().doubleValue() < -100000
                || gra.getUtilityRange().get().getY().doubleValue() > 100000) {
            fail("Unexpected utility values were returned");
        }
        if (gra.getAvgBranchingFactor() < 5
                || gra.getAvgBranchingFactor() > 50) {
            fail("Analysis result contained unexpected avg branching factor");
        }
        if (gra.getAvgDepth() < 0 || gra.getAvgDepth() > 1000) {
            fail("Analysis result contained unexpected avg depth");
        }
        if (gra.getTerminationRate() < 0.0 || gra.getTerminationRate() > 1) {
            fail("Analysis result contained unexpected termination rate");
        }
    }

    @Ignore
    @Test
    public void interruptTest() {
        IWrappedProblem<ConnectGame> problemC = GPS
                .wrap(ConnectGame.createConnect6());
        final GameAnalyser<ConnectGame> gaC = new GameAnalyser<ConnectGame>(
                new GamesModule<>(new Game<ConnectGame>(problemC)));
        gaC.analyze();

        IWrappedProblem<ConnectGame> problem = GPS
                .wrap(ConnectGame.createConnect6());
        final GameAnalyser<ConnectGame> ga = new GameAnalyser<ConnectGame>(
                new GamesModule<>(new Game<ConnectGame>(problem)));

        final IGameAnalysisResult[] gara = new IGameAnalysisResult[1]; // final array to interact with thread
        Thread thread = new Thread() {
            @Override
            public void run() {
                gara[0] = ga.analyze();
            }
        };
        thread.start();
        try {
            Thread.sleep(100); // wait for initialisation, some work done
        } catch (InterruptedException e) {
        }
        thread.interrupt();
        try {
            thread.join(); // wait for joining. Should be faster
        } catch (InterruptedException e) {
        }

        /*
         * For some reason the thread.interrupt() is not always executet
         * in time (when the threads are analysing the problem).
         * This only happens when building the project, thus it is probably linked to
         * the tests running on a thread each, resulting in the analysis starting,
         * but the test thread not getting the cpu soon enough to actually interrupt it.
         * 
         * -> TestIgnore
         */
        if (!ga.isShutDown()) {
            fail("The interrupt did not speed up analysis");
        }
    }

    @Test
    public void comparablyRandom() {
        GPS<ConnectGame> gps1 = new GPS<>(ConnectGame.createConnect6());
        IGameAnalysisResult gra1 = gps1.gameAnalysis().get();

        GPS<ConnectGame> gps2 = new GPS<>(ConnectGame.createConnect6());
        IGameAnalysisResult gra2 = gps2.gameAnalysis().get();

        if (!(gra1.getAvgBranchingFactor()
                .equals(gra2.getAvgBranchingFactor()))) {
            fail("Result average branching factor of two times the same problem, should be the same");
        }
        if (!(gra1.getAvgDepth().equals(gra2.getAvgDepth()))) {
            fail("Result average depth of two times the same problem, should be the same");
        }

        if (!(gra1.getAvgTime().equals(gra2.getAvgTime()))) {

        }

        if (!(gra1.getCompletedNodeCount()
                .equals(gra2.getCompletedNodeCount()))) {
            fail("Result complete node count of two times the same problem, should be the same");
        }

        if (!(gra1.getDepthRange().equals(gra2.getDepthRange()))) {
            fail("Result depth range of two times the same problem, should be the same");
        }

        // Note: The memory per node is not tested since there will be slight differences by design

        if (!(gra1.getPlayerNumber().equals(gra2.getPlayerNumber()))) {
            fail("Result player number of two times the same problem, should be the same");
        }

        if (!(gra1.getTerminationRate().equals(gra2.getTerminationRate()))) {
            fail("Result termination rate of two times the same problem, should be the same");
        }

        if (!(gra1.getUtilityRange().equals(gra2.getUtilityRange()))) {
            fail("Result utility range of two times the same problem, should be the same");
        }
    }

    /**
     * Constant must match actual classification vector.
     */
    @Test
    public void testVectorSize() {
        GPS<Hanoi> gps = new GPS<>(new Hanoi(3));
        IGameAnalysisResult gra = gps.gameAnalysis().get();
        assertEquals(IGameAnalysisResult.VECTOR_SIZE,
                gra.getClassificationVector().length);
    }

}
