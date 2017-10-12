
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("View/main.fxml"));
        primaryStage.setTitle("Scénario du receiveFile() pour le protocle TFTP");
        primaryStage.setScene(new Scene(root, 500, 134));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
