/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (c) 2023, CoHESyS Lab, cohesys.lab@gmail.com.
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

package uk.ac.ox.poseidon.gui;

import com.esotericsoftware.minlog.Log;
import sim.display.Console;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Paths;

/**
 * Used by the GUI to save/checkpoint the model
 * Created by carrknight on 4/22/16.
 */
public class SaveButton extends JButton implements ActionListener {

    private static final ImageIcon saveIcon;

    static {
        saveIcon = new ImageIcon(FishGUI.IMAGES_PATH.resolve("save.png").toString());
    }

    private final JFileChooser chooser = new JFileChooser();
    private final FishGUI gui;

    /**
     * Creates a button with no set text or icon.
     */
    public SaveButton(FishGUI gui, Dimension dimension) {
        super();
        this.setIcon(
            new ImageIcon(
                saveIcon.getImage().getScaledInstance(dimension.width, dimension.height, Image.SCALE_SMOOTH))
        );
        this.gui = gui;
        chooser.setCurrentDirectory(Paths.get(".").toFile());
        this.addActionListener(this);
        this.setBorderPainted(false);
        this.setContentAreaFilled(false);
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }


    /**
     * asks to save file
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        Console c = (Console) gui.controller;

        if (chooser.showSaveDialog(c) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String currentExtension = FishStateUtilities.getFilenameExtension(file);
            //if the extension is not correct
            if (!(currentExtension.equalsIgnoreCase("checkpoint"))) {
                //force it!
                file = new File(file + ".checkpoint");
            }

            //log that you are about to write
            Log.info("going to save model to " + file);
            synchronized (gui.state.schedule) {

                gui.preCheckPoint();
                FishStateUtilities.writeModelToFile(file, (FishState) gui.state);
                gui.postCheckPoint();
            }

        } else {
        }

    }
}
