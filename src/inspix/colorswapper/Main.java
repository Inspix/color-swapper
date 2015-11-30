/* Color Swapper - Change selected colors of a whole picture.
   Copyright (C) 2015  Yuliyan Rusev, Inspix

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
    along with Color Swapper.  If not, see <http://www.gnu.org/licenses/>.
*/

package inspix.colorswapper;

import inspix.colorswapper.Scenes.MainWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader((getClass().getResource("Scenes/MainWindow.fxml")));
        Parent root = loader.load();

        MainWindowController controller = loader.getController();
        controller.setStage(primaryStage);

        primaryStage.setTitle("Color swapper");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
