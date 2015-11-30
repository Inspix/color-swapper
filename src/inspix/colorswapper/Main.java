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
