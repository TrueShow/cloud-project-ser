import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(AuthHandler.class);
    private DbHandler db;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (DbHandler.getConn() == null) {
            DbHandler.connect();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        db = new DbHandler();
        if (msg instanceof AuthRequest) {
            AuthRequest request = (AuthRequest) msg;
            if (request.isAuthOk()) {
                LOG.debug("Прокинули запрос в следующий Handler");
                ctx.fireChannelRead(msg);
            }
            String login = request.getLogin().trim();
            String password = request.getPassword().trim();

            if (!login.equals("") && !password.equals("")) {

                User user = new User();
                user.setUserName(login);
                user.setPassword(password);
                ResultSet result = db.getUser(user);
                int count = 0;

                try {
                    while (result.next()) {
                        count++;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (count >= 1) {
                    LOG.debug("логин и пароль есть в БД");
                    request.setAuthOk(true);
                    ctx.writeAndFlush(request);
                } else {
                    ctx.writeAndFlush(request);
                }
            } else {
                LOG.debug("Неверный логин или пароль");
                ctx.writeAndFlush(request);
            }
        }
        if (msg instanceof RegisterMsg) {
            RegisterMsg register = (RegisterMsg) msg;
            String login = register.getUserName();
            String password = register.getPassword();
            String firstName = register.getFirstName();
            String lastName = register.getLastName();
            User user = new User(firstName, lastName,login, password);
//            ResultSet result = db.getUser(user);
//            int count = 0;
//
//            try {
//                while (result.next()) {
//                    count++;
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//
//            if (count >= 1) {
//                LOG.debug("логин и пароль есть в БД");
//                register.setExist(true);
//                ctx.writeAndFlush(register);
//            } else {
                db.registerNewUser(user);
                LOG.debug("Новый пользователей зарегистрирован");
                ctx.writeAndFlush(register);
//            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
