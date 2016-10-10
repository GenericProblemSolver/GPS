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
package optimization.clustering;

import java.util.ArrayList;
import java.util.List;

import gps.annotations.Optimize;
import gps.annotations.Variable;

/**
 * Implementation of the clustering problem.
 * 
 * @see https://en.wikipedia.org/wiki/K-means_clustering
 * 
 * @author oesterca, mburri
 *
 */
public class ClusteringProblem {

    /** 
     * The points that this cluster consists of
     */
    List<Point> data;

    @Variable
    public Point c1 = new Point(0, 0);

    @Variable
    public Point c2 = new Point(0, 0);

    /**
     * Creates a new cluster with the given data
     * 
     * @param pData 
     *              the points in the cluster
     */
    public ClusteringProblem(final List<Point> pData) {
        if (pData == null) {
            throw new IllegalArgumentException(
                    "List of data points must not be null");
        }
        data = pData;
    }

    /**
     * Creates a new random cluster with the given amount of data points. 
     * 
     * @param numberOfDataPoints
     * 				the number of points in the cluster
     */
    public ClusteringProblem(final int numberOfDataPoints) {
        data = new ArrayList<Point>();
        for (int i = 0; i < numberOfDataPoints; i++) {
            data.add(new Point(numberOfDataPoints));
        }
    }

    /**
     * Returns the sum of the distances of each data point to the closest
     * clusterCenter.
     * 
     * @param clusterCenters
     *              the list of points that are to be evaluated
     * @return  
     *              the sum of the distances of each data point to the closest center
     */
    public double evalClusterCenters(final List<Point> clusterCenters) {
        double sumOfDistances = 0;
        for (final Point p : data) {
            double minDist = Double.MAX_VALUE;
            for (final Point clusterCenter : clusterCenters) {
                if (Point.euclideanDistance(p, clusterCenter) < minDist) {
                    minDist = Point.euclideanDistance(p, clusterCenter);
                }
            }
            sumOfDistances += minDist;
        }
        return sumOfDistances;
    }

    /**
     * Returns the sum of the distances of each data point to the closest center.
     * (for two centers)
     * 
     * @param p1    
     *          first clusterCenter
     * @param p2
     *          second clusterCenter
     * @return
     *          the sum of the distances of each data point to the closest center
     */
    @Optimize
    public double evalTwoClusterCenters(final Point p1, final Point p2) {
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException(
                    "The points that are to be evaluated must not be null");
        }
        final ArrayList<Point> clusterCenters = new ArrayList<>();
        clusterCenters.add(p1);
        clusterCenters.add(p2);
        final double result = evalClusterCenters(clusterCenters);
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(data.size()) + " data points.";
    }

}
