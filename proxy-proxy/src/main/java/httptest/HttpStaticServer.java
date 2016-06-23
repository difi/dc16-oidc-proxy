package httptest;

/**
 * Created with IntelliJ IDEA.
 * User: I
 * Date: 06.07.13
 * Time: 22:51
 * To change this template use File | Settings | File Templates.
 */

import httptest.Model.ChannelInfo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.List;

public class HttpStaticServer {

    private static int port;
    private static Calendar serverStartTime;
    public static final List<ChannelInfo> channelsInfo = Collections.synchronizedList(new ArrayList<ChannelInfo>());

    public HttpStaticServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        serverStartTime = Calendar.getInstance();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpStaticServerInitializer());

            b.bind(port).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        if(Desktop.isDesktopSupported())
        {
            Desktop.getDesktop().browse(new URI("http://www.localhost:8080"));
        }
        new HttpStaticServer(port).run();
        System.out.println("fuj");
    }

    public static int getPort(){
        return port;
    }

    public static Calendar getServerStartTime(){
        return serverStartTime;
    }
}