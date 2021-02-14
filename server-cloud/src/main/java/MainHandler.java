import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(MainHandler.class);
    private DbHandler db;
    private String clientNick;
    private Path clientPath;
    private static int cnt = 0;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("Клиент отключился, осталось подключено - {} клиентов", --cnt);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        cnt++;
        LOG.debug("Клиент подключился, подключено - {} клиентов", cnt);
        if (DbHandler.getConn() == null) {
            DbHandler.connect();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        db = new DbHandler();
        if (msg instanceof AuthRequest) {
            AuthRequest request = (AuthRequest) msg;
            String login = request.getLogin().trim();
            LOG.debug("Прилетел запрос на авторизацию с логина {}", login);
            String password = request.getPassword().trim();
            clientNick = login;
            if (!login.equals("") && !password.equals("")) {
                User user = new User();
                user.setUserName(login);
                user.setPassword(password);
                if (checkUser(user)) {
                    LOG.debug("логин и пароль есть в БД");
                    request.setAuthOk(true);
                    clientPath = Paths.get("server_repo", clientNick);
                    if (!Files.exists((clientPath))) {
                        Files.createDirectory(clientPath);
                    }
                    ctx.writeAndFlush(request);
                    LOG.debug("Пользователь с ником {} подтверждем", clientNick);
                } else {
                    ctx.writeAndFlush(request);
                }
            } else {
                LOG.debug("Неверный логин или пароль");
                ctx.writeAndFlush(request);
            }
        }
        if (msg instanceof FileMessage) {

            FileMessage fm = (FileMessage) msg;
            LOG.debug("Прилетел запрос на сохранение файла {}", fm.getFileName());
            Files.write(clientPath.resolve(fm.getFileName()), fm.getData());
            LOG.debug("Файл {} получен", fm.getFileName());
        }
        if (msg instanceof ListFileRequest) {
            LOG.debug("Прилетел запрос на обновление файла в сетевой папке");
            ctx.writeAndFlush(new ListFileRequest(clientPath));
            LOG.debug("С сервера высланы обновленные данные по списку файлов");
        }
        if (msg instanceof FileRequest) {
            FileRequest fr = (FileRequest) msg;
            LOG.debug("Прилетел запрос на загрузку файла {}", fr.getFilename());
            if (!fr.isDelete()) {
                if (Files.exists(Paths.get("server_repo", clientNick, fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get("server_repo", clientNick, fr.getFilename()));
                    ctx.writeAndFlush(fm);
                    LOG.debug("Файл {} отправлен", fm.getFileName());
                } else {
                    LOG.debug("Запрошенного файла {} нет!", fr.getFilename());
                }
            } else {
                LOG.debug("Прилетел запрос на удаление файла {}", fr.getFilename());
                if (Files.exists(Paths.get("server_repo", clientNick, fr.getFilename()))) {
                    Files.delete(Paths.get("server_repo", clientNick, fr.getFilename()));
                    LOG.debug("Файл {} удален", fr.getFilename());
                } else {
                    LOG.debug("Запрошенного на удаление файла {} нет!", fr.getFilename());
                }
            }
        }
        if (msg instanceof RegisterMsg) {

            RegisterMsg register = (RegisterMsg) msg;
            String login = register.getUserName();
            LOG.debug("Прилетел запрос на регистрацию нового пользователя с логином {}", login);
            String password = register.getPassword();
            String firstName = register.getFirstName();
            String lastName = register.getLastName();
            User user = new User(firstName, lastName, login, password);
            if (checkUser(user)) {
                LOG.debug("Пользователь с логином {} уже зергистриован в системе", user.getUserName());
                register.setExist(true);
                ctx.writeAndFlush(register);
            } else {
                db.registerNewUser(user);
                LOG.debug("Новый пользователь {} зарегистрирован", user.getUserName());
                ctx.writeAndFlush(register);
            }
        }
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        LOG.debug("Какая то ошибка");
        ctx.close();
    }

    private boolean checkUser(User user) {
        ResultSet result = db.getUser(user);
        int count = 0;
        try {
            while (result.next()) {
                count++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count >= 1;
    }
}
