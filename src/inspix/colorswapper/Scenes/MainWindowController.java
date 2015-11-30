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
import inspix.colorswapper.Utils.Toast;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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
import java.util.ResourceBundle;

public class MainWindowController implements Initializable{

    FileChooser fileChooser = new FileChooser();
    double currentX,currentY, distanceX,distanceY,scale,scaleFit;
    DecimalFormat f = new DecimalFormat("#.##");
    DecimalFormat fPercent = new DecimalFormat("#.##%");

    @FXML
    ImageView imageView;

    @FXML
    Button btnOpenFile, btnSave, btnSaveAs;

    @FXML
    Circle circle;

    @FXML
    ColorPicker colorPicker;

    @FXML
    VBox sidePanel;

    @FXML
    AnchorPane mainPane;

    @FXML
    CheckBox additionalInfo;

    @FXML
    GridPane additionalInfoContainer;

    @FXML
    Label redLabel, blueLabel, greenLabel, hueLabel, saturationLabel, brightnessLabel, alphaLabel, posXLabel, posYLabel;

    private WritableImage image;
    private File originalImage;

    private ContextMenu contextMenu;

    private Stage stage;

    public void setStage(Stage stage){
        this.stage = stage;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scale = 1;
        setUpContextMenu();
        setUpImageView();
        fileChooser.setTitle("Open Image...");
        FileChooser.ExtensionFilter png = new FileChooser.ExtensionFilter("Portable Network Graphics", "*.png");
        fileChooser.getExtensionFilters().add(png);
        fileChooser.setSelectedExtensionFilter(png);
    }


    private void setUpImageView() {
        imageView.setDisable(true);
        imageView.setEffect(new DropShadow(5, Color.BLACK));
        imageView.setOnMousePressed(e -> {
            currentX = e.getX() * scale;
            currentY = e.getY() * scale;
            distanceX = imageView.getViewport().getMinX() + currentX;
            distanceY = imageView.getViewport().getMinY() + currentY;
        });

        imageView.setOnMouseDragged(e ->
                imageView.setViewport(
                        new Rectangle2D(
                                distanceX - e.getX() * scale,
                                distanceY - e.getY() * scale,
                                imageView.getFitWidth() * scale,
                                imageView.getFitHeight() * scale))
        );

        imageView.setOnScroll(e -> {
            scale += e.getDeltaY() < 0 ? 0.01 : -0.01;
            System.out.println(scale);
            if (scale > 2)
                scale = 2;
            if (scale <= 0.001) {
                scale = 0.01;
            }
            imageView.setViewport(new Rectangle2D(imageView.getViewport().getMinX(), imageView.getViewport().getMinY(), imageView.getFitWidth() * scale, imageView.getFitHeight() * scale));

        });

        imageView.setOnMouseClicked(e -> {
            Color color = getPixelColor(e);
            if (color != null)
                colorPicker.setValue(color);
        });

        imageView.setOnMouseMoved(e -> {
            Color color = getPixelColor(e);
            if (color != null)
                circle.setFill(color);
        });
    }

    @FXML
    void onOpenButtonAction() {
        if (originalImage != null)
            fileChooser.setInitialDirectory(originalImage.getParentFile());
        originalImage = fileChooser.showOpenDialog(stage);
        WritableImage wimage = null;
        if (originalImage == null)
            return;
        try (FileInputStream is = new FileInputStream(originalImage)) {
            Image img = new Image(is);
            wimage = new WritableImage(img.getPixelReader(), (int) img.getWidth(), (int) img.getHeight());

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (wimage != null) {
            scale = 1;
            image = wimage;
            imageView.setSmooth(false);
            imageView.setDisable(false);
            imageView.setImage(wimage);
            imageView.setViewport(new Rectangle2D(0, 0, imageView.getFitWidth() * scale, imageView.getFitHeight() * scale));
            scaleFit = wimage.getWidth() / imageView.getFitWidth();
            sidePanel.getChildren().remove(0, sidePanel.getChildren().size());
            btnSave.setDisable(false);
            btnSaveAs.setDisable(false);
        }
    }

    @FXML
    void onSaveButtonAction() {
        writeImage(originalImage);
    }

    @FXML
    void onSaveAsButtonAction() {
        fileChooser.setInitialDirectory(originalImage.getParentFile());
        File file = fileChooser.showSaveDialog(stage);
        writeImage(file);
    }

    private void writeImage(File file) {
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null),
                    "png", file);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    private void onAdditionalInfoChange() {
        additionalInfoContainer.setVisible(additionalInfo.isSelected());
    }

    private void setUpContextMenu(){
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
            }

            if (alreadySelected) {
                Toast.create(mainPane, Toast.DURATION_SHORT, "Color already selected!");
                return;

            }

            ColorNode clrNode = new ColorNode((WritableImage)imageView.getImage());
            clrNode.setOriginalColor(clr);
            sidePanel.getChildren().add(clrNode);
        });
        // TODO: Clipboard Copying
        contextMenu.getItems().addAll(item1,item2,item3);
        imageView.setOnContextMenuRequested(e -> contextMenu.show(stage,e.getSceneX(),e.getSceneY()));
    }


    private Color getPixelColor(MouseEvent e) {
        if (imageView.getImage() == null){
            return null;
        }
        double viewPortX = imageView.getViewport().getMinX();
        double viewPortY = imageView.getViewport().getMinY();
        double xClicked = e.getX();
        double yClicked = e.getY();

        int xImage = (int)((xClicked * scale) + viewPortX);
        int yImage = (int)((yClicked * scale) + viewPortY);

        if (xImage >= imageView.getImage().getWidth() || xImage < 0) {
            if (additionalInfo.isSelected())
                updateAdditionalInfo(-1, -1, null);
            return null;
        }
        if (yImage >= imageView.getImage().getHeight() || yImage < 0) {
            if (additionalInfo.isSelected())
                updateAdditionalInfo(-1, -1, null);
            return null;
        }
        Color color = imageView.getImage().getPixelReader().getColor(xImage, yImage);

        if (additionalInfo.isSelected())
            updateAdditionalInfo(xImage, yImage, color);

        return color;

    }

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
}
