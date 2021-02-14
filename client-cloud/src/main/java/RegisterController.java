import animation.Shake;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RegisterController {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterController.class);
    private Network network;

    @FXML
    private Text textField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField loginFieldRegister;

    @FXML
    private PasswordField passFieldRegister;

    @FXML
    private Button registerButton;

    @FXML
    void initialize() {
        network = Network.getInstance(msg -> {
            if (msg instanceof RegisterMsg) {
                RegisterMsg register = (RegisterMsg) msg;
                if (register.isExist()) {
                    Platform.runLater(() -> {
                        textField.setText("Login exists! Enter another Login");
                    });
                } else {
                    Platform.runLater(() -> {
                        registerButton.getScene().getWindow().hide();
                        LOG.debug("Пользователь зарегистрирован {}", register.getUserName());
                    });
                }
            }
        });

        registerButton.setOnAction(e -> {
            String login = loginFieldRegister.getText().trim();
            String password = passFieldRegister.getText().trim();
            String firsName = firstNameField.getText();
            String lastName = lastNameField.getText();

            if (login.equals("") || password.equals("") || firsName.equals("") || lastName.equals("")) {
                Platform.runLater(() -> {
                    textField.setText("Fields should not be empty");
                });
            }

            network.sendObj(new RegisterMsg(
                    firsName,
                    lastName,
                    login,
                    password
            ));
        });
    }
}
