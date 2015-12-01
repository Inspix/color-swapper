/*
 * Copyright year Yuliyan Rusev - Inspix
 *
 * ColorHelpers.java is part of ColorSwapper.
 *
 *  ColorSwapper is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * ColorSwapper is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see http://www.gnu.org/licenses/.
 *
 */

package inspix.colorswapper.Utils;

import javafx.scene.paint.Color;

import java.util.Comparator;

public class ColorHelpers {

    public static Comparator<Color> hsbComparator = (o1, o2) -> {

        double step = 8;

        double lum1 = Math.sqrt(.241 * o1.getRed() + .691 * o1.getGreen() + .068 * o1.getBlue());
        double lum2 = Math.sqrt(.241 * o2.getRed() + .691 * o2.getGreen() + .068 * o2.getBlue());

        double[] hsv1 = ColorHelpers.RGBtoHSB(o1.getRed(), o1.getGreen(), o1.getBlue());
        double[] hsv2 = ColorHelpers.RGBtoHSB(o2.getRed(), o2.getGreen(), o2.getBlue());


        double res1h = hsv1[0] * step;
        double res1s = lum1 * step;
        double res1b = hsv1[2] * step;

        double res2h = hsv2[0] * step;
        double res2s = lum2 * step;
        double res2b = hsv2[2] * step;

        if (res1h != res2h) {
            return res1h < res2h ? -1 : 1;
        }
        if (res1s != res2s) {
            return res1s < res2s ? -1 : 1;
        }
        if (res1b != res2b) {
            return res1b < res2b ? -1 : 1;
        }
        return 0;
    };

    public static Comparator<Color> rgbComparator = ((o1, o2) -> {

        double r1 = o1.getRed();
        double r2 = o2.getRed();
        double g1 = o1.getGreen();
        double g2 = o2.getGreen();
        double b1 = o1.getBlue();
        double b2 = o2.getBlue();

        if (r1 != r2) {
            return r1 < r2 ? -1 : 1;
        }
        if (g1 != g2) {
            return g1 < g2 ? -1 : 1;
        }
        if (b1 != b2) {
            return b1 < b2 ? -1 : 1;
        }
        return 0;
    });

    public static int compareRGB(Color color1, Color color2) {
        return rgbComparator.compare(color1, color2);
    }

    public static int compareHSB(Color color1, Color color2) {
        return hsbComparator.compare(color1, color2);
    }

    public static double[] RGBtoHSB(double r, double g, double b) {

        double[] hsb = new double[3];

        double min = r < g ? r : g;
        min = min < b ? min : b;

        double max = r > g ? r : g;
        max = max > b ? max : b;

        hsb[2] = max;
        double delta = max - min;
        if (delta < 0.00000001) {
            hsb[0] = 0;
            hsb[1] = 0;
            return hsb;
        }

        if (max >= 0) {
            hsb[1] = delta / max;
        } else {
            hsb[0] = Double.NaN;
            hsb[1] = 0;
            return hsb;
        }

        if (r == max) {
            hsb[0] = (g - b) / delta;
        } else if (g == max) {
            hsb[0] = 2 + (b - r) / delta;
        } else {
            hsb[0] = 4 + (r - g) / delta;
        }

        hsb[0] *= 60;

        if (hsb[0] < 0) {
            hsb[0] += 360;
        }

        return hsb;
    }

    public static double[] RGBtoHSB(Color clr) {
        return RGBtoHSB(clr.getRed(), clr.getGreen(), clr.getBlue());
    }

}

