/*
 * Copyright year Yuliyan Rusev - Inspix
 *
 * Toast.java is part of ColorSwapper.
 *
 *  ColorSwapper is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * ColorSwapper is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see http://www.gnu.org/licenses/.
 *
 */

package inspix.colorswapper.Utils;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class Toast {
    private static boolean borderUpdated, backgroundUpdated;
    public static final double DURATION_SHORT = 2;
    public static final double DURATION_LONG = 5;
    private static Insets padding = new Insets(5);
    private static CornerRadii cornerRadii = new CornerRadii(10);
    private static Color backgroundColor = Color.GRAY;
    private static Color textColor = Color.BLACK;
    private static Color borderColor = Color.GRAY;
    private static BorderStroke borderStroke = new BorderStroke(borderColor, BorderStrokeStyle.SOLID, cornerRadii, BorderWidths.DEFAULT);
    private static Border border = new Border(borderStroke);
    private static BackgroundFill backgroundFill = new BackgroundFill(backgroundColor, cornerRadii, Insets.EMPTY);
    private static Background background = new Background(backgroundFill);
    private static Font font = Font.font("Calibri", FontWeight.BOLD, 20);
    private Label label;

    private Toast(AnchorPane parent, double duration, String text) {
        label = new Label(text);
        label.setTextFill(textColor);
        label.setPadding(padding);
        label.setBorder(border);
        label.setBackground(background);
        label.setFont(font);
        label.setEffect(new DropShadow(2, 2, 2, Color.SLATEGRAY));
        AnchorPane.setBottomAnchor(label, 5d);
        AnchorPane.setLeftAnchor(label, parent.getWidth() / 2.5);
        parent.getChildren().add(label);
        Animation fadeOut = createAnimation(duration, false);
        fadeOut.setOnFinished(e -> parent.getChildren().remove(label));
        fadeOut.play();
    }


    public static void create(AnchorPane parent, double duration, String text) {
        new Toast(parent, duration, text);
    }

    private Animation createAnimation(double duration, boolean fadeIn) {
        return new Transition() {
            {
                setDelay(Duration.seconds(duration));
                setCycleDuration(Duration.seconds(1));
            }

            @Override
            protected void interpolate(double frac) {
                label.setOpacity(fadeIn ? frac : 1 - frac);
            }
        };
    }

    public static void applyChanges() {
        if (borderUpdated) {
            borderStroke = new BorderStroke(borderColor, BorderStrokeStyle.SOLID, cornerRadii, BorderWidths.DEFAULT);
            border = new Border(borderStroke);
            borderUpdated = false;

        }
        if (backgroundUpdated) {
            backgroundFill = new BackgroundFill(backgroundColor, cornerRadii, Insets.EMPTY);
            background = new Background(backgroundFill);
            backgroundUpdated = false;
        }
    }

    public static void setPadding(Insets padding) {
        Toast.padding = padding;
    }

    public static void setCornerRadii(CornerRadii cornerRadii) {
        backgroundUpdated = true;
        borderUpdated = true;
        Toast.cornerRadii = cornerRadii;
    }

    public static void setBackgroundColor(Color backgroundColor) {
        backgroundUpdated = true;
        Toast.backgroundColor = backgroundColor;
    }

    public static void setTextColor(Color textColor) {
        Toast.textColor = textColor;
    }

    public static void setBorderColor(Color borderColor) {
        borderUpdated = true;
        Toast.borderColor = borderColor;
    }

    public static void setBorderStroke(BorderStroke borderStroke) {
        borderUpdated = true;
        Toast.borderStroke = borderStroke;
    }

    public static void setBorder(Border border) {
        Toast.border = border;
    }

    public static void setBackgroundFill(BackgroundFill backgroundFill) {
        backgroundUpdated = true;
        Toast.backgroundFill = backgroundFill;
    }

    public static void setBackground(Background background) {
        Toast.background = background;
    }

    public static void setFont(Font font) {
        Toast.font = font;
    }
}
