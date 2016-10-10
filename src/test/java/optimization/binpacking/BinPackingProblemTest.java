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
package optimization.binpacking;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author bargen
 */
public class BinPackingProblemTest {

    private BinObject bo0, bo1, bo2, bo3, bo4, bo5, bo6, bo7, bo8, bo9;

    private BinPackingProblem bpp;

    private List<BinObject> boList0, boList1, boList2, boList3, boList4,
            boList5;

    private Bin b;

    @Before
    public void init() {
        boList0 = new ArrayList<>();
        boList1 = new ArrayList<>();
        boList2 = new ArrayList<>();
        boList3 = new ArrayList<>();
        boList4 = new ArrayList<>();
        boList5 = new ArrayList<>();
        bo0 = new BinObject(3);
        bo1 = new BinObject(3);
        bo2 = new BinObject(3);
        bo3 = new BinObject(1);
        bo4 = new BinObject(10);
        bo5 = new BinObject(10);
        bo6 = new BinObject(10);
        bo7 = new BinObject(10);
        bo8 = new BinObject(10);
        bo9 = new BinObject(10);
        boList0.add(bo0);
        boList0.add(bo1);
        boList0.add(bo2);
        boList0.add(bo3);
        boList0.add(bo4);
        boList0.add(bo5);
        boList0.add(bo6);
        boList0.add(bo7);
        boList0.add(bo8);
        boList0.add(bo9);

        boList1.add(bo4);
        boList1.add(bo5);
        boList1.add(bo6);
        boList1.add(bo7);
        boList1.add(bo8);
        boList1.add(bo9);

        boList2.add(bo0);
        boList2.add(bo1);
        boList2.add(bo2);
        boList2.add(bo3);

        boList3.add(bo0);
        boList3.add(bo1);
        boList3.add(bo2);
        boList3.add(bo3);
        boList3.add(bo4);

        boList4.add(bo3);
        boList4.add(bo4);

        boList5.add(bo0);
        boList5.add(bo1);
        boList5.add(bo2);
        boList5.add(bo3);
        boList5.add(bo4);
        boList5.add(bo5);

    }

    @Test
    public void alternativConstructorTest() {
        bpp = new BinPackingProblem();
        bpp.getCosts(bpp.binList);
        List<Bin> bl = bpp.randomSetBinObjects(bpp.binList);
        for (int i = 0; i < 100; i++) {
            bl = bpp.randomSetBinObjects(bl);
            bpp.getCosts(bl);
        }
    }

    @Test
    public void BinTest() {
        b = new Bin();
        assertTrue(b.getEmptySpaceSize() == BinPackingProblem.BINSIZE);
        b.addObject(bo0);
        assertTrue(b.getEmptySpaceSize() == BinPackingProblem.BINSIZE
                - bo0.getSize());
        b.addObject(bo9);
        assertTrue(b.getEmptySpaceSize() == BinPackingProblem.BINSIZE
                - bo0.getSize() - bo9.getSize());
        b.removeObject(bo0);
        assertTrue(b.getEmptySpaceSize() == BinPackingProblem.BINSIZE
                - bo9.getSize());
    }

    @Test
    public void getCostsTest() {
        bpp = new BinPackingProblem(boList0);
        assertTrue(bpp.getCosts(bpp.binList) == 10);
        bpp = new BinPackingProblem(boList1);
        assertTrue(bpp.getCosts(bpp.binList) == 6);
        bpp = new BinPackingProblem(boList2);
        assertTrue(bpp.getCosts(bpp.binList) == 4);
        bpp = new BinPackingProblem(boList3);
        assertTrue(bpp.getCosts(bpp.binList) == 5);
        bpp = new BinPackingProblem(boList4);
        assertTrue(bpp.getCosts(bpp.binList) == 2);
        bpp = new BinPackingProblem(boList5);
        assertTrue(bpp.getCosts(bpp.binList) == 6);

    }

    @Test
    public void randomSetBinObjectsTest() {
        bpp = new BinPackingProblem(boList0);
        assertTrue(bpp.getCosts(bpp.randomSetBinObjects(bpp.binList)) <= 10);
        bpp = new BinPackingProblem(boList1);
        assertTrue(bpp.getCosts(bpp.randomSetBinObjects(bpp.binList)) == 6);
        bpp = new BinPackingProblem(boList2);
        assertTrue(bpp.getCosts(bpp.randomSetBinObjects(bpp.binList)) <= 4);
        bpp = new BinPackingProblem(boList3);
        assertTrue(bpp.getCosts(bpp.randomSetBinObjects(bpp.binList)) <= 5);
        bpp = new BinPackingProblem(boList4);
        assertTrue(bpp.getCosts(bpp.randomSetBinObjects(bpp.binList)) == 2);
        bpp = new BinPackingProblem(boList5);
        assertTrue(bpp.getCosts(bpp.randomSetBinObjects(bpp.binList)) <= 6);
    }
}
