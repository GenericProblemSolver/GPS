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
package butt.tool.benchmark.common;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * A process queue for organizing processes.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class ProcessQueue {

    private ProcessQueue(int elements) {
    }

    /**
     * Schedule processes.
     * 
     * @param elements
     *            The maximum number of processes that run simultaneously.
     * @param pProducer
     *            The process producer. First argument is the slot id that the
     *            process gets assigned to. Second argument is the element
     *            number of the element to produce. First produced process gets
     *            element number 0, second process 1 etc.
     * @param pConsumer
     *            Consumes a process. Gets called whenever a process finished.
     *            Gets also called when an interrupted exception occurred.
     * @throws InterruptedException
     */
    public static void schedule(int elements,
            BiFunction<Integer, Integer, Process> pProducer,
            Consumer<Process> pConsumer) throws InterruptedException {
        Process list[] = new Process[elements];
        boolean shutdown = false;
        int element = 0;
        int contains = 0;

        // fill empty slots
        for (int i = 0; contains < list.length; i++) {
            Process p = pProducer.apply(i, element++);
            if (p != null) {
                list[i] = p;
                contains++;
            } else {
                shutdown = true;
                break;
            }
        }

        // dispatch finished and refill empty slot with a new process
        try {
            for (int i = 0; contains > 0; i = (i + 1) % list.length) {
                final Process p = list[i];
                if (p != null && p.waitFor(100, TimeUnit.MILLISECONDS)) {
                    list[i] = null;
                    contains--;
                    Process n = shutdown ? null : pProducer.apply(i, element++);
                    if (n != null) {
                        list[i] = n;
                        contains++;
                    }
                    pConsumer.accept(p);
                }
            }
        } catch (final InterruptedException e) {
            for (int i = 0; i < list.length; i++) {
                if (list[i] != null) {
                    pConsumer.accept(list[i]);
                }
            }
            throw e;
        }
    }
}
