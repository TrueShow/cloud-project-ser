import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(LoginHandler.class);
    private DbHandler db;
    private String clientNick;
    private Path clientPath;
    private static int cnt = 0;

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
            AuthRequest answer = (AuthRequest) msg;
            String login = answer.getLogin().trim();
            String password = answer.getPassword().trim();
            clientNick = login;
            if (!login.equals("") && !password.equals("")) {
                User user = new User();
                user.setUserName(login);
                user.setPassword(password);
                if (checkUser(user)) {
                    LOG.debug("логин и пароль есть в БД");
                    clientPath = Paths.get("server_repo", clientNick);
                    Paths.get("server_repo", clientNick);
                    if (!Files.exists((clientPath))) {
                        Files.createDirectory(clientPath);
                    }
                    answer.setAuthOk(true);
                    ctx.writeAndFlush(answer);
                    ctx.pipeline().addLast(new MainHandler());
                    ctx.pipeline().remove(this);
                    ctx.fireChannelRead(msg);
                } else {
                    ctx.writeAndFlush(answer);
                }
            } else {
                LOG.debug("Неверный логин или пароль");
                ctx.writeAndFlush(answer);
            }
        }
        if (msg instanceof RegisterMsg) {
            RegisterMsg register = (RegisterMsg) msg;
            String login = register.getUserName();
            String password = register.getPassword();
            String firstName = register.getFirstName();
            String lastName = register.getLastName();
            User user = new User(firstName, lastName, login, password);
            if (checkUser(user)) {
                LOG.debug("логин и пароль есть в БД");
                register.setExist(true);
                ctx.writeAndFlush(register);
            } else {
                db.registerNewUser(user);
                LOG.debug("Новый пользователей зарегистрирован");
                ctx.writeAndFlush(register);
            }
        }
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
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
