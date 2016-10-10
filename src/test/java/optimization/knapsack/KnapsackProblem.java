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
package optimization.knapsack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import gps.annotations.Optimize;
import gps.annotations.Variable;

/**
 * Implementation of the knapsack problem.
 *
 * @see https://en.wikipedia.org/wiki/Knapsack_problem
 * @author Sotisi
 */
public class KnapsackProblem {

    /**
     * Represents a single Item of the knapsack problem with a value and a
     * weight.
     *
     * @author Sotisi
     */
    private class Item {
        /**
         * Value of the Item
         */
        private final int value;
        /**
         * Weight of the Item
         */
        private final int weight;

        /**
         * Creates a new Item with given weight and value.
         *
         * @param pWeight
         *            weight of the Item
         * @param pValue
         *            value of the Item
         */
        public Item(final int pWeight, final int pValue) {
            weight = pWeight;
            value = pValue;
        }

        /**
         * Returns the value of the item
         *
         * @return the value of the item
         */
        public int getValue() {
            return value;
        }

        /**
         * Returns the weight of the item
         *
         * @return the weight of the item
         */
        public int getWeight() {
            return weight;
        }
    }

    /**
     * Default value for the number of Items in the knapsack
     */
    public static final int DEFAULTITEMAMOUNT = 25;
    /**
     * Default value for the maximum Item value of a single item
     */
    public static final int DEFAULTMAXIMUMITEMVALUE = 20;
    /**
     * Default value for the maximum Item weight of a single item
     */
    public static final int DEFAULTMAXIMUMITEMWEIGHT = 20;
    /**
     * Default value for the weight capacity of the knapsack
     */
    public static final int DEFAULTMAXWEIGHT = 100;
    /**
     * Default value for the minimum Item value of a single item
     */
    public static final int DEFAULTMINIMUMITEMVALUE = 1;
    /**
     * Default value for the minimum Item weight of a single item
     */
    public static final int DEFAULTMINIMUMITEMWEIGHT = 1;

    @Variable
    public boolean[] bA = new boolean[KnapsackProblem.DEFAULTITEMAMOUNT];

    /**
     * List of all possible Items to "put" in the knapsack
     */
    private final List<Item> items;

    /**
     * Maximum weight capacity of the knapsack
     */
    private final int maximumWeight;

    /**
     * Creates a new KnapsackProblem with default parameters.
     */
    public KnapsackProblem() {
        this(KnapsackProblem.DEFAULTMAXWEIGHT,
                KnapsackProblem.DEFAULTITEMAMOUNT,
                KnapsackProblem.DEFAULTMINIMUMITEMWEIGHT,
                KnapsackProblem.DEFAULTMAXIMUMITEMWEIGHT,
                KnapsackProblem.DEFAULTMINIMUMITEMVALUE,
                KnapsackProblem.DEFAULTMAXIMUMITEMVALUE);
    }

    /**
     * Creates a new KnapsackProblem with given parameters
     *
     * @param pMaxWeight
     *            weight capacity of the knapsack
     * @param pItemAmount
     *            number of Items
     * @param pMinimumItemWeight
     *            minimum weight of a single Item
     * @param pMaximumItemWeight
     *            maximum weight of a single Item
     * @param pMinimumItemValue
     *            minimum value of a single Item
     * @param pMaximumItemValue
     *            maximum value of a single Item
     */
    public KnapsackProblem(final int pMaxWeight, final int pItemAmount,
            final int pMinimumItemWeight, final int pMaximumItemWeight,
            final int pMinimumItemValue, final int pMaximumItemValue) {
        items = new ArrayList<>();
        maximumWeight = pMaxWeight;
        Random random = new Random();
        for (int x = 0; x < pItemAmount; x++) {
            int weight = random
                    .nextInt(pMaximumItemWeight - pMinimumItemWeight + 1)
                    + pMinimumItemWeight;
            int value = random
                    .nextInt(pMaximumItemValue - pMinimumItemValue + 1)
                    + pMinimumItemValue;
            items.add(new Item(weight, value));
        }
    }

    /**
     * Calculates the "score" of the given solution for this KnapsackProblem.
     *
     * @param solution
     *            boolean array representing which Items are selected
     * @return the combined value of all selected Items, or 0 if weight limit is
     *         exceeded
     */
    @Optimize
    public int fitness(final boolean[] solution) {
        int currentWeight = 0;
        int currentValue = 0;
        for (int x = 0; x < solution.length; x++) {
            if (solution[x]) {
                Item item = items.get(x);
                currentWeight += item.getWeight();
                if (currentWeight > maximumWeight) {
                    return 0;
                }
                currentValue += item.getValue();
            }
        }
        return currentValue;
    }

    /**
     * Returns a "decent" result of this KnapsackProblem in the form of a
     * possible total value.
     *
     * @return a "decent" result of this KnapsackProblem in the form of a
     *         possible total value.
     */
    public int referenceSolution() {
        int result = 0;
        List<Item> efficiencySortedItems = new ArrayList<>(items);
        Collections.sort(efficiencySortedItems, new Comparator<Item>() {
            @Override
            public int compare(final Item o1, final Item o2) {
                double o1Efficiency = ((double) o1.getValue())
                        / ((double) o1.getWeight());
                double o2Efficiency = ((double) o2.getValue())
                        / ((double) o2.getWeight());
                return o1Efficiency > o2Efficiency ? -1
                        : o1Efficiency != o2Efficiency ? 1
                                : o2.weight - o1.weight;
            }
        });
        int weight = 0;
        for (Item i : efficiencySortedItems) {
            if (weight + i.getWeight() <= maximumWeight) {
                weight += i.getWeight();
                result += i.getValue();
            }
        }
        return result;
    }
}