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
package gps.optimization.wrapper;

import java.util.List;

public interface IOptimizable {

    public boolean hasObjectiveFunction();

    public double objectiveFunction(final Object[] params);

    public Object[] getDefaultParams();

    public List<Object[]> neighbor(final Object[] params);

    public boolean hasNeighborFunction();

    public void setThresholdForObjectiveFunction(final double pThresh);

    public boolean canBeGreaterThan();

    public void setMaximize(final byte pMax);

}
