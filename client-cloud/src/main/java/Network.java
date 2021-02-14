import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Network {
    private SocketChannel channel;
    private static final String host = "localhost";
    private static final int port = 8189;
    private Callback callback;
    private static Network network;
    private static final Logger LOG = LoggerFactory.getLogger(Network.class);


    public static Network getInstance(Callback callback) {
        if (network == null) {
            network = new Network(callback);
        }
        return network;
    }

    private Network(Callback callback) {
        this.callback = callback;
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
                                        new ClientHandler(callback)
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

    public void sendObj(AbstractMessage msg) {
        channel.writeAndFlush(msg);
    }

    public void close() {
        if (channel.isActive()) {
            channel.close();
            LOG.debug("Канал клиент Netty закрыт");
        }
        else LOG.debug("Соединение не установлено");
    }

    public SocketChannel getChannel() {
        return channel;
    }
}
