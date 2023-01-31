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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.swing.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.widgetprocessor.iface.WidgetProcessor;
import sim.display.GUIState;

/**
 * finds actual strategies and place a button to change them on the fly
 * Created by carrknight on 6/8/15.
 */
public class StrategyWidgetProcessor implements WidgetProcessor<JComponent, SwingMetawidget> {

    /**
     * strategies require fishstate reference to be instantiated from the factory so we can only use this
     * when a GUISTate already exists. It is also important for syncing anyway
     */
    private final GUIState state;

    public StrategyWidgetProcessor(GUIState state) {
        this.state = state;
    }

    /**
     * Process the given widget. Called after a widget has been built by the
     * <code>WidgetBuilder</code>, and before it is added to the <code>Layout</code>.
     *
     * @param widget      the widget to process. Never null
     * @param elementName XML node name of the business field. Typically 'entity', 'property' or 'action'.
     *                    Never null
     * @param attributes  attributes of the widget to process. Never null. This Map is modifiable - changes
     *                    will be passed to subsequent WidgetProcessors and Layouts
     * @param metawidget  the parent Metawidget. Never null
     * @return generally the original widget (as passed in to the first argument). Can be a
     * different widget if the WidgetProcessor wishes to substitute the original widget for
     * another. Can be null if the WidgetProcessor wishes to cancel all further processing
     * of this widget (including laying out)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public JComponent processWidget(
            JComponent widget, String elementName, Map<String, String> attributes, SwingMetawidget metawidget) {

        if (!attributes.containsKey(StrategyInspector.KEY) || !(widget instanceof SwingMetawidget)) return widget;

        try {

            // contains the key
            final Object toModify =
                    DoubleParameterWidgetProcessor.getToInspectByTraversingMPath((SwingMetawidget) widget, metawidget);
            // get the super class
            Class superClass = Class.forName(attributes.get(StrategyInspector.KEY));
            // get current class (that's the name on the button)
            Class actualClass = PropertyUtils.getNestedProperty(toModify, attributes.get("name"))
                    .getClass();

            // add a button to change strategy
            JButton changeStrategy = new JButton(actualClass.getSimpleName());
            widget.add(changeStrategy);
            changeStrategy.addActionListener(e -> {
                // if possible, pause!

                StrategyFactoryDialog dialog = new StrategyFactoryDialog(superClass);
                int returned = JOptionPane.showOptionDialog(
                        widget,
                        dialog,
                        "Select New Strategy",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        null);
                // if ok was pressed on the dialog
                if (returned == JOptionPane.OK_OPTION) {
                    final Object newStrategy = dialog.getSelected().apply(state.state);
                    // use the beansutils to set the new value to the field
                    try {
                        synchronized (state.state.schedule) {
                            PropertyUtils.setSimpleProperty(
                                    // the object to modify
                                    toModify,
                                    // the name of the field
                                    attributes.get("name"),
                                    // the new value (table lookup)
                                    newStrategy);
                        }
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e1) {
                        System.err.print("failed to set new strategy! " + e1);
                        e1.printStackTrace();
                    }

                    // reb
                    metawidget.setToInspect(metawidget.getToInspect());
                }
            });

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Could not find key!");
            e.printStackTrace();
        }
        return widget;
    }
}
