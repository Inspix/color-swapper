/*
 * Copyright year Yuliyan Rusev - Inspix
 *
 * SlicerController.java is part of ColorSwapper.
 *
 *  ColorSwapper is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * ColorSwapper is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see http://www.gnu.org/licenses/.
 *
 */

package inspix.colorswapper.Scenes;

import com.sun.javafx.event.EventDispatchChainImpl;
import inspix.colorswapper.Utils.ImageUtils;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SlicerController implements Initializable {

    @FXML
    ImageView imageView;
    @FXML
    Canvas canvas;
    @FXML
    StackPane stackPane;
    @FXML
    VBox parent;
    @FXML
    ToggleButton tbtnGrid;
    @FXML
    Spinner<Integer> spinnerX, spinnerY, spinnerW, spinnerH;


    private SpinnerValueFactory<Integer> xValue, yValue, wValue, hValue;

    private Image image;
    private GraphicsContext g;
    private WritableImage writableImage;
    private Rectangle2D viewport;
    private FileChooser fileChooser = new FileChooser();
    private File imageFile;
    private Stage stage;
    private Color lineColor;

    private double resampleFactor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lineColor = Color.rgb(0, 0, 0, 0.1);
        stackPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        setUpImageView();
        EventDispatchChainImpl chain = new EventDispatchChainImpl();
        chain.append(canvas.getEventDispatcher());
        chain.append(imageView.getEventDispatcher());
        stackPane.addEventHandler(MouseEvent.ANY, chain::dispatchEvent);
        stackPane.addEventHandler(ScrollEvent.ANY, chain::dispatchEvent);

        viewport = imageView.getViewport();
        g = canvas.getGraphicsContext2D();
    }

    private void setUpSpinners() {
        if (image == null)
            return;
        int wmax = (int) image.getWidth();
        int hmax = (int) image.getHeight();
        xValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, wmax);
        yValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, hmax);
        wValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, wmax);
        hValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, hmax);
        spinnerX.setValueFactory(xValue);
        spinnerY.setValueFactory(yValue);
        spinnerW.setValueFactory(wValue);
        spinnerH.setValueFactory(hValue);
        spinnerX.setDisable(false);
        spinnerY.setDisable(false);
        spinnerW.setDisable(false);
        spinnerH.setDisable(false);
        spinnerX.valueProperty().addListener(this::onSpinnerChange);
        spinnerY.valueProperty().addListener(this::onSpinnerChange);
        spinnerW.valueProperty().addListener(this::onSpinnerChange);
        spinnerH.valueProperty().addListener(this::onSpinnerChange);
    }

    private void onSpinnerChange(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
        onCutMenuAction();
    }

    @FXML
    void onOpenMenuAction() {
        if (imageFile != null)
            fileChooser.setInitialDirectory(imageFile.getParentFile());
        imageFile = fileChooser.showOpenDialog(stage);
        if (imageFile == null)
            return;
        try (FileInputStream is = new FileInputStream(imageFile)) {
            image = new Image(is);
            writableImage = ImageUtils.resample(image, 4);
            resampleFactor = 4;

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (image != null) {
            imageView.setSmooth(true);
            imageView.setDisable(false);
            imageView.setImage(writableImage);
            setUpSpinners();
        }
    }

    @FXML
    void onTBtnGridAction() {
        onCutMenuAction();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {

            if (event.getCode() == KeyCode.RIGHT)
                xValue.setValue(xValue.getValue() + 1);
            else if (event.getCode() == KeyCode.LEFT)
                xValue.setValue(xValue.getValue() - 1);
            else if (event.getCode() == KeyCode.DOWN)
                yValue.setValue(yValue.getValue() + 1);
            else if (event.getCode() == KeyCode.UP)
                yValue.setValue(yValue.getValue() - 1);
            onCutMenuAction();
        });
    }

    @FXML
    void onCutMenuAction() {
        viewport = imageView.getViewport();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (!tbtnGrid.isSelected() || hValue.getValue() <= 0 || wValue.getValue() <= 0)
            return;
        g.setLineWidth(1);
        g.setStroke(lineColor);

        double totalW = image.getWidth();
        double totalH = image.getHeight();

        double w = wValue.getValue();
        double h = hValue.getValue();
        double x = xValue.getValue() % w;
        double y = yValue.getValue() % h;

        double xBalance = w / h;
        double yBalance = h / w;

        int partsX = (int) Math.ceil((totalH / w) * xBalance);
        int partsY = (int) Math.ceil((totalW / h) * yBalance);

        for (int i = 0; i < partsX; i++) {
            drawLine(
                    0,
                    i * h + y,
                    totalW,
                    i * h + y);
        }
        for (int i = 0; i < partsY; i++) {
            drawLine(
                    i * w + x,
                    0
                    , i * w + x,
                    totalH);
        }
        g.setLineWidth(2);
        g.setFill(Color.BURLYWOOD);
        drawBorder();

    }

    private void drawLine(double x, double y, double x2, double y2) {
        g.strokeLine(
                ((viewport.getMinX() / resampleFactor) * scale * -1) + x * scale,
                ((viewport.getMinY() / resampleFactor) * scale * -1) + y * scale,
                ((viewport.getMinX() / resampleFactor) * scale * -1) + x2 * scale,
                ((viewport.getMinY() / resampleFactor) * scale * -1) + y2 * scale);
    }

    private void drawBorder() {
        g.strokeRect(viewport.getMinX() / resampleFactor * scale * -1, viewport.getMinY() / resampleFactor * scale * -1, image.getWidth() * scale, image.getHeight() * scale);
    }

    private double currentX, currentY, scale = 1, distanceX, distanceY;
    private boolean dragged;

    @FXML
    void setUpImageView() {
        imageView.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            currentX = e.getX() / scale;
            currentY = e.getY() / scale;
            distanceX = imageView.getViewport().getMinX() / resampleFactor + currentX;
            distanceY = imageView.getViewport().getMinY() / resampleFactor + currentY;

        });

        imageView.setOnMouseDragged(e -> {
            dragged = true;
            imageView.setViewport(
                    new Rectangle2D(
                            (distanceX - e.getX() / scale) * resampleFactor,
                            (distanceY - e.getY() / scale) * resampleFactor,
                            (imageView.getFitWidth() / scale) * resampleFactor,
                            (imageView.getFitHeight() / scale) * resampleFactor)
            );
            onCutMenuAction();
        });

        imageView.setOnScroll(e -> {
            scale += e.getDeltaY() > 0 ? 0.1 : (-0.1);
            if (scale > 15)
                scale = 15;
            if (scale <= 0.001) {
                scale = 0.01;
            }

            imageView.setViewport(new Rectangle2D(imageView.getViewport().getMinX(), imageView.getViewport().getMinY(), (imageView.getFitWidth() / scale) * resampleFactor, (imageView.getFitHeight() / scale) * resampleFactor));

            onCutMenuAction();
        });

        imageView.setOnMouseClicked(e -> {
            if (dragged) {
                dragged = false;
            }
        });

    }
}
