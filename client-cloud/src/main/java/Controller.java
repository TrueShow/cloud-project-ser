import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Controller {
    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);
    private Network network = Network.getInstance(this);
    public List<String> list;
    @FXML
    public Button loginUser;

    @FXML
    public TextField passwordField;

    @FXML
    public Button registerNewUser;

    @FXML
    public TextField loginField;

    @FXML
    public Button updateButton;

    @FXML
    public Button disconnectButton;

    @FXML
    public ListView<String> filesListCloud;

    @FXML
    public ListView<String> filesListClient;

    @FXML
    public Button downloadSelectedFile;

    @FXML
    public Button browseFile;

    @FXML
    public TextField pathToFile;

    @FXML
    public Button uploadFile;

    @FXML
    public Button connection;

    @FXML
    void initialize() {
        refreshLocalFilesList();

        registerNewUser.setOnAction(e -> {
            openNewScene("registerForm.fxml");
        });

        browseFile.setOnAction(e -> {
            getTheUserFilePath();
        });

        connection.setOnAction(e -> {
            network.launch();
            LOG.debug("Стартанул Netty Client");
            try {
                Thread.sleep(500);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            network.sendObj(new ListFileRequest());
            LOG.debug("Запрос на обновление листа отправлен (при старте сервера)");
        });

        uploadFile.setOnAction(e -> {
            FileMessage fm = new FileMessage(pathToFile.getText());
            network.sendObj(fm);
            LOG.debug("Файл отправлен - {}", fm.getFileName());
            pathToFile.clear();
            refreshStorageFilesList();
            refreshLocalFilesList();
        });

        disconnectButton.setOnAction(event -> {
            filesListCloud.getItems().clear();
            network.close();
            LOG.debug("Соединение с сервером остановлено");
        });

        updateButton.setOnAction(e -> {
            refreshStorageFilesList();
            refreshLocalFilesList();
        });

        downloadSelectedFile.setOnAction(e -> {
            String fileSelected = filesListCloud.getSelectionModel().getSelectedItem();
            network.sendObj(new FileRequest(fileSelected));
        });
    }

    public void getTheUserFilePath() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(new Stage());
        if (file != null) {
            String fileAsString = file.toString();
            pathToFile.setText(fileAsString);
        } else {
            pathToFile.setText(null);
        }
    }

    public void refreshStorageFilesList() {
        Platform.runLater(() -> {
            if (network.getChannel().isActive()) {
                filesListCloud.getItems().clear();
                network.sendObj(new ListFileRequest());
                LOG.debug("Запрос на обновление листа отправлен");
            } else {
                LOG.debug("Файл не обновляется, нет подключения к серверу");
            }
        });
    }

    public void refreshLocalFilesList() {
        Platform.runLater(() -> {
            try {
                filesListClient.getItems().clear();
                Files.list(Paths.get("client_repo")).map(p -> p.getFileName().toString()).forEach(o -> filesListClient.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void openNewScene(String window) {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(window));

        try {
            loader.load();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        Parent root = loader.getRoot();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }
}
