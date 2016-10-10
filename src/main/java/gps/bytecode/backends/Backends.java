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
package gps.bytecode.backends;

import java.util.ArrayList;

public class Backends {
    /**
     * Array of all backends that should try to solve a given problem
     */
    static Class<?>[] classes = { Z3StdoutBackend.class,
            Z3StdoutEvalBackend.class, Z3BitVectorJavaApiBackend.class,
            Z3JavaApiBackend.class, LeonBackend.class };

    /**
     * Returns an Iterable of instatiated Backends
     * 
     * The idea is that some backends may be unavailable because some native
     * components are missing or failed to load, in that case we dont have to
     * crash, we could just use a different Backend
     * 
     * @return
     */
    public static Iterable<IBytecodeBackend> getAvailableBackends() {
        ArrayList<IBytecodeBackend> backends = new ArrayList<>();

        for (Class<?> backendClass : classes) {
            try {
                Object backend = backendClass.newInstance();
                IBytecodeBackend be = (IBytecodeBackend) backend;

                if (be.isAvailable()) {
                    backends.add(be);
                }

            } catch (ClassCastException e) {
                throw new RuntimeException("Backend class "
                        + backendClass.getName() + " is not a BytecodeBackend");
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(
                        "Backend class " + backendClass.getName()
                                + " must have a public default Constructor");
            }
        }

        return backends;
    }
}
