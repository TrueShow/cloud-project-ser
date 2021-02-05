//package temp_proto;
//
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.ListView;
//import javafx.scene.control.TextField;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//
//public class ControllerOld {
//    private static final Logger LOG = LoggerFactory.getLogger(ControllerOld.class);
//
//    private NetworkOld networkOld = NetworkOld.getInstance(this);
//
//    public List<String> list;
//
//    @FXML
//    public Button deleteButton;
//
//    @FXML
//    public Button loginUser;
//
//    @FXML
//    public TextField passwordField;
//
//    @FXML
//    public Button registerNewUser;
//
//    @FXML
//    public TextField loginField;
//
//    @FXML
//    public Button updateButton;
//
//    @FXML
//    public Button disconnectButton;
//
//    @FXML
//    public ListView<String> filesListCloud;
//
//    @FXML
//    public ListView<String> filesListClient;
//
//    @FXML
//    public Button downloadSelectedFile;
//
//    @FXML
//    public Button browseFile;
//
//    @FXML
//    public TextField pathToFile;
//
//    @FXML
//    public Button uploadFile;
//
//    @FXML
//    public Button connection;
//
//    @FXML
//    void initialize() {
////        refreshLocalFilesList();
//
//        registerNewUser.setOnAction(e -> {
//            openNewScene("registerForm.fxml");
//        });
//
//        browseFile.setOnAction(e -> {
//            getTheUserFilePath();
//        });
//
//        connection.setOnAction(e -> {
//            networkOld.launch();
//            LOG.debug("Стартанул Netty Client");
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException interruptedException) {
//                interruptedException.printStackTrace();
//            }
//            networkOld.sendObj(new ListFileRequest());
//            LOG.debug("Запрос на обновление листа отправлен (при старте сервера)");
//        });
//
//        uploadFile.setOnAction(e -> {
//            FileMessage fm = new FileMessage(pathToFile.getText());
//            networkOld.sendObj(fm);
//            LOG.debug("Файл отправлен - {}", fm.getFileName());
//            pathToFile.clear();
//            refreshStorageFilesList();
////            refreshLocalFilesList();
//        });
//
//        disconnectButton.setOnAction(e -> {
//            filesListCloud.getItems().clear();
//            networkOld.close();
//            LOG.debug("Соединение с сервером остановлено");
//        });
//
//        deleteButton.setOnAction(e->{
//            String fileSelected = filesListCloud.getSelectionModel().getSelectedItem();
//            networkOld.sendObj(new FileRequest(fileSelected, true));
//            refreshStorageFilesList();
//        });
//
////        updateButton.setOnAction(e -> {
////            refreshStorageFilesList();
////            refreshLocalFilesList();
////        });
//
//        downloadSelectedFile.setOnAction(e -> {
//            String fileSelected = filesListCloud.getSelectionModel().getSelectedItem();
//            networkOld.sendObj(new FileRequest(fileSelected, false));
//
//
////            if (filesListCloud.isFocused()) {
////                String fileSelected = filesListCloud.getSelectionModel().getSelectedItem();
////                networkOld.sendObj(new FileRequest(fileSelected, false));
////            }
////            if (filesListClient.isFocused()) {
////                String fileSelected = filesListClient.getSelectionModel().getSelectedItem();
////                try {
////                    Files.delete(Paths.get("/client_repo/" + fileSelected));
////                } catch (IOException ioException) {
////                    ioException.printStackTrace();
////                }
////            }
//        });
//    }
//
//    public void getTheUserFilePath() {
//        FileChooser chooser = new FileChooser();
//        File file = chooser.showOpenDialog(new Stage());
//        if (file != null) {
//            String fileAsString = file.toString();
//            pathToFile.setText(fileAsString);
//        } else {
//            pathToFile.setText(null);
//        }
//    }
//
//    public void refreshStorageFilesList() {
//        Platform.runLater(() -> {
//            if (networkOld.getChannel().isActive()) {
//                filesListCloud.getItems().clear();
//                networkOld.sendObj(new ListFileRequest());
//                LOG.debug("Запрос на обновление листа отправлен");
//            } else {
//                LOG.debug("Файл не обновляется, нет подключения к серверу");
//            }
//        });
//    }
//
////    public void refreshLocalFilesList() {
////        Platform.runLater(() -> {
////            try {
////                filesListClient.getItems().clear();
////                Files.list(Paths.get("client_repo")).map(p -> p.getFileName().toString()).forEach(o -> filesListClient.getItems().add(o));
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////        });
////    }
//
//    public void openNewScene(String window) {
//
//        FXMLLoader loader = new FXMLLoader();
//        loader.setLocation(getClass().getResource(window));
//
//        try {
//            loader.load();
//        } catch (IOException ioException) {
//            ioException.printStackTrace();
//        }
//
//        Parent root = loader.getRoot();
//        Stage stage = new Stage();
//        stage.setScene(new Scene(root));
//        stage.showAndWait();
//    }
//}
