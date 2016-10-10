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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class AssignmentProblemTest {

    private AssignmentProblem ap;

    @Before
    public void init() {
        List<Job> jobList = new ArrayList<>();
        List<Worker> workerList = new ArrayList<>();
        jobList.add(new Job(0.1));
        workerList.add(new Worker(0.1));
        jobList.add(new Job(1.1));
        workerList.add(new Worker(1.1));
        jobList.add(new Job(2.2));
        workerList.add(new Worker(2.1));
        jobList.add(new Job(3.3));
        workerList.add(new Worker(0.1));
        jobList.add(new Job(5.1));
        workerList.add(new Worker(18.1));
        jobList.add(new Job(12));
        workerList.add(new Worker(0.1));
        jobList.add(new Job(0.001));
        workerList.add(new Worker(20.1));
        jobList.add(new Job(0.1));
        workerList.add(new Worker(4.1));
        jobList.add(new Job(0.9));
        workerList.add(new Worker(12.1));
        jobList.add(new Job(56.1));
        workerList.add(new Worker(0.5));
        jobList.add(new Job(3.2));
        workerList.add(new Worker(23.1));
        jobList.add(new Job(4.4));
        workerList.add(new Worker(53.3));
        jobList.add(new Job(4.34));
        workerList.add(new Worker(23.5));
        jobList.add(new Job(53));
        workerList.add(new Worker(0.1));
        ap = new AssignmentProblem(workerList, jobList);
    }

    @Test
    public void getCostsTest() {
        Map<Worker, Job> a = ap.workerJobAssignment;
        assertTrue(ap.getCosts(a) == 554.7801);
    }

    @Test
    public void changeRandomTest() {
        Map<Worker, Job> a = ap.workerJobAssignment;
        assertTrue(ap.getCosts(a) != ap
                .getCosts((ap.changeRandom(ap.workerJobAssignment))));
    }

}
