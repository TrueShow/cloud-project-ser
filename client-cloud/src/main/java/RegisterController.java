import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

//еще не реализовано
public class RegisterController {

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
        registerButton.setOnAction(e -> {
            RegisterMsg rm = new RegisterMsg(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    loginFieldRegister.getText(),
                    passFieldRegister.getText()
            );
            // network.sendObj(rm);
            registerButton.getScene().getWindow().hide();
        });
    }
}
