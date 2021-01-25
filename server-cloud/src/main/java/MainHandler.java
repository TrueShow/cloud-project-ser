import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(MainHandler.class);
    private static int counter = 0;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("Клиент отключился, осталось подключено - {} клиентов", --counter);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        counter++;
        LOG.debug("Клиент подключился, подключено - {} клиентов", counter);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOG.debug("Прилетел запрос, объект - {}", msg.getClass());
        if (msg instanceof FileMessage) {
            FileMessage fm = (FileMessage)msg;
            LOG.debug("Это файл с именем {}",fm.getFileName());
            Files.write(Paths.get("/server_repo/" + fm.getFileName()), fm.getData(), StandardOpenOption.CREATE);
            LOG.debug("Файл {} получен",fm.getFileName());
        }
        if (msg instanceof ListFileRequest) {
            List<String> listOfFiles = Files.list(Paths.get("server_repo/"))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
            ctx.writeAndFlush(new ListFileRequest(listOfFiles));
            LOG.debug("С сервера высланы обновленные данные по списку файлов");
        }
        if (msg instanceof FileRequest) {
            FileRequest fr = (FileRequest) msg;
            if (Files.exists(Paths.get("server_repo/" + fr.getFilename()))) {
                FileMessage fm = new FileMessage(Paths.get("server_repo/" + fr.getFilename()));
                ctx.writeAndFlush(fm);
                LOG.debug("Файл {} отправлен",fm.getFileName());
            } else {
                LOG.debug("Запрошенного файла {} нет!", fr.getFilename());
            }
        }
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
