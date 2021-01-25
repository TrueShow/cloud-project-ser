import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
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
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private Socket socket;
    private ObjectDecoderInputStream in;
    private ObjectEncoderOutputStream out;
    private List<String> list;
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

    private static final Logger LOG = LoggerFactory.getLogger(ClientHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            out.writeObject(new ListFileRequest());
            if (msg instanceof ListFileRequest) {
                ListFileRequest lfr = (ListFileRequest) msg;
                filesListCloud.getItems().clear();
                list = lfr.getList();
                Platform.runLater(() -> {
                    list.forEach(o -> filesListCloud.getItems().add(o));
                    LOG.debug("Список файлов обновлен");
                });
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
            new NettyClient("localhost", 8189);
            flag = true;
            if (socket != null && socket.isConnected()) {
                LOG.debug("Клиент уже подключен");
            } else {
                try {
                    socket = new Socket("localhost", 8189);
                    LOG.debug("Клиент подключился...");
                    out = new ObjectEncoderOutputStream(socket.getOutputStream());
                    in = new ObjectDecoderInputStream(socket.getInputStream());
                    LOG.debug("Канал по передачи объекта запущен");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        uploadFile.setOnAction(e -> {
            FileMessage fm = new FileMessage(pathToFile.getText());
            try {
                out.writeObject(fm);
                LOG.debug("Файл отправлен - {}", fm.getFileName());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            refreshStorageFilesList();
            refreshLocalFilesList();
        });

        disconnectButton.setOnAction(event -> {
            flag = false;
            if (socket == null || socket.isClosed()) {
                LOG.debug("Нет подключения к серверу");
            } else {
                try {
                    out.close();
                    in.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LOG.debug("Соединение с сервером остановлено");
            }
        });
        updateButton.setOnAction(e -> {
            refreshStorageFilesList();
            refreshLocalFilesList();
        });

        downloadSelectedFile.setOnAction(e -> {
            try {
                String fileSelected = filesListCloud.getSelectionModel().getSelectedItem();
                out.writeObject(new FileRequest(fileSelected));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
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
            if (socket.isConnected()) {
                try {
                    filesListCloud.getItems().clear();
                    out.writeObject(new ListFileRequest());
                    Files.list(Paths.get("client_repo")).map(p -> p.getFileName().toString()).forEach(o -> filesListClient.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
