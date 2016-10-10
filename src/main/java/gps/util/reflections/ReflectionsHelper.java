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
package gps.util.reflections;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.reflections.Reflections;
import org.reflections.vfs.Vfs;

import com.google.common.collect.Lists;

import gps.util.KryoHelper;
import gps.util.Tuple;

/**
 * Inspired heavily by
 *
 * <link>https://git-wip-us.apache.org/repos/asf?p=isis.git;a=blob;f=core/applib
 * /src/main/java/org/apache/isis/applib/services/classdiscovery/
 * ClassDiscoveryServiceUsingReflections.java;h=
 * 283f053ddb15bfe32f111d88891602820854415e;hb=
 * 283f053ddb15bfe32f111d88891602820854415e</link>
 * 
 * Reflections fix from
 * <link>https://gist.github.com/nonrational/287ed109bb0852f982e8</link>
 * 
 * This also implements a cache for the reflections invokes.
 * 
 * @author haker@uni-bremen.de
 */
public class ReflectionsHelper {

    /**
     * The logger for this class
     */
    private static final Logger LOGGER = Logger
            .getLogger(ReflectionsHelper.class.getCanonicalName());

    /**
     * OSX contains file:// resources on the classpath including .mar and
     * .jnilib files.
     * 
     * Reflections use of Vfs doesn't recognize these URLs and logs warns when
     * it sees them. By registering those file endings, we supress the warns.
     */
    private static void registerUrlTypes() {

        final List<Vfs.UrlType> urlTypes = Lists.newArrayList();

        // include a list of file extensions / filenames to be recognized
        urlTypes.add(new EmptyIfFileEndingsUrlType(".mar", ".jnilib", ".zip"));

        urlTypes.addAll(Arrays.asList(Vfs.DefaultUrlTypes.values()));

        Vfs.setDefaultURLTypes(urlTypes);
    }

    /**
     * Remember whether the url types already have been set.
     */
    private static boolean hasSetUrlTypes = false;

    /**
     * Get a {@link org.reflections.Reflections#Reflections} object. All actions
     * performed on this object are not considered by the cache. Use
     * {@link #getSubTypesOfCached(String, Class)} to use advantage of the
     * cache.
     * 
     * @param str
     *            The string for instantiation.
     * @return The reflections instance.
     */
    public static Reflections getReflections(String str) {
        if (!hasSetUrlTypes) {
            registerUrlTypes();
            hasSetUrlTypes = true;
        }
        return new Reflections(str);
    }

    /**
     * The cache of the reflections stuff
     */
    private static HashMap<Tuple<String, Class<?>>, Set<?>> cache = null;

    /**
     * Calls the {@link org.reflections.Reflections#getSubTypesOf(Class)} method
     * and caches the result.
     * 
     * @param path
     *            The path to search. See
     *            {@link org.reflections.Reflections#Reflections(String, org.reflections.scanners.Scanner...)}
     *            .
     * @param type
     *            The type of the classes. See
     *            {@link org.reflections.Reflections#getSubTypesOf(Class)}.
     * @return The classes that implement the given type. See
     *         {@link org.reflections.Reflections#getSubTypesOf(Class)}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<Class<? extends T>> getSubTypesOfCached(
            final String path, final Class<T> type) {
        if (cache == null) {
            cache = new HashMap<>();
            readCacheFromFile();
        }
        Object ret = cache.getOrDefault(new Tuple<>(path, type), null);
        if (ret == null) {
            Reflections r = getReflections(path);
            Set<Class<? extends T>> set = r.getSubTypesOf(type);
            cache.put(new Tuple<>(path, type), set);
            return set;
        }
        return (Set<Class<? extends T>>) ret;
    }

    /**
     * Read the reflection cache from the resource file. Does nothing if some
     * error occurs. In case of error the cache stays empty and a warning is
     * printed to the logger of this class.
     */
    public static void readCacheFromFile() {
        final String fileName = "reflections.cache";
        URL s = ClassLoader.getSystemClassLoader().getResource(fileName);
        if (s == null) {
            LOGGER.warning("Cannot load " + fileName + " from resources.");
            return;
        }

        InputStream is;
        try {
            is = s.openStream();
            @SuppressWarnings("unchecked")
            Map<Tuple<String, Class<?>>, Set<?>> c = KryoHelper
                    .streamToObject(is, HashMap.class);
            is.close();
            c.entrySet().stream()
                    .forEach(d -> cache.put(d.getKey(), d.getValue()));
        } catch (IOException | RuntimeException e) {
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
                    .getResource("reflections.cache");
            LOGGER.info("writing  cache to " + url.toExternalForm());
            final String path = url.getPath();
            final File file = new File(path);
            final FileOutputStream pw = new FileOutputStream(file);
            try {
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
     * {@link ReflectionsHelper#getSubTypesOfCached(String, Class)} must use
     * reflections to load data.
     */
    public static void clearCache() {
        cache = new HashMap<>();
    }

    /**
     * Singleton class does not provide a constructor
     */
    private ReflectionsHelper() {

    }
}
