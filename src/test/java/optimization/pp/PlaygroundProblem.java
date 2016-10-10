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
package optimization.pp;

import gps.annotations.Optimize;
import gps.annotations.Variable;

/**
 * Implementation of the PlaygroundProblem:
 * <br>
 * max((x,y) -> x*y) with 2x + y < z, z = 24 (per default)
 * 
 * @author mburri
 */
public class PlaygroundProblem {

    /**
     * available meters of fence
     */
    private final int maxFence;

    @Variable
    public int height = 1;

    @Variable
    public int width = 1;

    public PlaygroundProblem(final int i) {
        maxFence = i;
    }

    public PlaygroundProblem() {
        this(24);
    }

    /**
     * Returns the size of the fenced area defined by the Fence object
     * 
     * @param f the Fence object that the size of the area is calculated for
     * @return  the size of the area
     */
    @Optimize
    public double evalFence(final int height, final int width) {
        if (height < 0 || width < 0 || height > maxFence || width > maxFence
                || height * 2 + width > maxFence) {
            return 0;
        }
        if (height < 0 || width < 0) {
            return 0;
        }
        return height * width;
    }

    @Override
    public String toString() {
        return "Max. available fence: " + maxFence;
    }

}