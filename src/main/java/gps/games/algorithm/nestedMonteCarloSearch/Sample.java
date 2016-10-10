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
package gps.games.algorithm.nestedMonteCarloSearch;

import java.util.ArrayList;
import java.util.List;

import gps.games.wrapper.Action;
import gps.games.wrapper.Game;

/**
 * Representing a sampling result used by {@link NMCS} and {@link IterativeNMCS}.
 * Used to store a sequence of actions that was determined by {@link NMCS} to
 * solve a given game and its value (i.e. the return (or an abstraction of the return)
 * {@link Game#getUtility()}).
 * 
 * @author jschloet@tzi.de
 *
 */
public class Sample {

    /**
     * The value of {@link Sample#sequence} , i.e. the return (or an abstraction of the return)
     * {@link Game#getUtility()} called on the game state that can be reached using {@link Sample#sequence}.
     */
    private double value;

    /**
     * Sequence of {@link Action}s that was determined by {@link NMCS} to be the solution for
     * a given game.
     */
    private List<Action> sequence;

    /**
     * Constructor. Sets {@link Sample#value} with the given parameter.
     * Instantiates the {@link #sequence}.
     * 
     * @param pValue Used to set {@link Sample#value}.
     */
    Sample(double pValue) {
        setValue(pValue);
        setSequence(new ArrayList<>());
    }

    /**
     * Empty constructor
     */
    Sample() {

    }

    /**
     * Getter for {@link Sample#value}
     * 
     * @return {@link Sample#value}
     */
    public double getValue() {
        return value;
    }

    /**
     * Setter for {@link Sample#value}
     * 
     * @param value Used to set {@link Sample#value}
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Getter for {@link Sample#sequence}
     * 
     */
    public List<Action> getSequence() {
        return sequence;
    }

    /**
     * Setter for {@link Sample#sequence}
     * 
     * @param sequence Used to set {@link Sample#sequence}
     */
    public void setSequence(List<Action> sequence) {
        this.sequence = sequence;
    }
}
