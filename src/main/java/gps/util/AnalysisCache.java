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
package gps.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esotericsoftware.kryo.KryoException;

import gps.games.algorithm.analysis.GameAnalysisResult;
import gps.games.wrapper.Game;
import gps.util.reflections.ReflectionsHelper;

/**
 * Analysis Cache class.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class AnalysisCache {

    /**
     * The file name the user specified by calling
     * {@link #setWriteFilename(String)}.
     */
    private static String userFileName = null;

    /**
     * Set the filename for the cache file.
     * 
     * @param pFileName
     *            The file name. If {@code null} the default cache file in the
     *            resource section is used.
     */
    public static void setWriteFilename(final String pFileName) {
        userFileName = pFileName;
    }

    /**
     * The logger for this class
     */
    private static final Logger LOGGER = Logger
            .getLogger(ReflectionsHelper.class.getCanonicalName());

    /**
     * The map that represents the cache.
     */
    private static HashMap<Object, GameAnalysisResult> cache = null;

    /**
     * Get the identifier of a game instance.
     * 
     * @param pGame
     *            The game instance.
     * @return The identifier.
     */
    private static Object getKey(Game<?> pGame) {
        final Object identifier;
        final Object p = pGame.getProblem();
        if (p instanceof IButtSampleProblem) {
            final IButtSampleProblem bs = (IButtSampleProblem) p;
            identifier = bs.getIdentifier();
        } else {
            // fallback if interface is not present
            LOGGER.warning("the class " + p.getClass().getCanonicalName()
                    + " does not implement"
                    + IButtSampleProblem.class.getCanonicalName()
                    + " using kryo to get an identifier (slow)");
            identifier = KryoHelper.objectToString(p);
        }
        return identifier;
    }

    /**
     * Get the cached analysis result of a cache of the game exists.
     * 
     * @param pGame
     *            The game.
     * @return The cached analysis result.
     */
    public static <T> Optional<GameAnalysisResult> getCachedResult(
            final Game<T> pGame) {
        if (cache == null) {
            instantiateNewCache();
            readCacheFromFile();
        }
        final Object identifier = getKey(pGame);
        return Optional.ofNullable((GameAnalysisResult) cache.get(identifier));
    }

    /**
     * Put a game and analysis result to the cache.
     * 
     * @param pGame
     *            The game.
     * @param pAnalysis
     *            The analysis result.
     */
    public static <T> void putToCache(final Game<T> pGame,
            final GameAnalysisResult pAnalysis) {
        if (pGame == null) {
            LOGGER.logp(Level.WARNING, AnalysisCache.class.getCanonicalName(),
                    "putToCache", "pGame is null. Nothing is cached.");
            return; // do nothing
        }
        if (pAnalysis == null) {
            LOGGER.logp(Level.WARNING, AnalysisCache.class.getCanonicalName(),
                    "putToCache", "pAnalysis is null. Nothing is cached.");
            return; // do nothing
        }
        final Object identifier = getKey(pGame);
        cache.put(identifier, pAnalysis);
    }

    /**
     * The filename where the cache is stored.
     */
    private static final String FILENAME = "analysis.cache";

    /**
     * Read the cache from the resource file. Does nothing if some error occurs.
     * In case of error the cache stays empty and a warning is printed to the
     * logger of this class.
     */
    public static void readCacheFromFile() {
        URL s = ClassLoader.getSystemClassLoader().getResource(FILENAME);
        if (s == null) {
            LOGGER.warning("Cannot load " + FILENAME + " from resources.");
            return;
        }

        InputStream is;
        try {
            is = s.openStream();
            @SuppressWarnings("unchecked")
            Map<Object, GameAnalysisResult> c = KryoHelper.streamToObject(is,
                    HashMap.class);
            is.close();
            c.entrySet().stream()
                    .forEach(d -> cache.put(d.getKey(), d.getValue()));
        } catch (IOException | KryoException e) {
            LOGGER.warning("Cannot load reflections cache " + e.getMessage());
            // ignore and do nothing
        }

    }

    /**
     * Writes the current cache to the cache file.
     */
    public static void writeCacheToFile() {
        try {
            final URL url = ClassLoader.getSystemClassLoader()
                    .getResource(FILENAME);
            if (userFileName == null) {
                LOGGER.info("writing  cache to " + url.toExternalForm());
            } else {
                LOGGER.info("writing  cache to " + userFileName);
            }
            final String path = userFileName == null ? url.getPath()
                    : userFileName;
            final File file = new File(path);

            final FileOutputStream pw = new FileOutputStream(file);
            try {
                KryoHelper.objectToBytes(cache);
                pw.write(KryoHelper.objectToBytes(cache));
                pw.flush();
            } finally {
                pw.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Clear the current cache. All subsequent calls to
     * {@link AnalysisCache#getCachedResult(Game)} must use the analyser to load
     * data.
     */
    public static void instantiateNewCache() {
        cache = new HashMap<>();
    }
}
