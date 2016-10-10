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
package optimization.assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import gps.annotations.Neighbor;
import gps.annotations.Optimize;
import gps.annotations.Variable;

/**
 * The assigment problem gets two equal sized lists and every element in the
 * list has a cost factor.
 * 
 * @author bargen
 *
 */
public class AssignmentProblem {

    /**
     * The list of the workers.
     */
    private List<Worker> workerList;

    /**
     * The list of the job.
     */
    private List<Job> jobList;

    /**
     * Every entry in this map represents an assigned job
     */
    @Variable
    public Map<Worker, Job> workerJobAssignment;

    /**
     * Constructor for the assignment problem
     * @param pWorkerList The list of the workers
     * @param pJobList The list of the jobs
     */
    public AssignmentProblem(final List<Worker> pWorkerList,
            final List<Job> pJobList) {
        if (pWorkerList == null || pJobList == null) {
            throw new IllegalArgumentException(
                    "The worker list nor the job list should not be null.");
        }
        if (pWorkerList.isEmpty() || pJobList.isEmpty()
                || pJobList.size() != pWorkerList.size()) {
            throw new IllegalArgumentException(
                    "The worker list nor the job list should not be empty and both lists need to have the same size.");
        }
        workerList = pWorkerList;
        jobList = pJobList;
        workerJobAssignment = createFirstInstance();

    }

    /**
     * Alternative constructor for the assignment problem
     */
    public AssignmentProblem() {
        List<Job> pJobList = new ArrayList<>();
        List<Worker> pWorkerList = new ArrayList<>();
        jobList.add(new Job(0.1));
        workerList.add(new Worker(0.1));
        jobList.add(new Job(100.1));
        workerList.add(new Worker(1.1));
        jobList.add(new Job(2.1));
        workerList.add(new Worker(2.1));
        jobList.add(new Job(3.3));
        workerList.add(new Worker(0.1));
        jobList.add(new Job(5.1));
        workerList.add(new Worker(18.1));
        workerList = pWorkerList;
        jobList = pJobList;
        workerJobAssignment = createFirstInstance();

    }

    /**
     * Returns the first assignment
     */
    private Map<Worker, Job> createFirstInstance() {
        Map<Worker, Job> returnMap = new HashMap<>();
        for (int i = 0; i < workerList.size(); i++) {
            returnMap.put(workerList.get(i), jobList.get(i));
        }
        return returnMap;
    }

    /**
     * Multiplies every cost factor from the job with the worker factor
     * @param pMap The assignment map
     * @return The total costs of the assignment
     */
    @Optimize
    public double getCosts(Map<Worker, Job> pMap) {
        if (pMap == null) {
            throw new IllegalArgumentException(
                    "The assign map should not be null.");
        }
        if (pMap.isEmpty()) {
            throw new IllegalArgumentException(
                    "The assign map should not be empty.");
        }
        double returnValue = 0;
        Map<Worker, Job> map = pMap;
        for (Worker w : workerList) {
            returnValue += map.get(w).getFactor() * w.getFactor();
        }
        return returnValue;

    }

    /**
     * Switches two random picked job with each other
     * @param pMap The assignment map
     * @return A changed map
     */
    @Neighbor
    public Map<Worker, Job> changeRandom(Map<Worker, Job> pMap) {
        if (pMap == null) {
            throw new IllegalArgumentException(
                    "The assign map should not be null.");
        }
        if (pMap.isEmpty()) {
            throw new IllegalArgumentException(
                    "The assign map should not be empty.");
        }
        Map<Worker, Job> map = pMap;
        Random rand = new Random();
        int fst = rand.nextInt(workerList.size());
        int sec = rand.nextInt(workerList.size());
        while (fst == sec) {
            sec = rand.nextInt(workerList.size());
        }
        map.replace(workerList.get(fst), jobList.get(sec));
        map.replace(workerList.get(sec), jobList.get(fst));
        return map;
    }

}
