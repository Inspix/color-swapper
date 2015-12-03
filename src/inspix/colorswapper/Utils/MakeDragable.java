/*
 * Copyright year Yuliyan Rusev - Inspix
 *
 * MakeDraggable.java is part of ColorSwapper.
 *
 *  ColorSwapper is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * ColorSwapper is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see http://www.gnu.org/licenses/.
 *
 */

package inspix.colorswapper.Utils;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

public class MakeDragable {

    public enum Sides {
        ALL,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        LEFTRIGHT,
        TOPBOTTOM
    }

    protected Sides sideSelected;
    protected static final int MARGIN = 2;
    protected boolean minWidthInit,minHeightInit;
    protected boolean isDragging = false;
    protected double previous;
    protected final Region region;
    protected Sides sides;


    protected MakeDragable(Region region, Sides sides) {
        this.region = region;
        this.sides = sides;
    }

    public static void makeDragable(Region region, Sides sides) {
        final MakeDragable resizer = new MakeDragable(region, sides);
        region.setOnMousePressed(resizer::mousePressed);
        region.setOnMouseReleased(resizer::mouseReleased);
        region.setOnMouseDragged(resizer::mouseDragged);
        region.setOnMouseMoved(resizer::mouseOver);
    }


    protected void mouseReleased(MouseEvent event) {
        isDragging = false;
        sideSelected = null;
        this.region.setCursor(Cursor.DEFAULT);
    }

    protected void mouseDragged(MouseEvent event) {
        if (!isDragging) return;

        boolean horizontal = sideSelected == Sides.LEFTRIGHT;

        double current = horizontal ? event.getX() : event.getY();
        if (horizontal){
            double size = region.getWidth() + (previous - current);
            if(size >= region.getMaxWidth()){
                size = region.getMaxWidth();
            }
            region.setMinWidth(size);

        }

        else
            region.setMinWidth(region.getHeight() + (previous - current));

        previous = current;
    }

    protected void mousePressed(MouseEvent event) {
        if (!isOnTheEdge(event)) return;
        isDragging = true;


        if (!minWidthInit && sideSelected == Sides.LEFTRIGHT) {
            region.setMinWidth(region.getWidth());
            minWidthInit = true;
        }
        if (!minHeightInit && sideSelected == Sides.TOPBOTTOM) {
            region.setMinHeight(region.getHeight());
            minHeightInit = true;
        }
        previous = sideSelected == Sides.LEFTRIGHT ? event.getX() : event.getY();
    }

    protected void mouseOver(MouseEvent event) {
        if (isOnTheEdge(event) || isDragging) {
            if (sideSelected == Sides.LEFTRIGHT)
                region.setCursor(Cursor.H_RESIZE);
            else
                region.setCursor(Cursor.V_RESIZE);
        } else {
            region.setCursor(Cursor.DEFAULT);
        }
    }

    protected boolean isOnTheEdge(MouseEvent event) {
        switch (sides) {

            case ALL:
                return checkLeft(event) || checkRight(event) || checkTop(event) || checkBottom(event);
            case LEFT:
                return checkLeft(event);
            case RIGHT:
                return checkRight(event);
            case TOP:
                return checkTop(event);
            case BOTTOM:
                return checkBottom(event);
            case LEFTRIGHT:
                return checkLeft(event) || checkRight(event);
            case TOPBOTTOM:
                return checkTop(event) || checkBottom(event);
            default:
                return false;
        }
    }

    protected boolean checkTop(MouseEvent e) {
        boolean result = e.getY() - MARGIN <= 0 && e.getY() + MARGIN >= 0;
        if (result)
            sideSelected = Sides.TOPBOTTOM;
        return result;
    }

    protected boolean checkBottom(MouseEvent e) {
        boolean result = e.getY() - MARGIN <= region.getHeight() && e.getY() + MARGIN >= region.getHeight();
        if (result)
            sideSelected = Sides.TOPBOTTOM;
        return result;
    }

    protected boolean checkRight(MouseEvent e) {
        boolean result = e.getX() > region.getWidth() - MARGIN && e.getX() < region.getWidth() + MARGIN;
        if (result)
            sideSelected = Sides.LEFTRIGHT;
        return result;
    }

    protected boolean checkLeft(MouseEvent e) {
        boolean result = e.getX() > -MARGIN && e.getX() < MARGIN;
        if (result)
            sideSelected = Sides.LEFTRIGHT;
        return result;
    }
}
