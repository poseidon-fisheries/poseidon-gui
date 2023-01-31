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

package uk.ac.ox.poseidon.gui.widget;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.function.Function;
import javax.swing.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;
import sim.display.Display2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.tripbased.ProfitFunctionRegression;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.gui.FishGUI;
import uk.ac.ox.poseidon.gui.TriColorMap;
import uk.ac.ox.poseidon.gui.drawing.ColorEncoding;
import uk.ac.ox.poseidon.gui.drawing.ColorfulGrid;
import uk.ac.ox.poseidon.gui.drawing.CoordinateTransformer;

/**
 * Created by carrknight on 6/30/16.
 */
public class GeographicalRegressionWidget implements WidgetBuilder<JComponent, SwingMetawidget> {

    /**
     * needs the gui to plot
     */
    private final FishGUI gui;

    public GeographicalRegressionWidget(FishGUI gui) {
        this.gui = gui;
    }

    /**
     * tries to build a list of market subwidgets for each market available in this MarketMap object
     */
    @Override
    public JComponent buildWidget(String elementName, Map<String, String> attributes, SwingMetawidget metawidget) {

        final Class<?> actualClass = WidgetBuilderUtils.getActualClassOrType(attributes, String.class);
        // if it is a primitive or not a MarketMap we have no use for it
        if (actualClass == null || !GeographicalRegression.class.isAssignableFrom(actualClass)) return null;

        try {
            // nested address? no problem
            String address = StrategyFactoryWidgetProcessor.addressFromPath(attributes, metawidget);
            GeographicalRegression regression =
                    ((GeographicalRegression) PropertyUtils.getProperty(metawidget.getToInspect(), address));

            if (actualClass == ProfitFunctionRegression.class) {

                JPanel panel = new JPanel(null);
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.add(new GeographicalRegressionJButton(
                        gui, regression, metawidget.getToInspect(), attributes.get("name")));
                // now add all the species
                panel.add(new JSeparator());
                GeographicalRegression[] regressions = ((ProfitFunctionRegression) regression).catchesRegression();
                for (int i = 0; i < regressions.length; i++)
                    panel.add(new GeographicalRegressionJButton(
                            gui, regressions[i], metawidget.getToInspect(), "Expected Catches Species " + i));
                return panel;
            } else {
                return new GeographicalRegressionJButton(
                        gui, regression, metawidget.getToInspect(), attributes.get("name"));
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // because of recursion this will happen
            //   e.printStackTrace();
            return null;
        }
    }

    private static class GeographicalRegressionJButton extends JButton implements ActionListener {

        private final FishGUI gui;

        private final GeographicalRegression regression;

        private final Fisher fisher;

        /**
         * Creates a button with no set text or icon.
         */
        GeographicalRegressionJButton(FishGUI gui, GeographicalRegression regression, Fisher fisher, String name) {
            this.gui = gui;
            this.regression = regression;
            this.setText("Show Heatmap");
            this.addActionListener(this);
            this.fisher = fisher;
            setText("Show " + name);
        }

        /**
         * Invoked when an action occurs.
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            final FishState state = (FishState) gui.state;

            ColorfulGrid heatmapPortrayal = new ColorfulGrid(gui.guirandom);
            java.util.List<SeaTile> tiles = state.getMap().getAllSeaTilesExcludingLandAsList();
            DoubleSummaryStatistics statistics = new DoubleSummaryStatistics();
            for (SeaTile tile : tiles) {
                double predict = regression.predict(tile, state.getHoursSinceStart(), fisher, state);
                if (Double.isFinite(predict)) statistics.accept(predict);
            }

            double min = 0;
            double max = 0;
            double average = 0;
            if (statistics.getCount() > 0) {
                min = statistics.getMin();
                max = statistics.getMax();
                average = statistics.getAverage();
            }
            if (min == max) {
                min = -1;
                average = 10;
                max = 40;
            }

            heatmapPortrayal.addEnconding(
                    "Heatmap",
                    new ColorEncoding(
                            new TriColorMap(min, average, max, Color.RED, Color.WHITE, Color.BLUE),
                            new Function<SeaTile, Double>() {
                                @Override
                                public Double apply(SeaTile tile) {
                                    return regression.predict(tile, state.getHoursSinceStart(), fisher, state);
                                }
                            },
                            false));
            Display2D heatmapDisplay = gui.setupPortrayal(heatmapPortrayal);

            // add printout to clicks
            NauticalMap map = ((FishState) gui.state).getMap();
            CoordinateTransformer transformer = new CoordinateTransformer(heatmapDisplay, map);
            MouseListener heatmapClicker = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Int2D gridPosition = transformer.guiToGridPosition(e.getX(), e.getY());
                    double predict;
                    if (regression instanceof ProfitFunctionRegression) {
                        predict = ((ProfitFunctionRegression) regression)
                                .predict(
                                        map.getSeaTile(gridPosition.getX(), gridPosition.getY()),
                                        state.getHoursSinceStart(),
                                        state,
                                        fisher,
                                        true);
                    } else {
                        predict = regression.predict(
                                map.getSeaTile(gridPosition.getX(), gridPosition.getY()),
                                state.getHoursSinceStart(),
                                fisher,
                                state);
                    }

                    System.out.println(gridPosition + ":---> " + predict);
                    System.out.println(e.getX() + " --- " + e.getY());
                    System.out.println("----------------------------------------------");
                }

                @Override
                public void mousePressed(MouseEvent e) {}

                @Override
                public void mouseReleased(MouseEvent e) {}

                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseExited(MouseEvent e) {}
            };
            heatmapDisplay.insideDisplay.addMouseListener(heatmapClicker);

            JFrame heatmapFrame = gui.setupDisplay2D(heatmapPortrayal, heatmapDisplay, "Heatmap", false);
            heatmapPortrayal.setSelectedEncoding("Heatmap");
            heatmapFrame.setVisible(true);
        }
    }
}
