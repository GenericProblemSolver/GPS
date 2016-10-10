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

import java.lang.annotation.Annotation;

import butt.tool.benchmark.IBenchmarkRunner;
import gps.ISolverModule;
import gps.common.AbstractAlgorithm;
import gps.games.algorithm.AbstractGameAlgorithm;
import gps.preprocessing.AbstractImplementer;
import gps.util.reflections.ReflectionsHelper;

/**
 * Tool for creating the reflections cache.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class ReflectionsCache {

    /**
     * Write the cache for the reflections helper.
     * 
     * @param args
     *            is ignored.
     */
    public static void main(String args[]) {
        ReflectionsHelper.clearCache();
        ReflectionsHelper.getSubTypesOfCached("gps", ISolverModule.class);
        ReflectionsHelper.getSubTypesOfCached("benchmarks.*",
                IBenchmarkRunner.class);
        ReflectionsHelper.getSubTypesOfCached("gps.annotations",
                Annotation.class);
        ReflectionsHelper.getSubTypesOfCached("gps.*", AbstractAlgorithm.class);
        ReflectionsHelper.getSubTypesOfCached("gps.*",
                AbstractGameAlgorithm.class);
        ReflectionsHelper.getSubTypesOfCached("gps.preprocessing.*",
                AbstractImplementer.class);
        ReflectionsHelper.writeCacheToFile();
        System.out.println("Done. Reflection Cache has been written.");
    }
}
