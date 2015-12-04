/*
 * Copyright year Yuliyan Rusev - Inspix
 *
 * ImageUtils.java is part of ColorSwapper.
 *
 *  ColorSwapper is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * ColorSwapper is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see http://www.gnu.org/licenses/.
 *
 */

package inspix.colorswapper.Utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Created by Inspix on 03/12/2015.
 */
public class ImageUtils {
    public static WritableImage resample(Image img, int magnitude) {
        WritableImage result = new WritableImage((int) img.getWidth() * magnitude, (int) img.getHeight() * magnitude);
        PixelReader r = img.getPixelReader();
        PixelWriter w = result.getPixelWriter();
        for (int ix = 0; ix < img.getWidth(); ix++) {
            for (int iy = 0; iy < img.getHeight(); iy++) {
                Color current = r.getColor(ix, iy);
                for (int nx = ix * magnitude; nx < ix * magnitude + magnitude; nx++) {
                    for (int ny = iy * magnitude; ny < iy * magnitude + magnitude; ny++) {
                        w.setColor(nx, ny, current);
                    }
                }
            }
        }
        return result;
    }
}
