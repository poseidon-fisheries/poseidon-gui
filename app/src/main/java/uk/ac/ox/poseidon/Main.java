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

package uk.ac.ox.poseidon;

import com.esotericsoftware.minlog.Log;
import com.google.common.io.Files;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import sim.display.Console;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateLogger;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;
import uk.ac.ox.poseidon.gui.FishGUI;
import uk.ac.ox.poseidon.gui.ScenarioSelector;

public class Main {

    public static final long SEED = System.currentTimeMillis();
    public static final int STEPS_PER_DAY = 1;

    // main
    public static void main(String[] args) throws IOException {
        File file = new File(".");
        System.out.println("Current Working Directory: " + file.getAbsolutePath());
        // this is relatively messy, as all GUI functions are
        // basically it creates a widget to choose the scenario object and its parameters
        // once that's done you create a new Fisherstate and give it the  scenario
        // the you pass the FisherState to the FishGui and the model starts.

        final boolean[] instantiate = {false};

        // this is the first main container
        final JDialog scenarioSelection = new JDialog((JFrame) null, true);
        final ScenarioSelector scenarioSelector = new ScenarioSelector();
        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(scenarioSelector, BorderLayout.CENTER);
        // create ok and exit button
        Box buttonBox = new Box(BoxLayout.LINE_AXIS);
        contentPane.add(buttonBox, BorderLayout.SOUTH);
        final JButton ok = new JButton("OK");
        ok.addActionListener(e -> {
            instantiate[0] = true;
            scenarioSelection.dispatchEvent(new WindowEvent(scenarioSelection, WindowEvent.WINDOW_CLOSING));
        });
        buttonBox.add(ok);
        final JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> System.exit(0));
        buttonBox.add(cancel);

        // create file opener (for YAML)
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        Log.info("current directory: " + Paths.get(".").toAbsolutePath());
        chooser.setCurrentDirectory(Paths.get(".").toFile());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) // you can open directories
                return true;
                String extension =
                        Files.getFileExtension(f.getAbsolutePath()).trim().toLowerCase();
                return extension.equals("yaml") || extension.equals("yml");
            }

            @Override
            public String getDescription() {
                return "Any YAML scenario";
            }
        });

        final JButton readFromFileButton = new JButton("Open scenario from file");
        readFromFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (chooser.showOpenDialog(scenarioSelector) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    // log that you are about to write
                    Log.info("opened file " + file);
                    FishYAML yaml = new FishYAML();
                    try {
                        // read yaml
                        Scenario scenario = yaml.loadAs(
                                String.join("\n", java.nio.file.Files.readAllLines(file.toPath())), Scenario.class);
                        // add it to the swing
                        SwingUtilities.invokeLater(() -> {
                            if (scenarioSelector.hasScenario("yaml")) scenarioSelector.removeScenarioOption("yaml");
                            String name = file.getName();
                            scenarioSelector.addScenarioOption(name, scenario);
                            scenarioSelector.select(name);
                            scenarioSelector.repaint();
                        });

                    } catch (Exception yamlError) {
                        Log.warn(yamlError.getMessage());
                        Log.warn(file + " is not a valid YAML scenario!");
                    }
                } else {
                    Log.info("open file cancelled");
                }
            }
        });
        final JButton writeToFileButton = new JButton("Save scenario to file");
        writeToFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (chooser.showSaveDialog(scenarioSelector) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    String currentExtension = FishStateUtilities.getFilenameExtension(file);
                    // if the extension is not correct
                    if (!(currentExtension.equalsIgnoreCase("yaml") | currentExtension.equalsIgnoreCase("yml"))) {
                        // force it!
                        file = new File(file + ".yaml");
                    }

                    // log that you are about to write
                    Log.info("going to save config to file " + file);
                    FishYAML yaml = new FishYAML();
                    String toWrite = yaml.dump(scenarioSelector.getScenario());
                    try {
                        Files.write(toWrite.getBytes(), file);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        Log.error("Failed to write to " + file);
                        Log.error(e1.getMessage());
                    }
                } else {
                    Log.info("save cancelled");
                }
            }
        });
        buttonBox.add(readFromFileButton);
        buttonBox.add(writeToFileButton);

        final JButton restoreButton = new JButton("Restore saved model");
        restoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                assert SwingUtilities.isEventDispatchThread();
                System.out.println(SwingUtilities.isEventDispatchThread());
                FishState state = new FishState(SEED, STEPS_PER_DAY);
                FishGUI vid = new FishGUI(state);
                Console c = new Console(vid);
                scenarioSelection.setEnabled(false);
                c.doOpen();
                c.setVisible(true);

                scenarioSelection.dispatchEvent(new WindowEvent(scenarioSelection, WindowEvent.WINDOW_CLOSING));
            }
        });

        buttonBox.add(restoreButton);
        scenarioSelection.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        scenarioSelection.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}

            @Override
            public void windowClosing(WindowEvent e) {}

            @Override
            public void windowClosed(WindowEvent e) {
                if (!instantiate[0]) System.exit(0);
            }

            @Override
            public void windowIconified(WindowEvent e) {}

            @Override
            public void windowDeiconified(WindowEvent e) {}

            @Override
            public void windowActivated(WindowEvent e) {}

            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        scenarioSelection.setContentPane(contentPane);
        scenarioSelection.pack();
        // limit its max size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (scenarioSelection.getSize().height > screenSize.height) {
            scenarioSelection.setMaximumSize(screenSize);
            scenarioSelection.setSize(screenSize);
        }
        scenarioSelection.setVisible(true);

        if (instantiate[0]) {
            FishState state = new FishState(SEED, STEPS_PER_DAY);
            Log.set(Log.LEVEL_INFO);
            Log.setLogger(new FishStateLogger(state, Paths.get("log.txt")));

            state.setScenario(scenarioSelector.getScenario());

            state.attachAdditionalGatherers();

            FishGUI vid = new FishGUI(state);
            Console c = new Console(vid);
            c.setVisible(true);
        }
    }
}
