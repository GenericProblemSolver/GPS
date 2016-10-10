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
package butt.tool.cachetool;

import java.io.File;
import java.io.IOException;

import butt.tool.benchmark.common.Benchmarks;
import butt.tool.benchmark.common.TimeInterval;
import gps.games.GamesModule;
import gps.util.AnalysisCache;

/**
 * Tool for creating the analyser cache.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class AnalyserCache {

    /**
     * Write the cache for the analysis results.
     * 
     * @param args
     *            is ignored.
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        // allow user to pass the filename as argument.
        if (args.length > 0) {
            File f = new File(args[0]);
            f.createNewFile();
            if (f.canWrite()) {
                AnalysisCache.setWriteFilename(args[0]);
            } else {
                System.err.println("Cannot write to file " + args[0]);
                return;
            }
        }
        // create an empty cache
        // uncomment if you want to start from sratch.
        // TODO add some command line switch
        // AnalysisCache.instantiateNewCache();
        System.out.println("Creating analysis cache");

        // collect some statistics
        final long counter = Benchmarks.benchmarksGamesStream().count();
        final TimeInterval ti = new TimeInterval();
        final long progress[] = new long[] { 0 };
        ti.start();

        // run the analysis results (use parallel stream to parallelize)
        Benchmarks.benchmarksGamesStream().map(g -> new GamesModule<>(g))
                .sequential().forEach(gmod -> {
                    // run the analysis
                    gmod.gameAnalysis();
                    long i = ++progress[0];
                    ti.end();
                    System.out.println("#" + i + "/" + counter + " "
                            + (i * 100 / counter) + "% (eta:"
                            + TimeInterval.string(
                                    Math.abs(ti.deltaNanos() - Math.round(
                                            (((double) counter) / ((double) i))
                                                    * (double) (ti.deltaNanos()
                                                            / i * counter))))
                            + ")");
                    // also write partial cache to file
                    AnalysisCache.writeCacheToFile();
                });

        // write cache to file
        AnalysisCache.writeCacheToFile();
        System.out.println("Done. Analysis Cache has been written.");
    }

    /**
     * Singleton class does not provide a constructor.
     */
    private AnalyserCache() {

    }
}
