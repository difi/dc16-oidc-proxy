package no.difi.idporten.oidc.proxy.proxy;/*
/**
* Thanks to https://github.com/sandamal/NettyRP and
* https://github.com/carrot-garden/net_netty/tree/master/example/src/main/java/io/netty/example/proxy for providing
* the skeleton for this code.
*/

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Start ServerBootStrap in a given port for http inbound connections
 */
public class NettyHttpListener {
    private static Logger logger = LoggerFactory.getLogger(NettyHttpListener.class);


    private int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private static String host;

    private static int HOST_PORT;

    private int bossGroupSize;
    private int workerGroupSize;

    private int maxConnectionsQueued;

    private boolean SSL;

    private EventLoopGroup commonEventLoopGroup;

    public NettyHttpListener(int port) {
        this.port = port;
    }

    public NettyHttpListener() {
        /* Backend Address */
        //host = "10.243.220.64";
        host = "www.ulv.no";
        HOST_PORT = 80;

        /* Reverse Proxy port */
        this.port = 8080;

        SSL = false;
        port = SSL ? 8443 : port;

        this.bossGroupSize = 1;
        this.workerGroupSize = 8;

        this.maxConnectionsQueued = 64;
    }

    public static void setHost(String host) {
        NettyHttpListener.host = host;
    }

    protected static SocketAddress getTargetSocketAddress() {
        return new InetSocketAddress(host, HOST_PORT);
    }

    public void start() {

        System.out.println("Starting the server...");
        System.out.println("Starting Inbound Http Listener on Port " + this.port);

        // Configure SSL.
        SslContext sslCtx = null;
        /*
        if (SSL) {
            try {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
            } catch (CertificateException ex) {
                Logger.getLogger(NettyHttpListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SSLException ex) {
                Logger.getLogger(NettyHttpListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        */

        commonEventLoopGroup = new NioEventLoopGroup(bossGroupSize);
//        bossGroup = new NioEventLoopGroup(bossGroupSize);
//        workerGroup = new NioEventLoopGroup(workerGroupSize);

        try {
            ServerBootstrap b = new ServerBootstrap();

//            b.commonEventLoopGroup(bossGroup, workerGroup)
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

            Channel ch = null;
            try {
                ch = b.bind(port).sync().channel();
                ch.closeFuture().sync();
                System.out.println("Inbound Listener Started");
            } catch (InterruptedException e) {
                System.out.println("Exception caught");
            }
        } finally {
            logger.info("Shutting down");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void destroy() {
//        bossGroup.shutdownGracefully();
//        workerGroup.shutdownGracefully();
        commonEventLoopGroup.shutdownGracefully();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
