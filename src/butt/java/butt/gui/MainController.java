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
package butt.gui;

import javafx.fxml.FXML;

/**
 * Main controller for the gui
 * 
 * @author haker@uni-bremen.de
 *
 */
public class MainController {

    /**
     * Gets called by fxml loader once after every attribute has been
     * inisitalized
     */
    public void initialize() {
    }

    /**
     * Called when the quit button is pressed from the menu: file > quit
     */
    @FXML
    private void quit() {
        javafx.application.Platform.exit();
    }
}