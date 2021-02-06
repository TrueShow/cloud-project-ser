import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(MainHandler.class);
    private DbHandler db;
    private String clientNick;
    private Path clientPath;
    private  static int cnt = 0;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("Клиент отключился, осталось подключено - {} клиентов", --cnt);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("Клиент подключился, подключено - {} клиентов", cnt);
        db = new DbHandler();
        clientNick = "user" + cnt;
        clientPath = Paths.get("server_repo", clientNick);
        if (!Files.exists((clientPath))) {
            Files.createDirectory(clientPath);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOG.debug("Прилетел запрос, объект - {}", msg.getClass());
        if (msg instanceof FileMessage) {
            FileMessage fm = (FileMessage) msg;
            LOG.debug("Это файл с именем {}", fm.getFileName());
            Files.write(clientPath.resolve(fm.getFileName()),fm.getData());
            LOG.debug("Файл {} получен", fm.getFileName());
        }
        if (msg instanceof ListFileRequest) {
            ctx.writeAndFlush(new ListFileRequest(clientPath));
            LOG.debug("С сервера высланы обновленные данные по списку файлов");
        }
        if (msg instanceof FileRequest) {
            FileRequest fr = (FileRequest) msg;
            if (!fr.isDelete()) {
                if (Files.exists(Paths.get("server_repo", clientNick, fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get("server_repo", clientNick, fr.getFilename()));
                    ctx.writeAndFlush(fm);
                    LOG.debug("Файл {} отправлен", fm.getFileName());

                } else {
                    LOG.debug("Запрошенного файла {} нет!", fr.getFilename());
                }
            } else {
                if (Files.exists(Paths.get("server_repo", clientNick, fr.getFilename()))) {
                    Files.delete(Paths.get("server_repo", clientNick, fr.getFilename()));
                    LOG.debug("Файл {} удален", fr.getFilename());

                } else {
                    LOG.debug("Запрошенного файла {} нет!", fr.getFilename());
                }
            }


        }
//        if (msg instanceof RegisterMsg) {
//            RegisterMsg rm = (RegisterMsg) msg;

        //     db.registerNewUser(user);
//        }
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        LOG.debug("Какая то ошибка");
        ctx.close();
    }
}
