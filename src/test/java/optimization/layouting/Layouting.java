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

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import gps.annotations.Neighbor;
import gps.annotations.Optimize;
import gps.annotations.Variable;

/**
 * The layouting problem get a list of modules and by using of the reverse
 * polish notation switches them around
 * 
 * @author bargen
 *
 */
public class Layouting {

    /**
     * The given modules
     */
    private HashMap<Character, Module> moduleMap;

    /**
     * A backup list for the modules because the moduleMap gets modified during
     * the cost calculation
     */
    private List<Module> moduleListBackup;

    /**
     * The layout defines how the modules are orientated given by a string in
     * the reverse polish notation
     */
    @Variable
    public String layout;

    /**
     * The constructor of the layouting problem. It gets a list of all modules
     * and puts them in a hashmap with an unique id.
     * 
     * @param pModules
     *            the list of the modules
     */
    public Layouting(final List<Module> pModules) {
        if (pModules == null) {
            throw new IllegalArgumentException();
        }
        moduleListBackup = pModules;
        char id = 44;
        moduleMap = new HashMap<Character, Module>();
        for (Module m : pModules) {
            moduleMap.put(id, m);
            id++;
        }
        layout = createFirstInstance();
    }

    /**
     * The alternativ constructor of the layouting problem with hardcoded
     * modules.
     */
    public Layouting() {
        List<Module> pModules = new ArrayList<>();
        pModules.add(new Module("1", 1, 1));
        pModules.add(new Module("2", 21, 41));
        pModules.add(new Module("3", 31, 41));
        pModules.add(new Module("4", 21, 61));
        pModules.add(new Module("5", 31, 81));
        pModules.add(new Module("6", 51, 11));
        pModules.add(new Module("7", 16, 1));
        pModules.add(new Module("8", 14, 1));
        pModules.add(new Module("9", 13, 4));
        pModules.add(new Module("10", 1, 4));
        pModules.add(new Module("11", 341, 6));
        pModules.add(new Module("12", 51, 1));
        pModules.add(new Module("13", 3, 1));
        pModules.add(new Module("14", 6, 77));
        pModules.add(new Module("15", 13, 123));
        pModules.add(new Module("16", 45, 13));
        pModules.add(new Module("17", 76, 17));
        pModules.add(new Module("18", 2, 17));
        pModules.add(new Module("19", 87, 133));
        pModules.add(new Module("20", 34, 1));
        pModules.add(new Module("21", 56, 2));
        pModules.add(new Module("22", 89, 3));
        pModules.add(new Module("23", 11, 4));
        pModules.add(new Module("24", 111, 5));
        pModules.add(new Module("25", 1, 6));
        moduleListBackup = pModules;
        char id = 44;
        moduleMap = new HashMap<Character, Module>();
        for (Module m : pModules) {
            moduleMap.put(id, m);
            id++;
        }
        layout = createFirstInstance();
    }

    /**
     * Creates a new module map with the old module sizes
     */
    private void create() {
        char id = 44;
        moduleMap = new HashMap<Character, Module>();
        for (Module m : moduleListBackup) {
            moduleMap.put(id, m.getCleanModule());
            id++;
        }

    }

    /**
     * Calculates the costs of the given layout string and the module map, by
     * converting it into an infix notation and adding the modules up. So if to
     * modules are connected with a * the x sides of the module get added up and
     * y will be set to the maximum of y. With the + the y sides will get add
     * together and x will be the maximum of the both x. This will run until all
     * modules are on one huge module
     * 
     * @param pPolishForm
     *            the given layout
     * @return the size of the given layout
     */
    @Optimize
    public int getCosts(final String pPolishForm) {
        int i = 0;
        String infixForm = convert(pPolishForm);
        while (true) {
            infixForm = clearString(infixForm);
            i++;
            if (i >= infixForm.length() - 1) {
                i = 0;
            }
            if (infixForm.length() <= 4) {
                break;
            }
            if (infixForm.charAt(i) == '*') {
                if (infixForm.charAt(i + 1) != '('
                        && infixForm.charAt(i - 1) != ')') {

                    StringBuilder st = new StringBuilder(infixForm);
                    st.setCharAt(i, infixForm.charAt(i + 1));
                    infixForm = st.toString();
                    moduleMap.get(infixForm.charAt(i))
                            .setX(moduleMap.get(infixForm.charAt(i - 1)).getX()
                                    + moduleMap.get(infixForm.charAt(i + 1))
                                            .getX());
                    moduleMap
                            .get(infixForm.charAt(i)).setY(
                                    Math.max(
                                            moduleMap
                                                    .get(infixForm
                                                            .charAt(i - 1))
                                                    .getY(),
                                            moduleMap
                                                    .get(infixForm
                                                            .charAt(i + 1))
                                                    .getY()));
                    st = new StringBuilder(infixForm);
                    st.deleteCharAt(i - 1);
                    infixForm = st.toString();
                    if (!(i + 1 >= infixForm.length())) {
                        st = new StringBuilder(infixForm);
                        st.deleteCharAt(i);
                        infixForm = st.toString();
                    }

                }
            }

            if (infixForm.charAt(i) == '+') {
                if (infixForm.charAt(i + 1) != '('
                        && infixForm.charAt(i - 1) != ')') {
                    StringBuilder st = new StringBuilder(infixForm);
                    st.setCharAt(i, infixForm.charAt(i + 1));
                    infixForm = st.toString();
                    moduleMap.get(infixForm.charAt(i))
                            .setY(moduleMap.get(infixForm.charAt(i - 1)).getY()
                                    + moduleMap.get(infixForm.charAt(i + 1))
                                            .getY());
                    moduleMap
                            .get(infixForm.charAt(i)).setX(
                                    Math.max(
                                            moduleMap
                                                    .get(infixForm
                                                            .charAt(i - 1))
                                                    .getX(),
                                            moduleMap
                                                    .get(infixForm
                                                            .charAt(i + 1))
                                                    .getX()));
                    st = new StringBuilder(infixForm);
                    st.deleteCharAt(i - 1);
                    infixForm = st.toString();
                    if (!(i + 1 >= infixForm.length())) {
                        st = new StringBuilder(infixForm);
                        st.deleteCharAt(i);
                        infixForm = st.toString();
                    }
                }
            }
        }
        if (infixForm.charAt(0) == '(') {
            int ret = moduleMap.get(infixForm.charAt(1)).getX()
                    * moduleMap.get(infixForm.charAt(1)).getY();
            clearModules();
            return ret;
        }
        int ret = moduleMap.get(infixForm.charAt(0)).getX()
                * moduleMap.get(infixForm.charAt(0)).getY();
        clearModules();
        return ret;

    }

