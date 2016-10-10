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
package gps.games.gdl;

import java.io.File;
import java.io.IOException;

import org.ggp.base.util.files.FileUtils;
import org.ggp.base.util.gdl.factory.GdlFactory;
import org.ggp.base.util.gdl.factory.exceptions.GdlFormatException;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.symbol.factory.exceptions.SymbolFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class containing a parse-method.
 * 
 * @author Sven
 *
 */
public class GDL {

    private static final Logger logger = LoggerFactory.getLogger(GDL.class);

    /**
     * <p>
     * Tries to parse the file whose path is given.
     * </p>
     * 
     * <p>
     * The given file should be parsed correctly, if the file is encoded in
     * UTF-8 and it's contents conform to the specified grammar
     * </p>
     * 
     * @param filepath
     *            the absolute or relative path of file to be parsed
     * @return the parsed file's internal representation.
     * @throws IOException
     *             if there is an IOException when trying to read the file with
     *             the given filepath.
     * @throws SymbolFormatException
     * @throws GdlFormatException
     */
    public static StanfordGDLGame parse(final String filepath)
            throws IOException, GdlFormatException, SymbolFormatException {
        if (filepath == null) {
            logger.error(
                    "The static method GDL.parse() was called with the argument null!");
            throw new IllegalArgumentException("null is not a valid filepath");
        }
        logger.info("Trying to parse the file \"" + filepath + "\"");
        String gdl = FileUtils.readFileAsString(new File(filepath));
        StateMachine sm = new CachedStateMachine(new ProverStateMachine());
        sm.initialize(GdlFactory.createList(gdl));
        return new StanfordGDLGame(sm);
    }

}
