//package temp_proto;
//
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.codec.serialization.ClassResolvers;
//import io.netty.handler.codec.serialization.ObjectDecoder;
//import io.netty.handler.codec.serialization.ObjectEncoder;
//import io.netty.util.ReferenceCountUtil;
//import javafx.application.Platform;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.nio.file.StandardOpenOption;
//
//public class NetworkOld {
//    private SocketChannel channel;
//    private static final String host = "localhost";
//    private static final int port = 8189;
//
//    private static final Logger LOG = LoggerFactory.getLogger(NetworkOld.class);
//
//    private static NetworkOld networkOld;
//    private ControllerOld controllerOld;
//
//    NetworkOld(ControllerOld controllerOld) {
//        this.controllerOld = controllerOld;
//    }
//
//    public static NetworkOld getInstance(ControllerOld controllerOld) {
//        if (networkOld == null) {
//            networkOld = new NetworkOld(controllerOld);
//        }
//        return networkOld;
//    }
//
//    public void launch() {
//        Thread thread = new Thread(() -> {
//            NioEventLoopGroup workerGroup = new NioEventLoopGroup();
//            try {
//                Bootstrap b = new Bootstrap();
//                b.group(workerGroup)
//                        .channel(NioSocketChannel.class)
//                        .handler(new ChannelInitializer<SocketChannel>() {
//                            @Override
//                            protected void initChannel(SocketChannel socketChannel) throws Exception {
//                                channel = socketChannel;
//                                socketChannel.pipeline().addLast(
//                                        new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)),
//                                        new ObjectEncoder(),
//                                        new ClientHandler()
//                                );
//                            }
//                        });
//                ChannelFuture f = b.connect(host, port).sync();
//                LOG.debug("Клиент Netty стартанул");
//
//                f.channel().closeFuture().sync();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } finally {
//                workerGroup.shutdownGracefully();
//            }
//        });
//        thread.setDaemon(true);
//        thread.start();
//    }
//
//    public SocketChannel getChannel() {
//        return channel;
//    }
//
//    public void close() {
//        channel.close();
//    }
//
//    public void sendObj(AbstractMessage msg) {
//        channel.writeAndFlush(msg);
//    }
//
//    private class ClientHandler extends ChannelInboundHandlerAdapter {
//        @Override
//        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            try {
//                if (msg instanceof ListFileRequest) {
//                    ListFileRequest lfr = (ListFileRequest) msg;
//                    Platform.runLater(() -> {
//                        controllerOld.filesListCloud.getItems().clear();
//                        controllerOld.list = lfr.getList();
//                        controllerOld.list.forEach(o -> controllerOld.filesListCloud.getItems().add(o));
//                    });
//
//                    LOG.debug("Список файлов обновлен");
//                }
//                if (msg instanceof FileMessage) {
//                    FileMessage fm = (FileMessage) msg;
//                    Platform.runLater(() -> {
//                        try {
//                            FileChooser fc = new FileChooser();
//                            fc.setInitialFileName(fm.getFileName());
//                            File save = fc.showSaveDialog(new Stage());
//                            LOG.debug("Запрос на сохранение файл {}", fm.getFileName());
//                            if (save != null) {
//                                save = new File(save.getAbsolutePath());
//                                Files.write(Paths.get(save.getAbsolutePath()), fm.getData(), StandardOpenOption.CREATE);
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    });
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            ReferenceCountUtil.release(msg);
//        }
//
//        @Override
//        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//            cause.printStackTrace();
//            ctx.close();
//        }
//    }
//}
