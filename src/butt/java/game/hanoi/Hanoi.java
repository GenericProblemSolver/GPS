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
package game.hanoi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import game.hanoi.assets.Action;
import gps.annotations.Heuristic;
import gps.annotations.Move;
import gps.annotations.TerminalTest;
import gps.util.IButtSampleProblem;

/**
 * Implementation for a Towers of Hanoi Game.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class Hanoi implements IButtSampleProblem {

    /**
     * The number of towers the game features.
     */
    private static final int TOWER_COUNT = 3;

    /**
     * An array of arrays which contain all towers. Each int represents a disk
     * and the value specifies the size of the disk. If a value is zero or below
     * the level is unoccupied.
     */
    private final int towers[][];

    /**
     * The heights of each tower. Basically the number of disks a tower
     * contains.
     */
    private final int heights[];

    /**
     * The disk that currently is lifted and waiting for a tower to be placed
     * on.
     */
    private int liftedDisk;

    /**
     * Constructs a Tower of Hanoi game with three towers and all disks
     * initially placed on the first tower.
     * 
     * @param numOfDisks
     *            The number of disks. Standard Hanoi has 7 disks.
     */
    public Hanoi(final int numOfDisks) {
        if (numOfDisks < 1) {
            throw new IllegalArgumentException(
                    "Hanoi must at least contain 1 disk");
        }

        towers = new int[TOWER_COUNT][numOfDisks];
        heights = new int[TOWER_COUNT];

        for (int i = numOfDisks; i > 0; i--) {
            getTower(0)[numOfDisks - i] = i;
        }
        heights[0] = numOfDisks;
        liftedDisk = 0;
    }

    /**
     * Checks whether the player can lift a disk from the specified tower. A
     * player can lift a disk if there exists a disk on the tower and if no
     * other disk is currently lifted.
     * 
     * Disks that have been lifted can be placed onto a tower with the
     * {@link #placeDisk(int)} method.
     * 
     * Only one disk at a time can be lifted.
     * 
     * @param tower
     *            The tower from where the topmost disk should be retrieved.
     *            There exists {@value #TOWER_COUNT} towers so the value must be
     *            between 0 (inclusive) and {@value #TOWER_COUNT} (exclusive).
     * 
     * @return {@code true} if the disk can be lifted. {@code false} otherwise.
     */
    private boolean canLift(int tower) {
        return liftedDisk <= 0 && getHeightOfTower(tower) > 0;

    }

    /**
     * Lifts a disk from the specified tower. A player can lift a disk if there
     * exists a disk on the tower and if no other disk is currently lifted. If
     * the disk cannot be lifted an exception is thrown.
     * 
     * Disks that have been lifted can be placed onto a tower with the
     * {@link #placeDisk(int)} method.
     * 
     * Only one disk at a time can be lifted.
     * 
     * Use {@link #canLift(int)} to check whether it is safe to place the disk.
     * 
     * @param tower
     *            The tower from where the topmost disk should be lifted. There
     *            exists {@value #TOWER_COUNT} towers so the value must be
     *            between 0 (inclusive) and {@value #TOWER_COUNT} (exclusive).
     */
    private void liftDisk(int tower) {
        if (liftedDisk > 0) {
            throw new RuntimeException("only one disk at a time can be lifted");
        }
        if (!canLift(tower)) {
            throw new RuntimeException(
                    "the tower does not contain any disk to lift");
        }

        int height = --heights[tower];
        liftedDisk = getTower(tower)[height];
        getTower(tower)[height] = 0;
    }

    /**
     * Checks whether the player can place the currently lifted disk onto the
     * specified tower. A player can place a disk if it has been previously
     * lifted and the disk on top of the target tower is bigger than the disk to
     * place.
     * 
     * Disks that have been lifted with the {@link #liftDisk(int)} method can be
     * placed onto a tower.
     * 
     * Only one disk at a time can be lifted.
     * 
     * @param tower
     *            The tower where the disk should be placed to. There exists
     *            {@value #TOWER_COUNT} towers so the value must be between 0
     *            (inclusive) and {@value #TOWER_COUNT} (exclusive).
     * 
     * @return {@code true} if the disk can be placed. {@code false} otherwise.
     */
    private boolean canPlace(int tower) {
        return liftedDisk > 0 && (getHeightOfTower(tower) <= 0
                || getTower(tower)[getHeightOfTower(tower) - 1] > liftedDisk);
    }

    /**
     * Places the currently lifted disk onto the top of the specified tower. A
     * player can place a disk if it has been previously lifted and the disk on
     * top of the target tower is bigger than the currently lifted disk.
     * 
     * Disks that have been lifted with the {@link #liftDisk(int)} method can be
     * placed onto a tower.
     * 
     * Only one disk at a time can be lifted.
     * 
     * Use {@link #canPlace(int)} to check whether it is safe to place the disk.
     * 
     * @param tower
     *            The tower where the disk should be placed to. There exists
     *            {@value #TOWER_COUNT} towers so the value must be between 0
     *            (inclusive) and {@value #TOWER_COUNT} (exclusive).
     */
    private void placeDisk(int tower) {
        if (liftedDisk <= 0) {
            throw new RuntimeException(
                    "a disk must be lifted before it can be placed");
        }
        final int[] stack = getTower(tower);
        if (!canPlace(tower)) {
            throw new RuntimeException(
                    "a disk can only be placed on a bigger disk");
        }
        stack[heights[tower]++] = liftedDisk;
        liftedDisk = 0;
    }

    /**
     * Moves the topmost disk from {@code fromTower} onto the {@code toTower}. A
     * disk can only be moved if there is currently no other disk lifted.
     * 
     * If a disk is currently lifted an RuntimeException is thrown.
     * 
     * If there exists no disk on the {@code fromTower} an RuntimeException is
     * thrown.
     * 
     * If the disk cannot be placed onto {@code toTower} an RuntimeException is
     * thrown.
     * 
     * @param fromTower
     *            The tower from where the topmost disk should be lifted. There
     *            exists {@value #TOWER_COUNT} towers so the value must be
     *            between 0 (inclusive) and {@value #TOWER_COUNT} (exclusive).
     * 
     * @param toTower
     *            The tower where the disk should be placed to. There exists
     *            {@value #TOWER_COUNT} towers so the value must be between 0
     *            (inclusive) and {@value #TOWER_COUNT} (exclusive).
     */
    public void move(int fromTower, int toTower) {
        liftDisk(fromTower);
        placeDisk(toTower);
    }

    /**
     * Checks whether the game is over. The game is over if all disks have been
     * piled up at the last tower.
     * 
     * @return {@code true} if the game is over {@code false} otherwise.
     */
    @gps.annotations.TerminalTest
    public boolean isGameOver() {
        return getHeightOfTower(
                TOWER_COUNT - 1) == getTower(TOWER_COUNT - 1).length;
    }

    /**
     * Get the tower array.
     * 
     * @param pTowerId
     *            There exists {@value #TOWER_COUNT} towers so the value must be
     *            between 0 (inclusive) and {@value #TOWER_COUNT} (exclusive).
     * @return An array of Disks. Values less than 1 represent no-disk.
     */
    private int[] getTower(int pTowerId) {
        return towers[pTowerId];
    }

    /**
     * Get the height of a tower. The height is defined by the amount of disks
     * that are piled.
     * 
     * @param pTowerId
     *            There exists {@value #TOWER_COUNT} towers so the value must be
     *            between 0 (inclusive) and {@value #TOWER_COUNT} (exclusive).
     * @return the height.
     */
    private int getHeightOfTower(int pTowerId) {
        return heights[pTowerId];
    }

    /**
     * Performs a move. Available moves can be retrieved with the
     * {@link #getMoves()} method.
     * 
     * If the move is not a possible move, an exception is thrown. See
     * {@link #move(int, int)} for more information.
     * 
     * @param pAction
     *            There exists {@value #TOWER_COUNT} towers so the tower values
     *            must be between 0 (inclusive) and {@value #TOWER_COUNT}
     *            (exclusive).
     */
    @Move
    public void move(final Action pAction) {
        move(pAction.from, pAction.to);
    }

    /**
     * Get a list of all available moves.
     * 
     * @return the list of available moves.
     */
    @gps.annotations.Action
    public List<Action> getMoves() {
        List<Action> ret = new ArrayList<>(TOWER_COUNT * TOWER_COUNT);
        for (int j = 0; j < TOWER_COUNT; j++) {
            for (int i = 0; i < TOWER_COUNT; i++) {
                if (canMove(j, i)) {
                    ret.add(new Action(j, i));
                }
            }
        }
        return ret;
    }

    /**
     * 
     * Checks whether the player can lift and place a disk from and to the
     * specified tower. A player can lift a disk if there exists a disk on the
     * tower and if no other disk is currently lifted. The disk can be placed
     * only if the underlying disk is bigger than the disk to place onto this
     * tower.
     * 
     * 
     * @param toLift
     *            The tower from where the topmost disk should be retrieved.
     *            There exists {@value #TOWER_COUNT} towers so the value must be
     *            between 0 (inclusive) and {@value #TOWER_COUNT} (exclusive).
     * @param toPlace
     *            The tower where the disk should be placed to. There exists
     *            {@value #TOWER_COUNT} towers so the value must be between 0
     *            (inclusive) and {@value #TOWER_COUNT} (exclusive).
     * @return {@code true} if the disk can be transfered. {@code false}
     *         otherwise.
     */

    private boolean canMove(int toLift, int toPlace) {
        if (!canLift(toLift)) {
            return false;
        }
        liftDisk(toLift);
        try {
            if (!canPlace(toPlace)) {
                return false;
            }
        } finally {
            placeDisk(toLift);
        }
        return true;
    }

    /**
     * copy constructor
     * 
     * @param o
     *            The instance from where to copy from
     */
    private Hanoi(final Hanoi o) {
        towers = new int[TOWER_COUNT][];
        for (int i = 0; i < TOWER_COUNT; i++) {
            towers[i] = Arrays.copyOf(o.towers[i], o.towers[i].length);
        }
        heights = Arrays.copyOf(o.heights, o.heights.length);
        liftedDisk = o.liftedDisk;
    }

    @Override
    public Hanoi clone() {
        return new Hanoi(this);
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Hanoi)) {
            return false;
        }
        final Hanoi o = (Hanoi) other;
        for (int t = 0; t < TOWER_COUNT; t++) {
            for (int l = 0; l < getHeightOfTower(t); l++) {
                if (getTower(t)[l] != o.getTower(t)[l]) {
                    return false;
                }
            }
        }
        return liftedDisk == o.liftedDisk && Arrays.equals(heights, o.heights);
    }

    @Override
    public int hashCode() {
        int h = Objects.hash(liftedDisk, Arrays.hashCode(heights));
        for (int t = 0; t < TOWER_COUNT; t++) {
            for (int l = 0; l < getHeightOfTower(t); l++) {
                Objects.hash(h, getTower(t)[l]);
            }
        }
        return h;
    }

    /**
     * Appends a well formatted disk to the StringBuilder. If disk is below 1
     * the tower is being shown.
     * 
     * @param sb
     *            The StringBuilder.
     * @param disk
     *            The size of the disk.
     * @param towerWidth
     *            The total size of the space in characters to fill.
     */
    private void appendDisk(final StringBuilder sb, int disk, int towerWidth) {
        final char blankChar = ' ';
        final char c = disk >= 1 ? '=' : '|';
        final int size = disk >= 1 ? disk * 2 - 1 : 1;
        final int blanks = (towerWidth - size) / 2;
        for (int i = 0; i < blanks; i++) {
            sb.append(blankChar);
        }
        for (int i = 0; i < size; i++) {
            sb.append(c);
        }
        for (int i = 0; i < blanks; i++) {
            sb.append(blankChar);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        final int width = getTower(0).length * 2 - 1;
        for (int story = getTower(0).length - 1; story >= 0; story--) {
            for (int tower = 0; tower < TOWER_COUNT; tower++) {
                appendDisk(sb, story >= getHeightOfTower(tower) ? 0
                        : getTower(tower)[story], width);
                sb.append(' ');
            }
            if (story > 0) {
                sb.append('\n');
            }
        }
        if (liftedDisk > 0) {
            appendDisk(sb, liftedDisk, width);
        }
        sb.append('\n');
        return sb.toString();
    }

    /**
     * A simple heuristic for testing purposes.
     * 
     * @return higher the more disks are on the target tower.
     */
    @Heuristic
    public int heuristicTest() {
        return getHeightOfTower(TOWER_COUNT - 1);
    }

    @Override
    public Object getIdentifier() {
        return "Han" + Objects.hash(liftedDisk, Arrays.hashCode(heights));
    }
}
