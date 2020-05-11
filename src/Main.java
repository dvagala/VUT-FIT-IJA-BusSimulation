import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("gui/scene.fxml"));

        Scene scene = new Scene(root, 1280, 720);
//        scene.getStylesheets().add("gui/mainScene.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("Factory Builder");
        primaryStage.show();



    }

    public int getInt(){
        return 1;
    }
}