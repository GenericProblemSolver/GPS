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
package games.go;

import java.util.ArrayList;
import java.util.List;
import games.go.Go;

/**
 * More convenient way to address coordinates
 */
public final class Coords {

    /**
     * Stores our Logger
     */
    // private static final Logger LOGGER = Logger.getAnonymousLogger();

    /**
     * Stores the x coordinate
     */
    public int x;

    /**
     * Stores the y coordinate
     */
    public int y;

    /**
     * Creates a new coordinate tuple
     * 
     * @param pX
     *            The x coordinate to be set
     * @param pY
     *            The y coordinate to be set
     */
    public Coords(final int pX, final int pY) {
        x = pX;
        y = pY;
    }

    /**
     * Returns a String representation of a coordinate
     */
    public String toString() {
        if (x == -1 && y == -1) {
            return "Pass";
        }
        StringBuffer sb = new StringBuffer();
        char boardVertical;
        sb.append(boardVertical = 'A');
        for (int i = 1; i <= Go.BOARD_SIZE; i++) {
            sb.append(++boardVertical);
        }
        char[] letters = sb.toString().toCharArray();
        return String.valueOf(x + 1) + letters[y];
    }

    /**
     * Converts a given String into Coords if the String matches the
     * requirements. Returns {@code null} otherwise
     * 
     * @param pString
     *            The String to convert to Coords. Must be a two-character
     *            String containing first an Integer from 1 to
     *            {@link Go.BOARD_SIZE} followed be an uppercase character, the
     *            highest of which is the {@link Go.BOARD_SIZE}th letter of the
     *            alphabet. Examples refer to a board size of 9x9.
     * 
     *            example (lower bound): "1A"; example (upper bound): "9I"
     * 
     * @return Coords that are represented by the given String
     */
    static Coords fromString(final String pString) {
        if (pString.equals("Pass")) {
            return new Coords(-1, -1);
        } else if (pString.isEmpty()) {
            return null;
        }
        int x = (Integer.valueOf(pString.charAt(0)) - 1 - '0');
        char y = pString.charAt(1);
        System.out.print(y + "\n");
        int count = 0;
        while (y != 'A') {
            y--;
            count++;
        }
        System.out.print(count + "\n");
        System.out.print(x + "\n");
        if (count < 0 || count >= Go.BOARD_SIZE || x < 0
                || x >= Go.BOARD_SIZE) {
            return null;
        } else {
            return new Coords(x, count);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Coords)) {
            return false;
        }
        final Coords otherCoords = (Coords) other;
        return (x == otherCoords.x && y == otherCoords.y);
    }

    /**
     * Returns all coordinates adjacent to this coordinate whilst respecting the
     * board boundaries. A (0,0) coordinate will, for example only have two
     * adjacent coordinates; center coordinates will have four adjacent
     * coordinates
     * 
     * @return A List of adjacent coordinates within the board boundaries
     */
    public List<Coords> getAdjacentCoords() {
        // LOGGER.info("Retrieving all coordinates adjacent to " +
        // this.toString());
        List<Coords> adjacentCoords = new ArrayList<Coords>();
        Coords up = new Coords(x - 1, y);
        Coords down = new Coords(x + 1, y);
        Coords right = new Coords(x, y + 1);
        Coords left = new Coords(x, y - 1);
        for (Coords coords : new Coords[] { up, down, left, right }) {
            if (coords.x < Go.BOARD_SIZE && coords.x >= 0
                    && coords.y < Go.BOARD_SIZE && coords.y >= 0) {
                // LOGGER.info(coords.toString() + " is adjacent to " +
                // this.toString());
                adjacentCoords.add(coords);
            }
        }
        // LOGGER.info("Found " + adjacentCoords.size() + " adjacent
        // coordinates");
        return adjacentCoords;
    }
}