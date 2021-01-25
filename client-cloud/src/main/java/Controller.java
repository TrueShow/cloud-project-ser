import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
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
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Controller extends ChannelInboundHandlerAdapter {

    private List<String> list;
    private Network network;
    public static volatile boolean flag;

    @FXML
    private Button updateButton;

    @FXML
    private Button disconnectButton;

    @FXML
    private ListView<String> filesListCloud;

    @FXML
    private ListView<String> filesListClient;

    @FXML
    private Button downloadSelectedFile;

    @FXML
    private Button browseFile;

    @FXML
    private TextField pathToFile;

    @FXML
    private Button uploadFile;

    @FXML
    private Button connection;

    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof ListFileRequest) {
                ListFileRequest lfr = (ListFileRequest) msg;
                filesListCloud.getItems().clear();
                list = lfr.getList();
                list.forEach(o -> filesListCloud.getItems().add(o));
                LOG.debug("Список файлов обновлен");
            }
            if (msg instanceof FileMessage) {
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get("client_repo/" + fm.getFileName()), fm.getData(), StandardOpenOption.CREATE);
                refreshLocalFilesList();
                LOG.debug("Файл {} принят", fm.getFileName());
            }

        } catch (IOException event) {
            event.printStackTrace();
        }
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @FXML
    void initialize() {
        refreshLocalFilesList();

        browseFile.setOnAction(e -> {
            getTheUserFilePath();
        });

        connection.setOnAction(e -> {
            network = new Network();
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

    private void getTheUserFilePath() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(new Stage());
        if (file != null) {
            String fileAsString = file.toString();
            pathToFile.setText(fileAsString);
        } else {
            pathToFile.setText(null);
        }
    }

    private void refreshStorageFilesList() {
        Platform.runLater(() -> {
            if (network.getChannel().isActive()) {
                filesListCloud.getItems().clear();
                network.sendObj(new ListFileRequest());
                LOG.debug("Запрос на обновление листа отправлен");
            } else {
                LOG.debug("Подключения к серверу нет");
            }
        });
    }

    private void refreshLocalFilesList() {
        Platform.runLater(() -> {
            try {
                filesListClient.getItems().clear();
                Files.list(Paths.get("client_repo")).map(p -> p.getFileName().toString()).forEach(o -> filesListClient.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
