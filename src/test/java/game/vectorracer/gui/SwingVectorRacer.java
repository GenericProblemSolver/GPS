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
package game.vectorracer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

import game.vectorracer.VectorRacer;
import game.vectorracer.assets.Player;
import game.vectorracer.assets.Vector;

/**
 * Provides a graphical user interface for the vector racer game.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class SwingVectorRacer {

    /**
     * Reference to the game
     */
    static VectorRacer game;

    /**
     * List of the initial players
     */
    static List<Player> players;

    /**
     * Apply all changes after a move to the scene
     */
    static void render() {
        final BufferedImage img = game.getTrack().getImage();
        for (final Player p : game.getParticipatingPlayers()) {
            img.setRGB(p.getPos().getX(), p.getPos().getY(), 0xff7700);
        }

        Player player = game.getCurrentPlayer();

        if (player != null) {
            final Vector p = player.getPos();
            final Vector v = player.getVelocity();

            Graphics2D g = img.createGraphics();
            g.setColor(Color.CYAN);
            // line actually doesn't have to show the exact collision line
            g.drawLine(p.getX(), p.getY(), p.getX() + v.getX(),
                    p.getY() + v.getY());

            img.setRGB(p.getX(), p.getY(), 0xff00ff);
        }

        icon.setIcon(new ImageIcon(img.getScaledInstance(img.getWidth() * 6,
                img.getHeight() * 6, Image.SCALE_FAST)));

        if (game.isFinished() || game.hasWinner()) {
            label.setText("Game over - winner is " + (game.getWinner() == null
                    ? "nobody" : ("Player " + game.getWinner().getId())));
        }

        list.setSelectedValue(game.getCurrentPlayer(), true);

        list.repaint();

    }

    /**
     * The ImageIcon container
     */
    private static final JLabel icon = new JLabel();

    /**
     * The JList which contains all the players of the game
     */
    private static final JList<Player> list = new JList<Player>();

    /**
     * The Label which gets shown when the game is over
     */
    private static final JLabel label = new JLabel("", JLabel.CENTER);

    /**
     * main Method shows a windows which you can use to play the game
     * 
     * @param agrs
     *            is ignored
     */
    public static void main(String agrs[]) {
        game = new VectorRacer(2, "vectorracer/track.bmp");
        players = game.getParticipatingPlayers();

        list.setListData(players.toArray(new Player[1]));
        list.setFixedCellWidth(300);
        label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 48));

        JFrame frame = new JFrame("Vector Racer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(icon, BorderLayout.CENTER);

        GridLayout gridLayout = new GridLayout(3, 3);
        Panel buttonpanel = new Panel();
        buttonpanel.setLayout(gridLayout);

        final Vector actions[] = game.getActions();
        for (final Vector v : actions) {
            final JButton button = new JButton(v.toString());
            button.addActionListener((e) -> {
                game.move(v);
                render();
            });
            buttonpanel.add(button);
        }
        frame.add(buttonpanel, BorderLayout.PAGE_END);
        frame.add(list, BorderLayout.LINE_END);
        frame.add(label, BorderLayout.PAGE_START);

        render();

        frame.pack();
        frame.setVisible(true);
    }
}
