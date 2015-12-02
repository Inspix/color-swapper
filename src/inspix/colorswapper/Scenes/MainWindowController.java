/*
 * Copyright year Yuliyan Rusev - Inspix
 *
 * MainWindowController.java is part of ColorSwapper.
 *
 * ColorSwapper is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * ColorSwapper is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see http://www.gnu.org/licenses/.
 *
 */

package inspix.colorswapper.Scenes;

import inspix.colorswapper.Controls.ColorNode;
import inspix.colorswapper.Utils.ColorHelpers;
import inspix.colorswapper.Utils.Toast;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class MainWindowController implements Initializable {


    //region FXML Fields
    @FXML
    AnchorPane mainPane;
    @FXML
    Button btnOpenFile, btnSave, btnSaveAs, btnFindAllUniquePixels, btnResetChanges, btnRemoveSelectedColors;
    @FXML
    ImageView imageView;
    @FXML
    Circle circle, circleOriginal;
    @FXML
    ColorPicker colorPicker, colorPickerOriginal;
    @FXML
    CheckBox additionalInfo, additionalInfoOriginal;
    @FXML
    ComboBox<String> comboBoxSort;
    @FXML
    GridPane additionalInfoContainer, additionalInfoContainerOriginal;
    @FXML
    Label redLabel, blueLabel, greenLabel, hueLabel, saturationLabel, brightnessLabel, alphaLabel, posXLabel, posYLabel,
            redLabelOriginal, blueLabelOriginal, greenLabelOriginal, hueLabelOriginal, saturationLabelOriginal,
            brightnessLabelOriginal, alphaLabelOriginal, posXLabelOriginal, posYLabelOriginal;
    @FXML
    Pane imageViewContainer;
    @FXML
    ScrollPane scrollPane;
    @FXML
    VBox sidePanel;
    //endregion

    //region Fields
    private WritableImage image;
    private Image original;
    private File imageFile;
    private ContextMenu contextMenu;
    private Stage stage;
    boolean dragged;
    FileChooser fileChooser = new FileChooser();
    double currentX, currentY, distanceX, distanceY, scale, scaleFit, scaleMultiplier = 0.01;
    DecimalFormat f = new DecimalFormat("#.##");
    DecimalFormat fPercent = new DecimalFormat("#.##%");
    //endregion

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scale = 1;
        setUpContextMenu();
        setUpImageView();
        setUpChoiceBox();
        DropShadow shadow = new DropShadow(BlurType.ONE_PASS_BOX, Color.color(0, 0, 0, 0.5), 2, 0, 2, 2);

        circle.setEffect(shadow);
        circleOriginal.setEffect(shadow);

        fileChooser.setTitle("Open Image...");
        FileChooser.ExtensionFilter png = new FileChooser.ExtensionFilter("Portable Network Graphics", "*.png");
        fileChooser.getExtensionFilters().add(png);
        fileChooser.setSelectedExtensionFilter(png);
    }

    private void setUpChoiceBox() {
        comboBoxSort.getItems().add("Sort R <= G <= B");
        comboBoxSort.getItems().add("Sort H <= S <= V");
        comboBoxSort.getItems().add("Sort by hue");
        comboBoxSort.getItems().add("Sort by saturation");
        comboBoxSort.getItems().add("Sort by brightness");
        comboBoxSort.getItems().add("Sort by alpha");

        comboBoxSort.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.intValue()) {
                case 0:
                    sortRGB();
                    break;
                case 1:
                    sortHSB();
                    break;
                case 2:
                    sortHSBpart(0);
                    break;
                case 3:
                    sortHSBpart(1);
                    break;
                case 4:
                    sortHSBpart(2);
                    break;
                case 5:
                    sortAlpha();
                    break;
            }
        });
    }

    private void setUpContextMenu() {
        contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem("Add for change..");
        MenuItem item2 = new MenuItem("Copy HEX Code to Clipboard");
        MenuItem item3 = new MenuItem("Copy RGB Values to Clipboard");
        item1.setOnAction(e -> {
            Color clr = colorPicker.getValue();
            boolean alreadySelected = false;
            for (int i = 0; i < sidePanel.getChildren().size(); i++) {
                ColorNode colorNode = (ColorNode) sidePanel.getChildren().get(i);
                alreadySelected = colorNode.getOriginalColor().equals(clr);
                if (alreadySelected) {
                    Toast.create(mainPane, Toast.DURATION_SHORT, "Color already selected!");
                    return;
                }
            }

            ColorNode clrNode = new ColorNode((WritableImage) imageView.getImage());
            clrNode.setFocusTraversable(false);
            clrNode.setOriginalColor(clr);
            sidePanel.getChildren().add(clrNode);
            comboBoxSort.setDisable(false);
            btnRemoveSelectedColors.setDisable(false);
            btnFindAllUniquePixels.setDisable(true);
        });
        // TODO: Clipboard Copying
        contextMenu.getItems().addAll(item1, item2, item3);
        imageView.setOnContextMenuRequested(e -> contextMenu.show(stage, e.getSceneX(), e.getSceneY()));
    }

    private void setUpImageView() {
        mainPane.setOnKeyPressed(event -> {
            if (event.isControlDown())
                scaleMultiplier = 0.1;
        });

        mainPane.setOnKeyReleased(event -> {
            scaleMultiplier = 0.01;
        });

        imageViewContainer.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        imageView.setDisable(true);
        imageView.setOnMousePressed(e -> {
            currentX = e.getX() * scale;
            currentY = e.getY() * scale;
            distanceX = imageView.getViewport().getMinX() + currentX;
            distanceY = imageView.getViewport().getMinY() + currentY;
        });

        imageView.setOnMouseDragged(e -> {
            dragged = true;
            imageView.setViewport(
                    new Rectangle2D(
                            distanceX - e.getX() * scale,
                            distanceY - e.getY() * scale,
                            imageView.getFitWidth() * scale,
                            imageView.getFitHeight() * scale));
        });

        imageView.setOnScroll(e -> {
            scale += e.getDeltaY() < 0 ? scaleMultiplier : (-scaleMultiplier);
            if (scale > 2)
                scale = 2;
            if (scale <= 0.001) {
                scale = 0.01;
            }


            imageView.setViewport(new Rectangle2D(imageView.getViewport().getMinX(), imageView.getViewport().getMinY(), imageView.getFitWidth() * scale, imageView.getFitHeight() * scale));

        });

        imageView.setOnMouseClicked(e -> {
            if (dragged) {
                dragged = false;
                return;
            }
            Color[] colors = getPixelColors(getRelativePosition(e));
            if (colors != null) {
                colorPickerOriginal.setValue(colors[0]);
                colorPicker.setValue(colors[1]);
                if (e.getButton() == MouseButton.PRIMARY) {
                    highlightColor();
                }
            }
        });

        imageView.setOnMouseMoved(e -> {
            Color[] colors = getPixelColors(getRelativePosition(e));
            if (colors != null) {
                circleOriginal.setFill(colors[0]);
                circle.setFill(colors[1]);
            }
        });
    }

    //region Sorting

    private void sortHSBpart(int part) {
        ObservableList<Node> worklist = FXCollections.observableArrayList(sidePanel.getChildren());
        Collections.sort(worklist, (o1, o2) -> {
            ColorNode n1 = (ColorNode) o1;
            ColorNode n2 = (ColorNode) o2;

            Color c1 = (Color) n1.getOriginalColor();
            Color c2 = (Color) n2.getOriginalColor();

            double[] hsb1 = ColorHelpers.RGBtoHSB(c1);
            double[] hsb2 = ColorHelpers.RGBtoHSB(c2);


            return hsb1[part] != hsb2[part] ? hsb1[part] < hsb2[part] ? -1 : 1 : 0;
        });
        sidePanel.getChildren().setAll(worklist);
    }

    private void sortAlpha() {
        ObservableList<Node> worklist = FXCollections.observableArrayList(sidePanel.getChildren());
        worklist.sort((o1, o2) -> {
            ColorNode n1 = (ColorNode) o1;
            ColorNode n2 = (ColorNode) o2;

            Color c1 = (Color) n1.getOriginalColor();
            Color c2 = (Color) n2.getOriginalColor();

            double opacity1 = c1.getOpacity();
            double opacity2 = c2.getOpacity();

            return opacity1 != opacity2 ? opacity1 < opacity2 ? -1 : 1 : 0;
        });
        sidePanel.getChildren().setAll(worklist);

    }

    private void sortHSB() {
        ObservableList<Node> worklist = FXCollections.observableArrayList(sidePanel.getChildren());
        worklist.sort((o1, o2) -> {
            ColorNode n1 = (ColorNode) o1;
            ColorNode n2 = (ColorNode) o2;
            return ColorHelpers.compareHSB((Color) n1.getOriginalColor(), (Color) n2.getOriginalColor());
        });
        sidePanel.getChildren().setAll(worklist);
    }

    private void sortRGB() {
        ObservableList<Node> worklist = FXCollections.observableArrayList(sidePanel.getChildren());
        worklist.sort((o1, o2) -> {
            ColorNode n1 = (ColorNode) o1;
            ColorNode n2 = (ColorNode) o2;
            return ColorHelpers.compareRGB((Color) n1.getOriginalColor(), (Color) n2.getOriginalColor());
        });
        sidePanel.getChildren().setAll(worklist);
    }
    //endregion

    //region Button actions
    @FXML
    void onOpenButtonAction() {
        if (imageFile != null)
            fileChooser.setInitialDirectory(imageFile.getParentFile());
        imageFile = fileChooser.showOpenDialog(stage);
        WritableImage wimage = null;
        if (imageFile == null)
            return;
        try (FileInputStream is = new FileInputStream(imageFile)) {
            original = new Image(is);
            wimage = new WritableImage(original.getPixelReader(), (int) original.getWidth(), (int) original.getHeight());

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (wimage != null) {
            btnFindAllUniquePixels.setDisable(false);
            btnRemoveSelectedColors.setDisable(true);
            scale = 1;
            image = wimage;
            imageView.setSmooth(true);
            imageView.setDisable(false);
            imageView.setImage(wimage);
            imageView.setFitWidth(imageViewContainer.getWidth());
            imageView.setFitHeight(imageViewContainer.getHeight());
            imageView.setViewport(new Rectangle2D(0, 0, imageView.getFitWidth() * scale, imageView.getFitHeight() * scale));
            scaleFit = wimage.getWidth() / imageView.getFitWidth();
            sidePanel.getChildren().remove(0, sidePanel.getChildren().size());
            btnSave.setDisable(false);
            btnSaveAs.setDisable(false);
        }
    }

    @FXML
    void onSaveButtonAction() {
        writeImage(imageFile);
    }

    @FXML
    void onSaveAsButtonAction() {
        fileChooser.setInitialDirectory(imageFile.getParentFile());
        File file = fileChooser.showSaveDialog(stage);
        writeImage(file);
    }

    @FXML
    void onResetChangesButtonAction() {
        sidePanel.getChildren().forEach(node -> ((ColorNode) node).resetChanges());
    }

    @FXML
    void onRemoveSelectedColorsAction() {
        sidePanel.getChildren().clear();
        btnFindAllUniquePixels.setDisable(false);
        btnRemoveSelectedColors.setDisable(true);
        comboBoxSort.setDisable(true);
    }

    @FXML
    void onFindUniquePixels() {
        Task task = createFindTask();

        ProgressBar progressBar = new ProgressBar(0);
        AnchorPane.setBottomAnchor(progressBar, 5d);
        AnchorPane.setLeftAnchor(progressBar, imageView.getFitWidth() / 2);
        mainPane.getChildren().add(progressBar);
        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            mainPane.getChildren().remove(progressBar);
            btnRemoveSelectedColors.setDisable(false);
            btnFindAllUniquePixels.setDisable(true);
            comboBoxSort.setDisable(false);
            Toast.create(mainPane, Toast.DURATION_LONG, "Colors found:" + String.valueOf(sidePanel.getChildren().size()));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    void onRGBTabButtonAction() {
        changeAllTabs(0);
    }

    @FXML
    void onHSBTabButtonAction() {
        changeAllTabs(1);
    }

    @FXML
    void onHEXTabButtonAction() {
        changeAllTabs(2);
    }

    //endregion

    @FXML
    private void onAdditionalInfoChange() {
        additionalInfoContainer.setVisible(additionalInfo.isSelected());
        additionalInfoContainerOriginal.setVisible(additionalInfoOriginal.isSelected());
    }

    private void changeAllTabs(int i) {
        sidePanel.getChildren().forEach(node -> ((ColorNode) node).changeTab(i));
    }

    private void highlightColor() {
        boolean colorFound = false;
        int count = sidePanel.getChildren().size();
        for (int i = 0; i < count; i++) {
            ColorNode node = (ColorNode) sidePanel.getChildren().get(i);
            if (node.getOriginalColor().equals(colorPickerOriginal.getValue())) {
                node.setBorder(new Border(new BorderStroke(Color.ROYALBLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));

                double scrollValue = i == count - 1 ? 1 : (double) i / count;

                scrollPane.setVvalue(scrollValue);
                colorFound = true;
            } else {
                node.setBorder(Border.EMPTY);
            }
        }
        if (!colorFound)
            Toast.create(mainPane, Toast.DURATION_SHORT, "Color not added for edit.");
    }

    private Task<Map<Color, ColorNode>> createFindTask() {
        return new Task<Map<Color, ColorNode>>() {
            @Override
            protected Map<Color, ColorNode> call() throws Exception {
                Map<Color, ColorNode> pixels = new HashMap<>();
                long pixelsToCheck = (long) image.getWidth() * (long) image.getHeight();
                long progress = 0;
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        Color current = image.getPixelReader().getColor(x, y);
                        Point2D coords = new Point2D(x, y);
                        if (pixels.containsKey(current)) {
                            pixels.get(current).addPixel(coords);
                            Platform.runLater(() -> pixels.get(current).updatePixelCount());
                        } else {
                            ColorNode clrNode = new ColorNode(image);
                            clrNode.setOriginalColor(current);
                            clrNode.findButtonDisable(true);
                            clrNode.addPixel(coords);
                            Platform.runLater(() -> {
                                sidePanel.getChildren().add(clrNode);
                                clrNode.updatePixelCount();
                            });
                            pixels.put(current, clrNode);
                        }
                        updateProgress(++progress, pixelsToCheck);
                    }
                }

                pixels.forEach((color, colorNode) -> colorNode.enableControlls());
                return pixels;
            }
        };
    }

    private void writeImage(File file) {
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null),
                    "png", file);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    //region Position and Pixel helpers
    private Point2D getRelativePosition(ScrollEvent e) {
        return getRelativeCursorPosition(new Point2D(e.getX(), e.getY()));
    }

    private Point2D getRelativePosition(MouseEvent e) {
        return getRelativeCursorPosition(new Point2D(e.getX(), e.getY()));
    }

    private Point2D getRelativeCursorPosition(Point2D coords) {

        if (imageView.getImage() == null) {
            return null;
        }
        double viewPortX = imageView.getViewport().getMinX();
        double viewPortY = imageView.getViewport().getMinY();
        double xClicked = coords.getX();
        double yClicked = coords.getY();

        int xImage = (int) ((xClicked * scale) + viewPortX);
        int yImage = (int) ((yClicked * scale) + viewPortY);

        return new Point2D(xImage, yImage);
    }

    private Color[] getPixelColors(Point2D point) {
        Color[] result = new Color[2];
        int x = (int) point.getX();
        int y = (int) point.getY();

        if (x >= imageView.getImage().getWidth() || x < 0) {
            if (additionalInfo.isSelected())
                updateAdditionalInfo(-1, -1, null);
            return null;
        }
        if (y >= imageView.getImage().getHeight() || y < 0) {
            if (additionalInfo.isSelected())
                updateAdditionalInfo(-1, -1, null);
            return null;
        }
        result[0] = original.getPixelReader().getColor(x, y);
        result[1] = image.getPixelReader().getColor(x, y);

        if (additionalInfo.isSelected())
            updateAdditionalInfo(x, y, result[0]);
        if (additionalInfoOriginal.isSelected())
            updateAdditionalInfoOriginal(x, y, result[1]);


        return result;

    }
    //endregion

    private void updateAdditionalInfo(int x, int y, Color clr) {
        posXLabel.setText(String.valueOf(x));
        posYLabel.setText(String.valueOf(y));
        alphaLabel.setText(String.valueOf(clr == null ? "none" : f.format(clr.getOpacity() * 255)));
        redLabel.setText(String.valueOf(clr == null ? "none" : f.format(clr.getRed() * 255)));
        greenLabel.setText(String.valueOf(clr == null ? "none" : f.format(clr.getGreen() * 255)));
        blueLabel.setText(String.valueOf(clr == null ? "none" : f.format(clr.getBlue() * 255)));
        hueLabel.setText(String.valueOf(clr == null ? "none" : f.format(clr.getHue())));
        saturationLabel.setText(String.valueOf(clr == null ? "none" : fPercent.format(clr.getSaturation())));
        brightnessLabel.setText(String.valueOf(clr == null ? "none" : fPercent.format(clr.getBrightness())));
    }

    private void updateAdditionalInfoOriginal(int x, int y, Color clr) {
        posXLabelOriginal.setText(String.valueOf(x));
        posYLabelOriginal.setText(String.valueOf(y));
        alphaLabelOriginal.setText(String.valueOf(clr == null ? "none" : f.format(clr.getOpacity() * 255)));
        redLabelOriginal.setText(String.valueOf(clr == null ? "none" : f.format(clr.getRed() * 255)));
        greenLabelOriginal.setText(String.valueOf(clr == null ? "none" : f.format(clr.getGreen() * 255)));
        blueLabelOriginal.setText(String.valueOf(clr == null ? "none" : f.format(clr.getBlue() * 255)));
        hueLabelOriginal.setText(String.valueOf(clr == null ? "none" : f.format(clr.getHue())));
        saturationLabelOriginal.setText(String.valueOf(clr == null ? "none" : fPercent.format(clr.getSaturation())));
        brightnessLabelOriginal.setText(String.valueOf(clr == null ? "none" : fPercent.format(clr.getBrightness())));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
