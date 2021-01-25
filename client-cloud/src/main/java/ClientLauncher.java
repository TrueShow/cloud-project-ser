import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientLauncher extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("cloudLayout.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Test Client");
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
