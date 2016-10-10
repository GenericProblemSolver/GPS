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

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import butt.tool.Arguments;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class for the benchmark and training tool graphical user interface.
 * 
 * If any arguments are passed to the tool or no window environment can be found
 * the tool is started headless otherwise a windows is shown.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class MainGUI extends Application {

    /**
     * Main entry point for GUI.
     * 
     * @param args
     *            Program Arguments.
     */
    public static void main(String[] args) {
        Arguments.parse(args);
        if (Arguments.isNoGui() || GraphicsEnvironment.isHeadless()) {
            butt.MainBUTT.main(args); // run headless mode
        } else {
            launch(args);
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        ResourceBundle bundle;
        try {
            bundle = (ResourceBundle.getBundle("gui.bundles.Main"));
        } catch (MissingResourceException e) {
            bundle = (ResourceBundle.getBundle("gui.bundles.Main",
                    Locale.ENGLISH));
        }
        stage.setTitle(bundle.getString("window"));
        stage.setScene(
                new Scene(FXMLLoader.load(ClassLoader.getSystemClassLoader()
                        .getResource("gui/fxml/main.fxml"), bundle)));
        stage.show();
        // this is a nice position to write reflection cache to file
        // ReflectionsHelper.writeCacheToFile();
    }

}
