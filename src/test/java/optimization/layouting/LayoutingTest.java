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
package optimization.layouting;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

public class LayoutingTest {

    private Layouting layouting;

    private List<Module> modules;

    @Before
    public void init() {
    }

    @Test
    public void alternativConstructorTest() {
        Layouting l = new Layouting();
        l.getCosts(l.layout);
        String lay = l.swapRandom(l.layout);
        for (int i = 0; i < 100; i++) {
            lay = l.swapRandom(lay);
            l.getCosts(lay);
        }
    }

    @Test
    public void getCostsTest() {
        modules = new ArrayList<Module>();
        modules.add(new Module("ALU", 1, 1));
        layouting = new Layouting(modules);
        assertTrue(layouting.getCosts(layouting.layout) == 1);
        modules = new ArrayList<Module>();
        modules.add(new Module("ALU", 2, 1));
        layouting = new Layouting(modules);
        assertTrue(layouting.getCosts(layouting.layout) == 2);
        modules = new ArrayList<Module>();
        modules.add(new Module("ALU", 1, 1));
        modules.add(new Module("ALU1", 1, 1));
        layouting = new Layouting(modules);
        assertTrue(layouting.getCosts(",-+") == 2);

    }

    @Ignore
    @Test
    public void swapRandomTest() {
        modules = new ArrayList<Module>();
        modules.add(new Module("ALU", 1, 1));
        modules.add(new Module("ALU1", 1, 1));
        modules.add(new Module("ALU2", 1, 1));
        modules.add(new Module("ALU3", 1, 1));
        layouting = new Layouting(modules);
        String lay = layouting.layout;
        assertTrue(!layouting.swapRandom(lay).equals(layouting.layout));
    }

}
