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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create a CSV file with the benchmark data as specified in RFC 4180
 * 
 * @author haker@uni-bremen.de
 *
 */
public class CSVReader implements Closeable {

    /**
     * The Reader where the data is read from.
     */
    private final BufferedReader in;

    /**
     * Construct a CSV Object.
     * 
     * @param fileName
     *            The filename for the csv file to be created.
     * 
     */
    public CSVReader(final String fileName) {
        try {
            in = new BufferedReader(new FileReader(fileName));
        } catch (final IOException e) {
            throw new RuntimeException("cannot read file", e);
        }
    }

    /**
     * Read a row from the CSV file.
     * 
     * @return Returns a map. The map uses the Strings in {@link CSV#FIELDS} as
     *         keys and the read data as values.
     */
    public Map<String, String> readLine() {
        String line;
        try {
            do {
                line = in.readLine();
                if (line == null) {
                    return null;
                }
            } while (line.equals(CSV.HEADLINE));
        } catch (IOException e) {
            return null;
        }
        String[] fields = line.split(",");
        final int num = Math.min(CSV.FIELDS.length, fields.length);

        Map<String, String> map = new HashMap<>(num);

        for (int i = 0; i < num; i++) {
            map.put(CSV.FIELDS[i], fields[i]);
        }
        return map;
    }

    /**
     * Reads all rows from the CSV file.
     * 
     * @return Returns list of a map. The map uses the Strings in
     *         {@link CSV#FIELDS} as keys and the read data as values. Each
     *         entry in the list represents a row.
     */
    public List<Map<String, String>> readAllLines() {
        Map<String, String> map;
        ArrayList<Map<String, String>> list = new ArrayList<>();
        for (;;) {
            map = readLine();
            if (map == null) {
                return list;
            }
            list.add(map);
        }
    }

    /**
     * Close the used stream.
     */
    @Override
    public void close() {
        try {
            in.close();
        } catch (IOException e) {
        }
    }
}
