public class AuthRequest extends AbstractMessage {
    private String login;
    private String password;
    private boolean authOk;
    private boolean error;

    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
        authOk = false;
        error = false;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAuthOk() {
        return authOk;
    }

    public void setAuthOk(boolean authOk) {
        this.authOk = authOk;
    }

    public boolean isError() {
        return error;
    }
}