    /**
     * To use the reverse polish notation you need special characters which
     * define the position of the modules.
     * 
     * @param pC
     *            the given char
     * @return true if the char is a * or a +
     */
    private boolean isSpecialChar(final char pC) {
        if (pC == '*' || pC == '+') {
            return true;
        }
        return false;
    }

    /**
     * Removes double braces out of the string
     * 
     * @param pString
     *            the given string
     * @return a cleaned up string
     */
    private String clearString(final String pString) {
        return pString.replaceAll("\\((.)\\)", "$1");
    }

    /**
     * Converts any postfix to infix
     * 
     * @param postfix
     *            String expression to be converted
     * @return String infix expression produced
     * 
     *         Source:
     *         http://javaingrab.blogspot.de/2014/07/postfix-to-infix-conversion
     *         -using-stack.html
     */
    private String convert(String postfix) {
        try {
            Stack<String> s = new Stack<>();

            for (int i = 0; i < postfix.length(); i++) {
                char c = postfix.charAt(i);
                if (isSpecialChar(c)) {
                    String b = s.pop();
                    String a = s.pop();
                    s.push("(" + a + c + b + ")");
                } else {
                    s.push("" + c);
                }
            }

            return s.pop();

        } catch (EmptyStackException e) {
            return convert(swapRandom(postfix));
        }
    }

    /**
     * Checks if the given reverse polish notation is valid
     * 
     * @param pPolishForm
     *            the given layout
     * @return true if it is valid
     */
    private boolean isValideNormal(final String pPolishForm) {
        char[] workingString = pPolishForm.toCharArray();
        char first = '(';
        for (int i = 0; i < workingString.length; i++) {
            if (isSpecialChar(workingString[i]) && first != '('
                    && first == workingString[i]) {
                return false;
            }
            first = workingString[i];
        }
        return true;

    }

    private void clearModules() {
        create();
    }

    /**
     * Switches two chars in a string with each other
     * 
     * @param pString
     *            the given string
     * @return A changed string
     */
    @Neighbor
    public String swapRandom(final String pString) {
        String workString = pString;
        Random rand = new Random();
        int firstRand = rand.nextInt(pString.length());
        int secondRand = rand.nextInt(pString.length());
        StringBuilder st = new StringBuilder(workString);
        char first = st.charAt(firstRand);
        char second = st.charAt(secondRand);
        st.setCharAt(firstRand, second);
        st.setCharAt(secondRand, first);
        workString = st.toString();
        if (!isValideNormal(workString)) {
            return M3Again(pString);
        }
        return workString;
    }

    /**
     * Creates the first layout instance by setting all the Ids behind each
     * other and after the Ids it puts the position markers for the modules
     * 
     * @return the first layout instance
     */
    private String createFirstInstance() {
        StringBuilder st = new StringBuilder();
        for (Character key : moduleMap.keySet()) {
            st.append(key);
        }
        String plusTimes = "";
        while (true) {
            if (plusTimes.length() == moduleMap.size() - 1) {
                break;
            }
            plusTimes += "+";

            if (plusTimes.length() == moduleMap.size() - 1) {
                break;
            }
            plusTimes += "*";
        }
        st.append(plusTimes);
        return st.toString();

    }

    /**
     * Calls the swap random function again
     */
    private String M3Again(final String pString) {
        return swapRandom(pString);
    }

    @Override
    public String toString() {
        return String.valueOf(moduleMap.size()) + " modules";
    }

}