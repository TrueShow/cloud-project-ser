import io.netty.util.ReferenceCountUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Controller {
    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);
    private Network network;

    @FXML
    public Button deleteButton;

    @FXML
    public Button disconnectButton;

    @FXML
    public ListView<String> filesListCloud;

    @FXML
    public Button downloadSelectedFile;

    @FXML
    public Button browseFile;

    @FXML
    public TextField pathToFile;

    @FXML
    public Button uploadFile;

    @FXML
    void initialize() {
        network = Network.getInstance(msg -> {
            try {
                if (msg instanceof ListFileRequest) {
                    ListFileRequest lfr = (ListFileRequest) msg;
                    Platform.runLater(() -> {
                        filesListCloud.getItems().clear();
                        filesListCloud.getItems().addAll(lfr.getList());
                    });
                    LOG.debug("Список файлов обновлен");
                }
                if (msg instanceof FileMessage) {
                    FileMessage fm = (FileMessage) msg;
                    Platform.runLater(() -> {
                        try {
                            FileChooser fc = new FileChooser();
                            fc.setInitialFileName(fm.getFileName());
                            File file = fc.showSaveDialog(new Stage());
                            LOG.debug("Запрос на сохранение файл {}", fm.getFileName());
                            if (file != null) {
                                file = new File(file.getAbsolutePath());
                                Files.write(Paths.get(file.getAbsolutePath()), fm.getData(), StandardOpenOption.CREATE);
                            }
                        } catch (IOException event) {
                            event.printStackTrace();
                        }
                    });
                }
            } catch (Exception ev) {
                ev.printStackTrace();
            }
            ReferenceCountUtil.release(msg);
        });

        network.sendObj(new ListFileRequest());
        LOG.debug("Запрос на обновление листа отправлен (при старте сервера)");
        browseFile.setOnAction(e -> {
            getTheUserFilePath();
        });

        uploadFile.setOnAction(e -> {
            if (network != null) {
                FileMessage fm = new FileMessage(pathToFile.getText());
                network.sendObj(fm);
                LOG.debug("Файл отправлен - {}", fm.getFileName());
                pathToFile.clear();
                refreshStorageFilesList();
            } else {
                LOG.debug("Соединение с сервером не установлено...");
            }
        });

        deleteButton.setOnAction(e -> {
            String fileSelected = filesListCloud.getSelectionModel().getSelectedItem();
            network.sendObj(new FileRequest(fileSelected, true));

            refreshStorageFilesList();
        });

        downloadSelectedFile.setOnAction(e -> {
            String fileSelected = filesListCloud.getSelectionModel().getSelectedItem();
            network.sendObj(new FileRequest(fileSelected, false));
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
