import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.ReferenceCountUtil;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Network {
    private SocketChannel channel;
    private static final String host = "localhost";
    private static final int port = 8189;

    private static final Logger LOG = LoggerFactory.getLogger(Network.class);

    private static Network network;
    private Controller controller;

    private Network(Controller controller) {
        this.controller = controller;
    }

    public static Network getInstance(Controller controller) {
        if (network == null) {
            network = new Network(controller);
        }
        return network;
    }

    public void launch() {
        Thread thread = new Thread(() -> {
            NioEventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                channel = socketChannel;
                                socketChannel.pipeline().addLast(
                                        new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)),
                                        new ObjectEncoder(),
                                        new ClientHandler()
                                );
                            }
                        });
                ChannelFuture f = b.connect(host, port).sync();
                LOG.debug("Клиент Netty стартанул");

                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void close() {
        channel.close();
    }

    public void sendObj(AbstractMessage msg) {
        channel.writeAndFlush(msg);
    }

    private class ClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                if (msg instanceof ListFileRequest) {
                    ListFileRequest lfr = (ListFileRequest) msg;
                    Platform.runLater(() -> {
                        controller.filesListCloud.getItems().clear();
                        controller.list = lfr.getList();
                        controller.list.forEach(o -> controller.filesListCloud.getItems().add(o));
                    });

                    LOG.debug("Список файлов обновлен");
                }
                if (msg instanceof FileMessage) {
                    FileMessage fm = (FileMessage) msg;
                    Files.write(Paths.get("client_repo/" + fm.getFileName()), fm.getData(), StandardOpenOption.CREATE);
                    Platform.runLater(() -> {
                        controller.refreshLocalFilesList();
                    });

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
    }
}
