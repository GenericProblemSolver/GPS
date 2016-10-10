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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import gps.annotations.Constraint;
import gps.annotations.Neighbor;
import gps.annotations.Optimize;
import gps.annotations.Variable;

/**
 * The bin packing problem in which an finite amount of objects are put
 * inside a minimal amount of bins with a fixed size
 * 
 * @see https://en.wikipedia.org/wiki/Bin_packing_problem
 * 
 * @author bargen
 *
 */
public class BinPackingProblem {

    /**
     * The list of bins
     */
    @Variable
    public List<Bin> binList;

    /**
     * The list of the objects
     */
    private List<BinObject> binObjectList;

    /**
     * The size of every bin
     */
    public final static int BINSIZE = 10;

    /**
     * The constructor of the bin packing problem which also creates the first
     * instance.
     * 
     * @param pBinObjectList
     *            The list of the objects.
     */
    public BinPackingProblem(final List<BinObject> pBinObjectList) {
        if (pBinObjectList == null) {
            throw new IllegalArgumentException(
                    "The list of the bin objects should not be null.");
        }
        binObjectList = pBinObjectList;
        binList = createFirstInstance();

    }

    /**
     * The alternative constructor of the bin packing problem with hardcoded
     * BinObjects.
     */
    public BinPackingProblem() {
        List<BinObject> pBinObjectList = new ArrayList<>();
        pBinObjectList.add(new BinObject(1));
        pBinObjectList.add(new BinObject(2));
        pBinObjectList.add(new BinObject(3));
        pBinObjectList.add(new BinObject(4));
        pBinObjectList.add(new BinObject(5));
        pBinObjectList.add(new BinObject(6));
        pBinObjectList.add(new BinObject(7));
        pBinObjectList.add(new BinObject(8));
        pBinObjectList.add(new BinObject(9));
        pBinObjectList.add(new BinObject(10));
        pBinObjectList.add(new BinObject(1));
        pBinObjectList.add(new BinObject(2));
        pBinObjectList.add(new BinObject(3));
        pBinObjectList.add(new BinObject(4));
        pBinObjectList.add(new BinObject(5));
        pBinObjectList.add(new BinObject(6));
        pBinObjectList.add(new BinObject(7));
        pBinObjectList.add(new BinObject(8));
        pBinObjectList.add(new BinObject(9));
        pBinObjectList.add(new BinObject(10));
        pBinObjectList.add(new BinObject(1));
        pBinObjectList.add(new BinObject(2));
        pBinObjectList.add(new BinObject(3));
        pBinObjectList.add(new BinObject(4));
        pBinObjectList.add(new BinObject(5));
        pBinObjectList.add(new BinObject(6));
        pBinObjectList.add(new BinObject(7));
        pBinObjectList.add(new BinObject(8));
        pBinObjectList.add(new BinObject(9));
        pBinObjectList.add(new BinObject(10));
        binObjectList = pBinObjectList;
        binList = createFirstInstance();

    }

    public BinPackingProblem(int max) {
        binObjectList = new ArrayList<BinObject>();
        for (int i = 0; i < max; i++) {
            binObjectList.add(new BinObject(1));
        }
        binList = createFirstInstance();
    }

    /**
     * Method to create the first instance by putting every object in its own
     * bin
     * 
     * @return The first list of bins
     */
    private List<Bin> createFirstInstance() {
        List<Bin> returnList = new ArrayList<>();
        for (BinObject b : binObjectList) {
            Bin bin = new Bin();
            bin.addObject(b);
            returnList.add(bin);
        }
        return returnList;

    }

    /**
     * Returns the amount of bins.
     * 
     * @param pBinList
     *            The list of bins
     * @return The amount of bins
     */
    @Optimize
    public int getCosts(final List<Bin> pBinList) {
        if (!isValid(pBinList)) {
            return Integer.MAX_VALUE;
        }
        return pBinList.size();
    }

    /**
     * Puts a random objects inside bins until they are full. If the bin is full
     * a new one will get created.
     * 
     * @param pBinList
     *            The list of bins
     * @return An optimized list of the bins
     */
    @Neighbor
    public List<Bin> randomSetBinObjects(final List<Bin> pBinList) {
        Random rand = new Random();
        List<BinObject> usableBinObjectList = new ArrayList<BinObject>();
        usableBinObjectList.addAll(binObjectList);
        List<Bin> returnList = new ArrayList<>();
        boolean isFull = false;
        while (usableBinObjectList.size() > 0) {
            Bin b = new Bin();
            isFull = false;
            while (!isFull) {
                int objectPoint = rand.nextInt(usableBinObjectList.size()
                        + (usableBinObjectList.size() <= 0 ? 1 : 0));
                BinObject bo = usableBinObjectList.get(objectPoint);
                b.addObject(bo);
                if (b.isOverFull()) {
                    isFull = true;
                    b.removeObject(bo);
                } else {
                    usableBinObjectList.remove(objectPoint);
                }
                if (usableBinObjectList.size() == 0) {
                    break;
                }
            }
            returnList.add(b);
        }
        return returnList;
    }

    /**
     * Checks if every bin in the list is not over full.
     * 
     * @param pBinList
     *            The list of the bins
     * @return true if no bin is over full
     */
    @Constraint
    public boolean isValid(final List<Bin> pBinList) {
        for (Bin b : pBinList) {
            if (b.isOverFull()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(binObjectList.size()) + " items";
    }

}
