/*
 * Copyright year Yuliyan Rusev - Inspix
 *
 * ColorNode.java is part of ColorSwapper.
 *
 * ColorSwapper is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * ColorSwapper is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see http://www.gnu.org/licenses/.
 *
 */

package inspix.colorswapper.Controls;

import inspix.colorswapper.Scenes.MainWindowController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableArray;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class ColorNode extends AnchorPane implements Initializable {

    private SimpleObjectProperty<Paint> destinationColor;
    private SimpleObjectProperty<Paint> originalColor;
    private SpinnerValueFactory.IntegerSpinnerValueFactory redSpinnerValue;
    private SpinnerValueFactory.IntegerSpinnerValueFactory greenSpinnerValue;
    private SpinnerValueFactory.IntegerSpinnerValueFactory blueSpinnerValue;
    private SpinnerValueFactory.DoubleSpinnerValueFactory hueSpinnerValue;
    private SpinnerValueFactory.DoubleSpinnerValueFactory saturationSpinnerValue;
    private SpinnerValueFactory.DoubleSpinnerValueFactory brightnessSpinnerValue;
    private SpinnerValueFactory.DoubleSpinnerValueFactory alphaSpinnerValue;
    private ArrayList<Point2D> pixels;
    private Image image;
    private MainWindowController controller;

    @FXML
    private TextField hexTextField;

    @FXML
    private Spinner<Integer> redSpinner, greenSpinner, blueSpinner;

    @FXML
    private Spinner<Double> alphaSpinner, hueSpinner, saturationSpinner, brightnessSpinner;

    @FXML
    private Rectangle originalColorRectangle, destinationColorRectangle;

    @FXML
    private Label countLabel, hexLabel;

    @FXML
    private Tab rgbTab, hsbTab, hexTab, colorPickerTab;

    @FXML
    private TabPane tabPane;

    @FXML
    private CheckBox liveUpdate;

    @FXML
    private Button resetButton, applyButton, findButton;

    @FXML
    private ColorPicker colorPicker;

    private boolean liveUpdateEnabled;

    public ColorNode() {
        originalColor = new SimpleObjectProperty<>(Color.BLACK);
        destinationColor = new SimpleObjectProperty<>(Color.BLACK);
        redSpinnerValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0, 1);
        greenSpinnerValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0, 1);
        blueSpinnerValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0, 1);
        alphaSpinnerValue = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, 1, 0.01);
        hueSpinnerValue = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 360, 0, 1);
        saturationSpinnerValue = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 1);
        brightnessSpinnerValue = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 1);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ColorNode.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ColorNode(Image image, MainWindowController controller) {
        this();
        this.controller = controller;
        this.pixels = new ArrayList<>();
        this.image = image;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        destinationColorRectangle.fillProperty().bindBidirectional(destinationColor);
        originalColorRectangle.fillProperty().bindBidirectional(originalColor);
        originalColorRectangle.setOnMouseClicked(e -> {
            setDestinationColor(originalColor.get());
            if (liveUpdate.isSelected()) {
                writePixels();
            }
        });

        colorPicker.setOnAction(e -> {
            setDestinationColor(colorPicker.getValue());
            colorPicker.requestFocus();
        });

        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            setDestinationColor(newValue);
            if (liveUpdateEnabled)
                writePixels();
        });
        setUpRectangles(originalColorRectangle, "Original Color");
        setUpRectangles(destinationColorRectangle, "Destination Color");
        setUpSpinners();
        setUpTabs();

        liveUpdate.setTooltip(new Tooltip("Will decrease the performance, redraws on any value change."));
    }

    private void setUpRectangles(Rectangle rect, String toolTipHeader) {
        rect.setStroke(Color.rgb(0, 0, 0, 0));
        rect.setOnMouseEntered(e -> rect.setEffect(new DropShadow(5, Color.LIMEGREEN)));
        rect.setOnMouseExited(e -> rect.setEffect(null));


        Tooltip tip = new Tooltip("");
        tip.setOnShowing(e -> {
            Color clr = (Color) rect.getFill();
            long r = Math.round(clr.getRed() * 255);
            long g = Math.round(clr.getGreen() * 255);
            long b = Math.round(clr.getBlue() * 255);
            long hue = Math.round(clr.getHue());
            DecimalFormat f = new DecimalFormat("#.##%");

            tip.setText(
                    String.format("%s\nRGB: %d / %d / %d\nHSB: %d / %s / %s\n HEX:%s",
                            toolTipHeader,
                            r,
                            g,
                            b,
                            hue,
                            f.format(clr.getSaturation()),
                            f.format(clr.getBrightness()),
                            clr.toString().toUpperCase()
                    )
            );
        });
        Tooltip.install(rect, tip);

    }

    private void setUpTabs() {
        rgbTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                repopulateRGB((Color) destinationColor.getValue());
        });
        hsbTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                repopulateHSB((Color) destinationColor.getValue());
        });
        hexTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                String hex = destinationColor.getValue().toString().toUpperCase();
                hexLabel.setText(hex);
                hexTextField.setText(hex);
            }

        });
        colorPickerTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                colorPicker.setValue((Color) getDestinationColor());
        });
    }

    private void setUpSpinners() {
        redSpinner.setValueFactory(redSpinnerValue);
        redSpinner.setEditable(true);
        setUpSpinnerScrollWeel(redSpinner);
        greenSpinner.setValueFactory(greenSpinnerValue);
        greenSpinner.setEditable(true);
        setUpSpinnerScrollWeel(greenSpinner);
        blueSpinner.setValueFactory(blueSpinnerValue);
        blueSpinner.setEditable(true);
        setUpSpinnerScrollWeel(blueSpinner);
        alphaSpinner.setValueFactory(alphaSpinnerValue);
        alphaSpinner.setEditable(true);
        setUpSpinnerScrollWeel(alphaSpinner);
        hueSpinner.setValueFactory(hueSpinnerValue);
        hueSpinner.setEditable(true);
        setUpSpinnerScrollWeel(hueSpinner);
        saturationSpinner.setValueFactory(saturationSpinnerValue);
        saturationSpinner.setEditable(true);
        setUpSpinnerScrollWeel(saturationSpinner);
        brightnessSpinner.setValueFactory(brightnessSpinnerValue);
        brightnessSpinner.setEditable(true);
        setUpSpinnerScrollWeel(brightnessSpinner);

        redSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            Color clr = (Color) destinationColor.getValue();
            destinationColor.set(new Color(
                    (double) validateValue(newValue, redSpinner.getEditor()) / 255,
                    clr.getGreen(),
                    clr.getBlue(),
                    clr.getOpacity()));
        });
        greenSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            Color clr = (Color) destinationColor.getValue();
            destinationColor.set(new Color(
                    clr.getRed(),
                    (double) validateValue(newValue, greenSpinner.getEditor()) / 255,
                    clr.getBlue(),
                    clr.getOpacity()));
        });
        blueSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            Color clr = (Color) destinationColor.getValue();
            destinationColor.set(new Color(
                    clr.getRed(),
                    clr.getGreen(),
                    (double) validateValue(newValue, blueSpinner.getEditor()) / 255,
                    clr.getOpacity()));
        });
        alphaSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            Color clr = (Color) destinationColor.getValue();
            double value = -1;
            if (newValue.matches("(\\d+.\\d+)"))
                value = Double.parseDouble(newValue);
            else
                return;
            if (value > 1) {
                value = 1;
            } else if (value < 0) {
                value = 0;
            }

            alphaSpinner.getEditor().setText(String.valueOf(value));
            destinationColor.set(new Color(
                    clr.getRed(),
                    clr.getGreen(),
                    clr.getBlue(),
                    value));

        });

        redSpinner.valueProperty().addListener((obj, o, n) -> {
            Color clr = (Color) destinationColor.getValue();
            Color result = new Color((double) n / 255, clr.getGreen(), clr.getBlue(), clr.getOpacity());
            destinationColor.set(result);
            if (liveUpdateEnabled)
                writePixels();
        });
        greenSpinner.valueProperty().addListener((obj, o, n) -> {
            Color clr = (Color) destinationColor.getValue();
            Color result = new Color(clr.getRed(), (double) n / 255, clr.getBlue(), clr.getOpacity());
            destinationColor.set(result);
            if (liveUpdateEnabled)
                writePixels();
        });
        blueSpinner.valueProperty().addListener((obj, o, n) -> {
            Color clr = (Color) destinationColor.getValue();
            Color result = new Color(clr.getRed(), clr.getGreen(), (double) n / 255, clr.getOpacity());
            destinationColor.set(result);
            if (liveUpdateEnabled)
                writePixels();
        });
        alphaSpinner.valueProperty().addListener((obj, o, n) -> {
            Color clr = (Color) destinationColor.getValue();
            Color result = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), n);
            destinationColor.set(result);
            if (liveUpdateEnabled)
                writePixels();
        });

        hueSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            Color clr = (Color) destinationColor.getValue();
            Color result = Color.hsb(newValue, clr.getSaturation(), clr.getBrightness(), clr.getOpacity());

            destinationColor.set(result);
            if (liveUpdateEnabled)
                writePixels();
        });

        saturationSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            Color clr = (Color) destinationColor.getValue();
            Color result = Color.hsb(clr.getHue(), newValue / 100, clr.getBrightness(), clr.getOpacity());

            destinationColor.set(result);
            if (liveUpdateEnabled)
                writePixels();
        });

        brightnessSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            Color clr = (Color) destinationColor.getValue();
            Color result = Color.hsb(clr.getHue(), clr.getSaturation(), newValue / 100, clr.getOpacity());

            destinationColor.set(result);
            if (liveUpdateEnabled)
                writePixels();

        });
    }

    private void setUpSpinnerScrollWeel(Spinner spinner) {
        spinner.setOnScroll(e -> {
            if (e.getDeltaY() > 0)
                spinner.increment();
            else
                spinner.decrement();
            e.consume();
        });
    }

    private void repopulateRGB(Color clr) {
        redSpinnerValue.setValue((int) (clr.getRed() * 255));
        greenSpinnerValue.setValue((int) (clr.getGreen() * 255));
        blueSpinnerValue.setValue((int) (clr.getBlue() * 255));
        alphaSpinnerValue.setValue(clr.getOpacity());
    }

    private void repopulateHSB(Color clr) {
        hueSpinnerValue.setValue(clr.getHue());
        saturationSpinnerValue.setValue(clr.getSaturation() * 100);
        brightnessSpinnerValue.setValue(clr.getBrightness() * 100);
    }

    @FXML
    public void resetChanges() {
        writePixels((Color) originalColor.getValue());
        liveUpdate.setSelected(false);
        liveUpdateEnabled = false;
    }

    @FXML
    private void onCheckBoxChange() {
        liveUpdateEnabled = liveUpdate.isSelected();
    }

    @FXML
    public void findPixels() {
        Color original = (Color) originalColor.get();
        findButton.setDisable(true);
        Task<ArrayList<Point2D>> task = new Task<ArrayList<Point2D>>() {
            @Override
            protected ArrayList<Point2D> call() throws Exception {
                ArrayList<Point2D> result = new ArrayList<>();
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        Color currentPixel = image.getPixelReader().getColor(x, y);
                        if (Objects.equals(original.toString(), currentPixel.toString())) {
                            result.add(new Point2D(x, y));
                            updateMessage(String.valueOf(result.size()));

                        }
                    }
                }
                return result;
            }
        };
        task.messageProperty().addListener((observable, oldValue, newValue) -> {
            countLabel.setText(newValue);
        });
        task.setOnSucceeded(e -> {
            countLabel.setEffect(new DropShadow(2, Color.LIME));
            pixels = task.getValue();

            liveUpdate.setDisable(false);
            applyButton.setDisable(false);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    public void writePixels() {
        Color destination = (Color) destinationColor.get();
        writePixels(destination);
        resetButton.setDisable(false);
    }

    private void writePixels(Color destination) {
        if (pixels.size() > 0)
            controller.writePixels(destination,pixels);
    }

    private int validateValue(String text, TextField editor) {
        int n = -1;
        if (text.matches("[0-9]+"))
            n = Integer.parseInt(text);
        if (n > 255) {
            n = 255;
        } else if (n < 0) {
            n = 0;
        }
        editor.setText(String.valueOf(n));
        return n;
    }

    public Paint getDestinationColor() {
        return destinationColor.get();
    }

    public SimpleObjectProperty<Paint> destinationColorProperty() {
        return destinationColor;
    }

    public void setDestinationColor(Paint destinationColor) {
        this.destinationColor.set(destinationColor);
    }

    public Paint getOriginalColor() {
        return originalColor.get();
    }

    public SimpleObjectProperty<Paint> originalColorProperty() {
        return originalColor;
    }

    public void setOriginalColor(Paint originalColor) {
        Color clr = (Color) originalColor;
        this.redSpinnerValue.setValue((int) (clr.getRed() * 255));
        this.greenSpinnerValue.setValue((int) (clr.getGreen() * 255));
        this.blueSpinnerValue.setValue((int) (clr.getBlue() * 255));
        this.alphaSpinnerValue.setValue(clr.getOpacity());
        this.originalColor.set(originalColor);
        this.destinationColor.set(originalColor);
    }

    public void addPixel(Point2D pixel) {
        pixels.add(pixel);
    }

    public void updatePixelCount() {
        countLabel.setText(String.valueOf(pixels.size()));
    }

    public void findButtonDisable(boolean value) {
        findButton.setDisable(value);
    }

    public void enableControlls() {
        applyButton.setDisable(false);
        liveUpdate.setDisable(false);
    }

    public ArrayList<Point2D> getPixels() {
        return pixels;
    }

    public void setPixels(ArrayList<Point2D> pixels) {
        if (pixels != null && pixels.size() > 0) {
            this.pixels = pixels;
            this.findButton.setDisable(true);
        }
    }

    public void changeTab(int index) {
        tabPane.getSelectionModel().select(index);
    }
}
