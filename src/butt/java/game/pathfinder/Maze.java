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
package game.pathfinder;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import gps.annotations.Heuristic;
import gps.annotations.Move;
import gps.annotations.TerminalTest;
import gps.util.IButtSampleProblem;

/**
 * A simple maze implementation that loads a maze from image and allows the
 * player to travel through the maze.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class Maze implements IButtSampleProblem {
    /** RGB color representation of the player */
    private final static int PLAYER_RGB = 0xff0000;

    /** RGB color representation of the goal */
    private final static int GOAL_RGB = 0x0000ff;

    /** RGB color representation of a wall (a node the player cannot move to) */
    private final static int WALL_RGB = 0x000000;

    /**
     * RGB color representation of a free node (the player can move to these
     * nodes)
     */
    private final static int FREE_RGB = 0xffffff;

    /**
     * the maze data.
     * 
     * 0:free, player is free to move here
     * 
     * 1:wall, player cannot move here
     */
    protected final byte maze[];

    /** wall marker in the maze array */
    private final static byte MAZE_WALL = 1;

    /** free marker in the maze array */
    private final static byte MAZE_FREE = 0;

    /**
     * width of the rectangular maze in nodes
     */
    protected final int width;

    /**
     * height of the rectangular maze in nodes
     */
    protected final int height;

    /**
     * current player position: y * width + x;
     */
    protected int playerPosition;

    /**
     * goal position: y * width + x;
     */
    protected final int goalPosition;

    /**
     * Copy Constructor
     * 
     * @param m
     *            The maze to copy from
     */
    private Maze(final Maze m) {
        maze = m.maze;
        width = m.width;
        height = m.height;
        playerPosition = m.playerPosition;
        goalPosition = m.goalPosition;
    }

    /**
     * Construct a Maze
     * 
     * @param pWidth
     *            Width of maze in nodes
     * @param pHeight
     *            Height of maze in nodes
     * @param pMaze
     *            Maze represented as array. row by row beginning with row 0.
     *            The array gets cloned.
     * @param pPlayerPos
     *            position of player in array (index of array)
     * @param pGoalPos
     *            position of goal in array (index of array)
     */
    protected Maze(final int pWidth, final int pHeight, final byte pMaze[],
            final int pPlayerPos, final int pGoalPos) {
        maze = pMaze.clone();
        width = pWidth;
        height = pHeight;
        playerPosition = pPlayerPos;
        goalPosition = pGoalPos;
    }

    @Override
    public Object clone() {
        return new Maze(this);
    }

    /**
     * Construct a new maze loading it from a bitmap.
     */
    public Maze(final String bitmap) {
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

        width = img.getWidth();
        height = img.getHeight();

        int dst = -1;
        playerPosition = -1;

        maze = new byte[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int rgb = img.getRGB(x, y) & 0xffffff;
                maze[y * width + x] = MAZE_FREE;
                switch (rgb) {
                case PLAYER_RGB:
                    if (playerPosition != -1) {
                        throw new RuntimeException(
                                "Labyrinth hat multiple player start point. Remove a red pixel.");
                    }
                    playerPosition = y * width + x;
                    break;
                case GOAL_RGB: // blu
                    if (dst != -1) {
                        throw new RuntimeException(
                                "Labyrinth hat multiple player goal point. Remove a blue pixel.");
                    }
                    dst = y * width + x;
                    break;
                case WALL_RGB: // black
                    maze[y * width + x] = MAZE_WALL;
                    break;
                }
            }
        }

        if (playerPosition == -1) {
            throw new RuntimeException(
                    "Labyrinth hat no player start point. Add a red pixel.");
        }
        if (dst == -1) {
            throw new RuntimeException(
                    "Labyrinth hat no goal. Add a blue pixel.");
        } else {
            goalPosition = dst;
        }
    }

    /**
     * Determines whether the player won the game.
     * 
     * @return {@code true} if player reached the target, otherwise
     *         {@code false}.
     */
    @TerminalTest
    public boolean hasWon() {
        return playerPosition == goalPosition;
    }

    /**
     * Calculate squared distance from current player position to goal position.
     * Can be used as a heuristic.
     * 
     * @return Squared distance to goal
     */
    @Heuristic
    public int distanceToDestination() {
        final int playerX = playerPosition % width;
        final int playerY = playerPosition / width;
        final int destX = goalPosition % width;
        final int destY = goalPosition / width;
        final int deltaX = destX - playerX;
        final int deltaY = destY - playerY;
        final int sq = deltaX * deltaX + deltaY * deltaY;
        return sq;
    }

    /**
     * move player up if possible
     */
    @Move
    public void moveUp() {
        final int newPos = playerPosition - width;
        if (newPos >= 0 && maze[newPos] != MAZE_WALL) {
            playerPosition = newPos;
        }
    }

    /**
     * move player down if possible
     */
    @Move
    public void moveDown() {
        final int newPos = playerPosition + width;
        if (newPos < width * height && maze[newPos] != MAZE_WALL) {
            playerPosition = newPos;
        }
    }

    /**
     * move player left if possible
     */
    @Move
    public void moveLeft() {
        final int newPos = playerPosition - 1;
        if (playerPosition % width > 0 && newPos > 0
                && maze[newPos] != MAZE_WALL) {
            playerPosition = newPos;
        }
    }

    /**
     * move player right if possible
     */
    @Move
    public void moveRight() {
        final int newPos = playerPosition + 1;
        if (newPos % width != 0 && newPos < width * height
                && maze[newPos] != MAZE_WALL) {
            playerPosition = newPos;
        }
    }

    /**
     * gets all possible successors
     */
    public List<Maze> getSuccessors() {
        List<Maze> lst = new ArrayList<Maze>(4);
        Maze a = (Maze) clone();
        Maze b = (Maze) clone();
        Maze c = (Maze) clone();
        Maze d = (Maze) clone();
        a.moveDown();
        b.moveUp();
        c.moveLeft();
        d.moveRight();
        if (a.playerPosition != playerPosition) {
            lst.add(a);
        }
        if (b.playerPosition != playerPosition) {
            lst.add(b);
        }
        if (c.playerPosition != playerPosition) {
            lst.add(c);
        }
        if (d.playerPosition != playerPosition) {
            lst.add(d);
        }
        return lst;
    }

    /**
     * Generates a rgb value from a node of the maze which is represented as a
     * byte.
     * 
     * @param b
     *            the byte representation of the node
     * @return the color in rgb
     */
    protected int mazeToRGB(byte b) {
        final int ret;
        switch (b) {
        case MAZE_WALL:
            ret = WALL_RGB;
            break;
        case MAZE_FREE:
            ret = FREE_RGB;
            break;
        default:
            throw new RuntimeException("Unexpected Data in Maze Array");
        }
        return ret;
    }

    /**
     * Create an Image representing this maze. Each node is represented as a
     * pixel.
     * 
     * @return The maze as an Image
     */
    public Image getImage() {
        final BufferedImage img = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int color = mazeToRGB(maze[y * width + x]);
                img.setRGB(x, y, color);
            }
        }

        img.setRGB(playerPosition % width, playerPosition / width, PLAYER_RGB);
        img.setRGB(goalPosition % width, goalPosition / width, GOAL_RGB);

        return img;
    }

    /**
     * Shows the maze as a message box
     * 
     * @throws java.awt.HeadlessException
     *             if GraphicsEnvironment.isHeadless returns true
     */
    public void showMessageBox() {
        JOptionPane.showMessageDialog(null,
                new JLabel(new ImageIcon(getImage().getScaledInstance(width * 2,
                        height * 2, Image.SCALE_FAST))),
                "MazeBox", JOptionPane.PLAIN_MESSAGE, null);
    }

    @Override
    public Object getIdentifier() {
        return "Mz" + Objects.hash(this.goalPosition, this.height,
                this.playerPosition, this.width);
    }
}
