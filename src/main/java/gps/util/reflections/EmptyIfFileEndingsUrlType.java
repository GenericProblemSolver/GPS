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

import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.reflections.vfs.Vfs;

import com.google.common.collect.Lists;

/**
 * 
 * Class that does create an empty Vfs.Dir if a specific file ending has been
 * set.
 * 
 * This class has been adapted from
 * <link>https://gist.github.com/nonrational/287ed109bb0852f982e8</link>
 * 
 * @author haker@uni-bremen.de
 *
 */
class EmptyIfFileEndingsUrlType implements Vfs.UrlType {

    /**
     * The list of file endings that are supposed to be supressed.
     */
    private final List<String> fileEndings;

    /**
     * Construct the object.
     * 
     * @param fileEndings
     *            The file endings that are to be supressed.
     */
    EmptyIfFileEndingsUrlType(final String... fileEndings) {
        this.fileEndings = Lists.newArrayList(fileEndings);
    }

    /**
     * Checks whether an url matches one of the file endings.
     * 
     * @param url
     *            The url that must match one of the file endings.
     * 
     * @return {@code true} if an url matches one of the specified file endings.
     *         {@code false} otherwise.
     */
    public boolean matches(URL url) {

        final String protocol = url.getProtocol();
        final String externalForm = url.toExternalForm();
        if (!protocol.equals("file")) {
            return false;
        }
        for (String fileEnding : fileEndings) {
            if (externalForm.endsWith(fileEnding)) {
                return true;
            }
        }
        return false;
    }

    /**
     * create an empty virtual file sys directory.
     * 
     * @param url
     *            the url for the vfs.
     * @return the vfs directory.
     */
    public Vfs.Dir createDir(final URL url) throws Exception {

        return emptyVfsDir(url);
    }

    /**
     * create an empty virtual file sys directory.
     * 
     * @param url
     *            the url for the vfs.
     * @return the vfs directory.
     */
    private static Vfs.Dir emptyVfsDir(final URL url) {

        return new Vfs.Dir() {
            @Override
            public String getPath() {

                return url.toExternalForm();
            }

            @Override
            public Iterable<Vfs.File> getFiles() {

                return Collections.emptyList();
            }

            @Override
            public void close() {

            }
        };
    }
}