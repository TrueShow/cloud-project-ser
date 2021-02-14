import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import animation.Shake;
import io.netty.util.ReferenceCountUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthController {
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);
    private Network network;

    @FXML
    private Text textField;

    @FXML
    private TextField authLoginField;

    @FXML
    private PasswordField authPassField;

    @FXML
    private Button authLoginButton;

    @FXML
    private Button newUserButton;

    @FXML
    void initialize() {
        network = Network.getInstance(msg -> {
            if (msg instanceof AuthRequest) {
                AuthRequest request = (AuthRequest) msg;
                LOG.debug("Получен статус по логину {} на авторизацию", request.getLogin());
                if (request.isAuthOk()) {
                    Platform.runLater(() -> {
                        authLoginButton.getScene().getWindow().hide();
                        openNewScene("/mainLayout.fxml");
                        LOG.debug("Открыта новая сцена mainLayout");
                    });
                } else {
                    Platform.runLater(() -> {
                        textField.setText("Invalid login or password");
                        LOG.debug("введенный логин {} или пароль неверные", request.getLogin());
                        Shake userLoginAnim = new Shake(authLoginField);
                        Shake userPassAnim = new Shake(authPassField);
                        userLoginAnim.playAnim();
                        userPassAnim.playAnim();
                    });
                }
            }
            ReferenceCountUtil.release(msg);
        });
        authLoginButton.setOnAction(e -> {
            String login = authLoginField.getText();
            String password = authPassField.getText();
            network.sendObj(new AuthRequest(login, password));
            LOG.debug("Направлен запрос на авторизацию по логину {} и паролю", login);
        });
        newUserButton.setOnAction(e -> {
            Platform.runLater(() -> {
                openNewScene("/registerForm.fxml");
                LOG.debug("Открыта новая сцена registerForm");
            });
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
