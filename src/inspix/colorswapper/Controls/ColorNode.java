package inspix.colorswapper.Controls;

import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Created by Inspix on 30/11/2015.
 */
public class ColorNode extends AnchorPane implements Initializable {

    private SimpleObjectProperty<Paint> destinationColor;
    private SimpleObjectProperty<Paint> originalColor;
    private SpinnerValueFactory.IntegerSpinnerValueFactory redSpinnerValue;
    private SpinnerValueFactory.IntegerSpinnerValueFactory greenSpinnerValue;
    private SpinnerValueFactory.IntegerSpinnerValueFactory blueSpinnerValue;
    private SpinnerValueFactory.DoubleSpinnerValueFactory alphaSpinnerValue;
    private ArrayList<Point2D> pixels;
    private WritableImage image;

    @FXML
    private Spinner<Integer> redSpinner, greenSpinner, blueSpinner;

    @FXML
    private Spinner<Double> alphaSpinner;

    @FXML
    private Rectangle originalColorRectangle, destinationColorRectangle;

    @FXML
    Label countLabel;

    public ColorNode() {
        originalColor = new SimpleObjectProperty<>(Color.BLACK);
        destinationColor = new SimpleObjectProperty<>(Color.BLACK);
        redSpinnerValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0, 1);
        greenSpinnerValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0, 1);
        blueSpinnerValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0, 1);
        alphaSpinnerValue = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, 1, 0.01);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ColorNode.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ColorNode(WritableImage image){
        this();
        this.pixels = new ArrayList<>();
        this.image = image;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        destinationColorRectangle.fillProperty().bindBidirectional(destinationColor);
        originalColorRectangle.fillProperty().bindBidirectional(originalColor);
        setUpSpinners();
    }

    private void setUpSpinners() {
        redSpinner.setValueFactory(redSpinnerValue);
        redSpinner.setEditable(true);
        greenSpinner.setValueFactory(greenSpinnerValue);
        greenSpinner.setEditable(true);
        blueSpinner.setValueFactory(blueSpinnerValue);
        blueSpinner.setEditable(true);
        alphaSpinner.setValueFactory(alphaSpinnerValue);
        alphaSpinner.setEditable(true);

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
            if (newValue.matches("(0\\.[0-9]+)|(1\\.0)"))
                value = Double.parseDouble(newValue);


            alphaSpinner.getEditor().setText(String.valueOf(value));
            destinationColor.set(new Color(
                    clr.getRed(),
                    clr.getGreen(),
                    clr.getBlue(),
                    value));
        });


        redSpinner.valueProperty().addListener((obj, o, n) -> {
            Color clr = (Color) destinationColor.getValue();
            destinationColor.set(new Color((double) n / 255, clr.getGreen(), clr.getBlue(), clr.getOpacity()));
        });
        greenSpinner.valueProperty().addListener((obj, o, n) -> {
            Color clr = (Color) destinationColor.getValue();
            System.out.println(clr);
            destinationColor.set(new Color(clr.getRed(), (double) n / 255, clr.getBlue(), clr.getOpacity()));
            System.out.println(destinationColor.getValue());
        });
        blueSpinner.valueProperty().addListener((obj, o, n) -> {
            Color clr = (Color) destinationColor.getValue();
            destinationColor.set(new Color(clr.getRed(), clr.getGreen(), (double) n / 255, clr.getOpacity()));
        });
        alphaSpinner.valueProperty().addListener((obj, o, n) -> {
            Color clr = (Color) destinationColor.getValue();
            destinationColor.set(new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), n));
        });
    }

    @FXML
    public void findPixels(){
        Color original = (Color)originalColor.get();

        Task<ArrayList<Point2D>> task = new Task<ArrayList<Point2D>>() {
            @Override
            protected ArrayList<Point2D> call() throws Exception {
                ArrayList<Point2D> result = new ArrayList<>();
                for (int x = 0; x < image.getWidth(); x++){
                    for (int y = 0; y < image.getHeight(); y++) {
                        Color currentPixel = image.getPixelReader().getColor(x,y);
                        if (Objects.equals(original.toString(), currentPixel.toString())){
                            result.add(new Point2D(x,y));
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
            countLabel.setEffect(new DropShadow(2,Color.LIME));
            pixels = task.getValue();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    public void writePixels(){
        Color destination = (Color)destinationColor.get();
        if (pixels.size() > 0)
        for (int i = 0; i < pixels.size(); i++) {
            Point2D pixel = pixels.get(i);
            image.getPixelWriter().setColor((int)pixel.getX(),(int)pixel.getY(),destination);
        }
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

    public ArrayList<Point2D> getPixels() {
        return pixels;
    }
}
