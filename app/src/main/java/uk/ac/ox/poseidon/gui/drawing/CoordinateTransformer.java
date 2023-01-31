/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.poseidon.gui.drawing;

import com.vividsolutions.jts.geom.Point;
import sim.display.Display2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

/**
 * A utility to turn pixel coordinates into model coordinates. Should really be default but MASON gui hides this somewhere
 * deep in the hitOrDraw method. Why do they hide it in such a weird method? Screw you, that's why.
 * It's either this or lugging around GUI objects of all kinds just to create a DrawInfo2D, so you can get weird LocationWrappers
 * to get Portrayals with which you can then access to get Model fields. And if that makes any sense to you, then
 * you'll like MASON. If that makes no sense to you, use this.
 * Created by carrknight on 7/20/15.
 */
public class CoordinateTransformer {


    /**
     * the display2d draws objects
     */
    private final Display2D display;

    /**
     * the nautical map is the model map
     */
    private final NauticalMap map;


    public CoordinateTransformer(Display2D mapDisplay, NauticalMap map) {
        this.display = mapDisplay;
        this.map = map;

    }


    /**
     * click somewhere on the display2D and you will get an event.x and event.y; Plug these in and you will
     * get the seatile of where you clicked
     *
     * @param guiX the x of the mouseclick event
     * @param guiY the y of the mouseclick event
     * @return the seatile you clicked on
     */
    public SeaTile cellHere(double guiX, double guiY) {


        final Int2D position = guiToGridPosition(guiX, guiY);
        return map.getSeaTile(position.x, position.y);


    }

    /**
     * click somewhere on the display2D and you will get an event.x and event.y; Plug these in and you will
     * get the grid cell location touched
     */
    public Int2D guiToGridPosition(double guiX, double guiY) {
        //create the transformer
        AffineTransform transform = new AffineTransform();
        transform.translate(
            display.getOffset().getX(),
            display.getOffset().getY()
        );
        transform.scale(display.getScale(), display.getScale());
        //invert it, which is easy.
        try {
            transform.invert();
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
            System.err.println("failure to transform");
        }

        //get proper X and y
        final Point2D transformed = transform.transform(new Point2D.Double(guiX, guiY), null);

        //now get the ratios of widths/heights; notice that we need to grab the inside display because
        //display2d can be resized at any point
        double widthRatio = display.insideDisplay.width / map.getWidth();
        double heightRatio = display.insideDisplay.height / map.getHeight();


        int xCell = (int) (transformed.getX() / widthRatio);
        int yCell = (int) (transformed.getY() / heightRatio);
        return new Int2D(xCell, yCell);
    }


    /**
     * transforms a click into a JTS point. JTS is a continuous space chart that GeoMason projects the real world coordinates to.
     * This transformer is very imprecise and simply returns the center of the seatile clicked rather than the correct mouse location
     *
     * @param guiX the x of the mouseclick event
     * @param guiY the y of the mouseclick event
     * @return the JTS point representing the center of the tile clicked
     */
    public Point guiToJTSPoint(double guiX, double guiY) {

        return gridToJTSPoint(guiToGridPosition(guiX, guiY));
    }


    public Point gridToJTSPoint(int x, int y) {
        return map.getRasterBathymetry().toPoint(x, y);
    }

    public Point gridToJTSPoint(Int2D gridPosition) {
        return gridToJTSPoint(gridPosition.getX(), gridPosition.getY());
    }

    /**
     * return how much in JTS ( the continuous space chart that GeoMason projects the real world coordinates)
     * a cell is wide
     *
     * @return
     */
    public int getCellWidthInJTS() {
        return (int) map.getRasterBathymetry().getPixelWidth();
    }

    public int getCellHeightInJTS() {
        return (int) map.getRasterBathymetry().getPixelHeight();

    }

}


