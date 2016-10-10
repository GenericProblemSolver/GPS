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

/**
 * A square sized module
 * 
 * @author bargen
 *
 */
public class Module {

    /**
     * The name of the module
     */
    private String name;
    /**
     * One side of the module
     */
    private int x;
    /**
     * One side of the module
     */
    private int y;
    /**
     * true if the module got rotated
     */
    private boolean isRotated;
    /**
     * The backup name
     */
    private String cleanName;
    /**
     * Backup side
     */
    private int cleanX;
    /**
     * Backup side
     */
    private int cleanY;

    /**
     * The Constructor of a module
     * 
     * @param pName
     *            the name of the module
     * @param pX
     *            the x side of the module
     * @param pY
     *            the y of the module
     */
    public Module(final String pName, final int pX, final int pY) {
        if (pName.contains("*") || pName.contains("+")) {
            throw new IllegalArgumentException(
                    "The name cant have a * oder a + in it");
        }
        if (pX < 1 || pY < 1) {
            throw new IllegalArgumentException(
                    "The sides should not be smaller than 0");
        }
        name = pName;
        x = pX;
        y = pY;
        isRotated = false;
        cleanName = name;
        cleanX = pX;
        cleanY = pY;

    }

    /**
     * Returns a new module with the first values the module got
     * 
     * @return a new module
     */
    public Module getCleanModule() {
        return new Module(cleanName, cleanX, cleanY);
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(final int pX) {
        x = pX;
    }

    public void setY(final int pY) {
        y = pY;
    }

    public boolean isRotated() {
        return isRotated;
    }

    /**
     * Switches the sides of the module
     */
    public void rotate() {
        int backup = x;
        x = y;
        y = backup;
        isRotated = !isRotated;
    }

}
