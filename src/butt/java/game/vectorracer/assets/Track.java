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
package game.vectorracer.assets;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;

/**
 * This Class represents a vector racer track
 *
 * @author haker@uni-bremen.de
 */
public class Track {

    /**
     * The Data is stored as an image.
     */
    private final BufferedImage track;

    /**
     * Start position for the players.
     */
    private final Vector start;

    /**
     * Path to the image.
     */
    private final String path;

    /**
     * Gets a functional interface for testing a position for a semantic meaning
     *
     * @return {@code true} if a wall or a goal exists {@code false} otherwise.
     */
    public INodeTest getWallGoalTest() {
        return new INodeTest() {
            @Override
            public boolean test(Vector p) {
                return isWall(p) || isGoal(p);
            }
        };
    }

    /**
     * Checks whether the given position points to a node in the track.
     *
     * @param pos
     *            the position
     *
     * @return {@code true} if the node exists {@code false} otherwise.
     */
    private boolean isPositionInMap(final Vector pos) {
        return (pos.getX() < track.getWidth() && pos.getX() >= 0
                && pos.getY() < track.getHeight() && pos.getY() >= 0);
    }

    /**
     * Tests whether the node at the given vector is a goal node
     *
     * @param pos
     *            the position of the node
     *
     * @return {@code true} if the goal exists {@code false} otherwise.
     */
    public boolean isGoal(final Vector pos) {
        return isPositionInMap(pos) && (track.getRGB(pos.getX(), pos.getY())
                & 0xffffff) == 0x0000ff;
    }

    /**
     * Tests whether the node at the given vector is a start node
     *
     * @param pos
     *            the position of the node
     *
     * @return {@code true} if the node exists {@code false} otherwise.
     */
    public boolean isStart(final Vector pos) {
        return isPositionInMap(pos) && (track.getRGB(pos.getX(), pos.getY())
                & 0xffffff) == 0xff0000;
    }

    /**
     * Tests whether the node at the given vector is a movable location for a
     * racer
     *
     * @param pos
     *            the position of the node
     *
     * @return {@code true} if the vector is movable {@code false} otherwise.
     */
    public boolean isMovable(final Vector pos) {
        return (isPositionInMap(pos)) && ((track.getRGB(pos.getX(), pos.getY())
                & 0xffffff) == 0xffffff || isStart(pos) || isGoal(pos));
    }

    /**
     * Tests whether the node at the given vector is a wall node
     *
     * @param pos
     *            the position of the node
     *
     * @return {@code true} if a wall exists {@code false} otherwise.
     */
    public boolean isWall(final Vector pos) {
        return !isMovable(pos);
    }

    /**
     * Returns the alg. sign of the given int. Can be either -1 for negative
     * numbers and 1 for positive numbers.
     *
     * @param i
     *            the int
     *
     * @return the sign
     */
    private static int sign(final int i) {
        return i < 0 ? -1 : 1;
    }

    /**
     * Checks for collisions between a given position and its path represented
     * by the velocity vector.
     *
     * @param pos
     *            the position from where to start checking for collisions
     * @param velocity
     *            the vector to check the collisions with
     * @param nodetest
     *            the test function. If the functional interface returns {@code
     *         true} the node is considered a collision and the collision
     *            detection is immediately stopped.
     *
     * @return the first position where the collision occurred or {@code null}
     *         if no collision occurred.
     */
    public Vector collisionTest(final Vector pos, final Vector velocity,
            final INodeTest nodetest) {
        Vector p = pos.copy();
        p.add(velocity);

        if (velocity.getX() != 0) {
            double dy = (double) velocity.getY() / (double) velocity.getX();
            if (Math.abs(dy) <= 1) { // dy is between -1 and 1
                for (int x = 0; sign(velocity.getX()) == 1
                        ? x <= velocity.getX()
                        : x >= velocity.getX(); x += sign(velocity.getX())) {
                    Vector n = new Vector(x + pos.getX(),
                            (int) Math.round(dy * x) + pos.getY());
                    if (nodetest.test(n.copy())) {
                        return n;
                    }
                }
            } else { // dy is bigger than 1 or smaller than -1
                double dx = 1d / dy; // make it in range from -1 to 1
                for (int y = 0; sign(velocity.getY()) == 1
                        ? y <= velocity.getY()
                        : y >= velocity.getY(); y += sign(velocity.getY())) {
                    Vector n = new Vector((int) Math.round(dx * y) + pos.getX(),
                            y + pos.getY());
                    if (nodetest.test(n.copy())) {
                        return n;
                    }
                }
            }
        } else { // vx is 0, we cannot divide by 0 so here comes the special
            // treatment
            for (int y = pos.getY(); sign(velocity.getY()) == 1 ? y <= p.getY()
                    : y >= p.getY(); y += sign(velocity.getY())) {
                Vector n = new Vector(pos.getX(), y);
                if (nodetest.test(n.copy())) {
                    return n;
                }
            }
        }

        return null;
    }

    /**
     * Construct a Vector Racer track from a file. A red (#ff0000) pixel is
     * considered as the start position for all vector racers. A blue (0x0000ff)
     * pixel is considered a goal node. White pixels are considered free nodes
     * where the players can move to.
     *
     * @param bitmap
     *            The Image
     */
    public Track(final String bitmap) {
        path = bitmap;
        // load image from file
        BufferedImage img = null;
        try {
            URL s = ClassLoader.getSystemClassLoader().getResource(bitmap);
            if (s == null) {
                throw new RuntimeException("getResourceAsStream returned null");
            }
            img = ImageIO.read(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        track = img;

        // find the start position in the image
        int width = img.getWidth();
        int height = img.getHeight();

        Vector startPosition = null;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Vector p = new Vector(x, y);
                if (isStart(p)) {
                    if (startPosition != null) {
                        throw new RuntimeException(
                                "track must contain only one start position");
                    }
                    startPosition = p;
                }
            }
        }

        if (startPosition == null) {
            throw new RuntimeException("track must contain a start position");
        }
        start = startPosition;
    }

    /**
     * The Startposition of the track.
     *
     * @return the start position
     */
    public Vector getStartPosition() {
        return start.copy();
    }

    /**
     * Returns a copy of the image the track is stored in
     *
     * @return the image
     */
    public BufferedImage getImage() {
        ColorModel cm = track.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = track.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Track)) {
            return false;
        }
        Track track1 = (Track) o;

        if (start != null ? !start.equals(track1.start)
                : track1.start != null) {
            return false;
        }
        return path != null ? path.equals(track1.path) : track1.path == null;

    }

    /**
     * Get the path of the track file.
     * 
     * @return The path of the track.
     */
    public String getPath() {
        return path;
    }
}
