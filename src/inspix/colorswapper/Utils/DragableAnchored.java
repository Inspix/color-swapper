/*
 * Copyright year Yuliyan Rusev - Inspix
 *
 * DragableAnchored.java is part of ColorSwapper.
 *
 *  ColorSwapper is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * ColorSwapper is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see http://www.gnu.org/licenses/.
 *
 */

package inspix.colorswapper.Utils;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import sun.plugin.javascript.navig.Anchor;

/**
 * Created by Inspix on 03/12/2015.
 */
public class DragableAnchored extends MakeDragable{
    private DragableAnchored(Region region, Sides sides) {
        super(region, sides);
        region.setOnMousePressed(this::mousePressed);
        region.setOnMouseReleased(this::mouseReleased);
        region.setOnMouseDragged(this::mouseDragged);
        region.setOnMouseMoved(this::mouseOver);
    }

    public static void makeDragable(Region region,Sides sides){
        new  DragableAnchored(region,sides);
    }

    @Override
    protected void mousePressed(MouseEvent event) {
        if (!isOnTheEdge(event)) return;

        isDragging = true;

        System.out.println("Overload");
        previous = sideSelected == Sides.LEFTRIGHT ? event.getX() : event.getY();

    }

    @Override
    protected void mouseDragged(MouseEvent event) {
        System.out.println("Before is dragging");
        if (!isDragging) return;

        boolean horizontal = sideSelected == Sides.LEFTRIGHT;
        System.out.println("Dragable acnhortedsa");

        double current = horizontal ? event.getX() : event.getY();
        double newSize = (horizontal ? AnchorPane.getLeftAnchor(region) : AnchorPane.getTopAnchor(region)) + (current - previous);

        if (horizontal)
            AnchorPane.setLeftAnchor(region,newSize);
        else
            AnchorPane.setTopAnchor(region,newSize);

        previous = current;
    }
}
