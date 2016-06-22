package no.difi.idporten.oidc.proxy.proxy;
/*
* Thanks to https://github.com/sandamal/NettyRP and
* https://github.com/carrot-garden/net_netty/tree/master/example/src/main/java/io/netty/example/proxy for providing
* the skeleton for this code.
*/

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Start ServerBootStrap in a given port for http inbound connections
 */
public class NettyHttpListener implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(NettyHttpListener.class);

    private static int HOST_PORT = 80;
    private static String host = "www.ulv.no";

    protected static SocketAddress getTargetSocketAddress() {
        return new InetSocketAddress(host, HOST_PORT);
    }

    public static void setHost(String host) {
        NettyHttpListener.host = host;
    }


    private int port = 8080;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private int bossGroupSize = 1;
    private int workerGroupSize = 8;

    private int maxConnectionsQueued = 64;

    private EventLoopGroup commonEventLoopGroup;

    @Inject
    private NettyHttpListener(Config config) {
        port = config.getInt("listen.port");
    }

    public NettyHttpListener() {
        // No action.
    }

    public void run() {
        logger.info("Starting the server...");

        commonEventLoopGroup = new NioEventLoopGroup(bossGroupSize);
//      bossGroup = new NioEventLoopGroup(bossGroupSize);
//      workerGroup = new NioEventLoopGroup(workerGroupSize);

        try {
            ServerBootstrap b = new ServerBootstrap();

//          b.commonEventLoopGroup(bossGroup, workerGroup)
            b.group(commonEventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new TransportFrontendInitializer(maxConnectionsQueued))
                    .childOption(ChannelOption.AUTO_READ, false);

            b.option(ChannelOption.TCP_NODELAY, true);
            b.childOption(ChannelOption.TCP_NODELAY, true);

            b.option(ChannelOption.SO_BACKLOG, maxConnectionsQueued);

            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000);

            b.option(ChannelOption.SO_SNDBUF, 1048576);
            b.option(ChannelOption.SO_RCVBUF, 1048576);
            b.childOption(ChannelOption.SO_RCVBUF, 1048576);
            b.childOption(ChannelOption.SO_SNDBUF, 1048576);

            b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            try {
                Channel channel = b.bind(port).sync().channel();

                logger.info("Starting Inbound Http Listener on port {}.", this.port);
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                logger.info("Interuption received.");
            }
        } finally {
            logger.info("Shutting down.");
            // bossGroup.shutdownGracefully();
            // workerGroup.shutdownGracefully();
        }
    }

    public void destroy() {
//        bossGroup.shutdownGracefully();
//        workerGroup.shutdownGracefully();
        commonEventLoopGroup.shutdownGracefully();
    }
}
