package no.difi.idporten.oidc.proxy.proxy;

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

/**
 * Start ServerBootStrap in a given port for http inbound connections
 */
public class NettyHttpListener implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(NettyHttpListener.class);

    private int port = 8080;

    private int bossGroupSize = 1;

    private int maxConnectionsQueued = 64;

    private EventLoopGroup commonEventLoopGroup;

    private InboundInitializer inboundInitializer;

    @Inject
    private NettyHttpListener(Config config, InboundInitializer inboundInitializer) {
        this.inboundInitializer = inboundInitializer;

        port = config.getInt("listen.port");
    }

    @Deprecated
    public NettyHttpListener() {
        // No action.
    }

    public void run() {
        logger.info("Starting the server...");

        commonEventLoopGroup = new NioEventLoopGroup(bossGroupSize);
        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(commonEventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(inboundInitializer)
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
        }
    }

    public void destroy() {
        commonEventLoopGroup.shutdownGracefully();
    }
}
