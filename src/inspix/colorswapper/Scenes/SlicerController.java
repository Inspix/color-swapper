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
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
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

import javax.imageio.ImageIO;
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

    @FXML
    MenuItem menuSave, menuSaveAs, menuSliceToParts;


    private SpinnerValueFactory<Integer> xValue, yValue, wValue, hValue;

    private Image image;
    private GraphicsContext g;
    private WritableImage writableImage;
    private Rectangle2D viewport;
    private FileChooser fileChooser = new FileChooser();
    private File imageFile;
    private Stage stage;
    private Color lineColor;
    private Color exportLineColor;
    private int gap;

    private double resampleFactor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resampleFactor = 4;
        gap = 2;
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
        FileChooser.ExtensionFilter png = new FileChooser.ExtensionFilter("Portable Network Graphics", "*.png");
        fileChooser.getExtensionFilters().add(png);
        fileChooser.setSelectedExtensionFilter(png);
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
        spinnerX.setDisable(true);
        spinnerY.setDisable(true);
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

    private WritableImage slice(File file) {
        boolean writeToFile = false;
        if (file != null)
            writeToFile = true;
        int currentPart = 0;
        exportLineColor = Color.rgb(247, 119, 249, 1);

        double totalW = image.getWidth();
        double totalH = image.getHeight();
        if (gap == 0)
            gap = 1;
        double w = wValue.getValue();
        double h = hValue.getValue();
        double x = xValue.getValue() % w;
        double y = yValue.getValue() % h;

        int partsW = (int) Math.ceil((totalW / w));
        int partsH = (int) Math.ceil((totalH / h));
        int gapW = partsW * gap;
        int gapH = partsH * gap;
        int px, py;
        if (x != 0) {
            px = -1;
            gapW += gap;
        } else {
            px = 0;
        }
        if (y != 0) {
            py = -1;
            gapH += gap;
        } else {
            py = 0;
        }
        int offsetX = 0;
        int offsetY = 0;


        WritableImage result = new WritableImage((int) image.getWidth() + gapW, (int) image.getHeight() + gapH);
        int modX = (int) image.getWidth() % (int) w;
        int modY = (int) image.getHeight() % (int) h;

        for (int partY = py; partY < partsH; partY++) {
            int cpartY = (partY == -1 ? 0 : partY);
            int ch = (int) h;
            int cy = cpartY * ch + (int) y;

            if (partY == -1) {
                ch = (int) y;
                cy = 0;
            } else if (partY == partsH - 1) {
                if (modY == 0)
                    ch = (int) h - (int) y;
                else
                    ch = modY - (int) y;
            }
            for (int partX = px; partX < partsW; partX++) {
                int cpartX = (partX == -1 ? 0 : partX);
                int cw = (int) w;
                int cx = cpartX * cw + (int) x;
                if (partX == -1) {
                    cw = (int) x;
                    cx = 0;
                } else if (partX == partsW - 1) {
                    if (modX == 0)
                        cw = (int) w - (int) x;
                    else
                        cw = modX - (int) x;
                }
                if (writeToFile) {

                    currentPart++;
                    WritableImage img = new WritableImage(image.getPixelReader(), cx, cy, cw, ch);
                    String filename = file.getName().replace(".png", currentPart + ".png");
                    File output = new File(file.getParentFile(), filename);
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(img, null),
                                "png", output);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                    continue;
                }

                result.getPixelWriter().setPixels(cx + offsetX, cy + offsetY, cw, ch, image.getPixelReader(), cx, cy);
                for (int i = 0; i < ch; i++) {
                    for (int j = 0; j < gap; j++) {
                        int lx = cx + cw + offsetX + j;
                        int ly = cy + i + offsetY;
                        if (ly < result.getHeight() && lx < result.getWidth())
                            result.getPixelWriter().setColor(lx, ly, exportLineColor);
                    }
                }
                for (int i = 0; i < cw + 2; i++) {
                    for (int j = 0; j < gap; j++) {
                        int lx = cx + i + offsetX;
                        int ly = cy + ch + offsetY + j;
                        if (ly < result.getHeight() && lx < result.getWidth())
                            result.getPixelWriter().setColor(lx, ly, exportLineColor);
                    }
                }
                offsetX += gap;

            }
            offsetY += gap;
            offsetX = 0;
        }

        return result;
    }

    /* X > Y
    private WritableImage slice(File file) {
        boolean writeToFile = false;
        if (file != null)
            writeToFile = true;
        int currentPart = 0;
        exportLineColor = Color.rgb(247, 119, 249, 1);

        double totalW = image.getWidth();
        double totalH = image.getHeight();
        if (gap == 0)
            gap = 1;
        double w = wValue.getValue();
        double h = hValue.getValue();
        double x = xValue.getValue() % w;
        double y = yValue.getValue() % h;

        int partsW = (int) Math.ceil((totalW / w));
        int partsH = (int) Math.ceil((totalH / h));
        int gapW = partsW * gap;
        int gapH = partsH * gap;
        int px, py;
        if (x != 0) {
            px = -1;
            gapW += gap;
        } else {
            px = 0;
        }
        if (y != 0) {
            py = -1;
            gapH += gap;
        } else {
            py = 0;
        }
        int offsetX = 0;
        int offsetY = 0;


        WritableImage result = new WritableImage((int) image.getWidth() + gapW, (int) image.getHeight() + gapH);
        int modX = (int) image.getWidth() % (int) w;
        int modY = (int) image.getHeight() % (int) h;

        for (int partX = px; partX < partsW; partX++) {
            int cpartX = (partX == -1 ? 0 : partX);
            int cw = (int) w;
            int cx = cpartX * cw + (int) x;
            if (partX == -1) {
                cw = (int) x;
                cx = 0;
            } else if (partX == partsW - 1) {
                if (modX == 0)
                    cw = (int) w - (int) x;
                else
                    cw = modX - (int) x;
            }
            for (int partY = py; partY < partsH; partY++) {
                int cpartY = (partY == -1 ? 0 : partY);
                int ch = (int) h;
                int cy = cpartY * ch + (int) y;

                if (partY == -1) {
                    ch = (int) y;
                    cy = 0;
                } else if (partY == partsH - 1) {
                    if (modY == 0)
                        ch = (int) h - (int) y;
                    else
                        ch = modY - (int) y;
                }

                if (writeToFile){

                    currentPart++;
                    WritableImage img = new WritableImage(image.getPixelReader(),cx,cy,cw,ch);
                    String filename = file.getName().replace(".png",currentPart + ".png");
                    File output = new File(file.getParentFile(),filename);
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(img, null),
                                "png", output);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                    continue;
                }

                result.getPixelWriter().setPixels(cx + offsetX, cy + offsetY, cw, ch, image.getPixelReader(), cx, cy);
                for (int i = 0; i < ch; i++) {
                    for (int j = 0; j < gap; j++) {
                        int lx = cx + cw + offsetX + j;
                        int ly = cy + i + offsetY;
                        if (ly < result.getHeight() && lx < result.getWidth())
                            result.getPixelWriter().setColor(lx, ly, exportLineColor);
                    }
                }
                for (int i = 0; i < cw + 2; i++) {
                    for (int j = 0; j < gap; j++) {
                        int lx = cx + i + offsetX;
                        int ly = cy + ch + offsetY + j;
                        if (ly < result.getHeight() && lx < result.getWidth())
                            result.getPixelWriter().setColor(lx, ly, exportLineColor);
                    }
                }
                offsetY += gap;

            }
            offsetX += gap;
            offsetY = 0;
        }

        return result;
    }

    */

    @FXML
    void onSaveMenuAction() {
        WritableImage result = slice(null);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(result, null),
                    "png", imageFile);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        menuSave.setDisable(true);
    }

    @FXML
    void onSaveAsMenuAction() {
        fileChooser.setTitle("Save file...");
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        WritableImage result = slice(null);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(result, null),
                    "png", file);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    void onSplitToFilesMenuAction() {
        File file = fileChooser.showSaveDialog(stage);
        if (file == null)
            return;
        slice(file);
    }

    @FXML
    void onOpenMenuAction() {

        fileChooser.setTitle("Open Image...");
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
            imageView.setViewport(new Rectangle2D(imageView.getViewport().getMinX(), imageView.getViewport().getMinY(), (imageView.getFitWidth() / scale) * resampleFactor, (imageView.getFitHeight() / scale) * resampleFactor));
            menuSave.setDisable(false);
            menuSaveAs.setDisable(false);
            menuSliceToParts.setDisable(false);
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

            if (event.getCode() == KeyCode.S)
                slice(null);
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

        int partsW = (int) Math.ceil((totalW / w));
        int partsH = (int) Math.ceil((totalH / h));

        for (int i = 0; i < partsH; i++) {
            drawLine(
                    0,
                    i * h + y,
                    totalW,
                    i * h + y);
        }
        for (int i = 0; i < partsW; i++) {
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
