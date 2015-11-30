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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable{

    FileChooser fileChooser = new FileChooser();
    double currentX,currentY, distanceX,distanceY,scale,scaleFit;

    @FXML
    ImageView imageView;

    @FXML
    Button btnOpenFile;

    @FXML
    Circle circle;

    @FXML
    ColorPicker colorPicker;

    @FXML
    VBox sidePanel;


    private ContextMenu contextMenu;

    private Stage stage;

    public void setStage(Stage stage){
        this.stage = stage;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scale = 1;
        setUpContextMenu();
        imageView.setDisable(true);
        imageView.setEffect(new DropShadow(5, Color.BLACK));
        imageView.setOnMousePressed(e -> {
            currentX = e.getX();
            currentY = e.getY();
            distanceX = imageView.getViewport().getMinX() + currentX;
            distanceY = imageView.getViewport().getMinY() + currentY;
        });

        imageView.setOnMouseDragged(e -> {
            imageView.setViewport(
                    new Rectangle2D(
                            distanceX - e.getX(),
                            distanceY - e.getY(),
                            imageView.getImage().getWidth()* scale,
                            imageView.getImage().getHeight()* scale));

        });

        imageView.setOnScroll(e -> {
            scale += e.getDeltaY() < 0 ? 0.01 : -0.01;
            if(scale > 50)
                scale = 50;
            if(scale <=0){
                scale=0.1;
            }
            imageView.setViewport(new Rectangle2D(imageView.getViewport().getMinX(),imageView.getViewport().getMinY(),imageView.getImage().getWidth() * scale,imageView.getImage().getHeight() * scale));

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

        btnOpenFile.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(stage);
            WritableImage wimage = null;
            try(FileInputStream is = new FileInputStream(file)){
                Image image= new Image(is);
                wimage = new WritableImage(image.getPixelReader(),(int)image.getWidth(),(int)image.getHeight());


            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (wimage != null){
                scale = 1;
                imageView.setDisable(false);
                imageView.setImage(wimage);
                imageView.setViewport(new Rectangle2D(0,0,wimage.getWidth() * scale,wimage.getHeight() * scale));
                scaleFit = wimage.getWidth() / imageView.getFitWidth();

            }
        });
    }

    private void setUpContextMenu(){
        contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem("Add for change..");
        MenuItem item2 = new MenuItem("Copy HEX Code to Clipboard");
        MenuItem item3 = new MenuItem("Copy RGB Values to Clipboard");
        item1.setOnAction(e -> {
            ColorNode clrNode = new ColorNode((WritableImage)imageView.getImage());
            clrNode.setOriginalColor(colorPicker.getValue());
            sidePanel.getChildren().add(clrNode);
        });
        // TODO: Clipboard Copying
        contextMenu.getItems().addAll(item1,item2,item3);
        imageView.setOnContextMenuRequested(e -> {
            contextMenu.show(stage,e.getSceneX(),e.getSceneY());
        });
    }


    private Color getPixelColor(MouseEvent e) {
        if (imageView.getImage() == null){
            return null;
        }
        double viewPortX = imageView.getViewport().getMinX();
        double viewPortY = imageView.getViewport().getMinY();
        double xClicked = e.getX();
        double yClicked = e.getY();

        int xImage = (int)((xClicked * scale  * scaleFit) + viewPortX);
        int yImage = (int)((yClicked * scale  * scaleFit) + viewPortY);

        if (xImage >= imageView.getImage().getWidth() || xImage < 0)
            return null;
        if (yImage >= imageView.getImage().getHeight() || yImage < 0)
            return null;
        return imageView.getImage().getPixelReader().getColor(xImage,yImage);

    }
}
