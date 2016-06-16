package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServerHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(ProxyServerHandler.class);

    // inboundChannel is the local channel which writes stuff to the localhost
    private final Channel inboundChannel;

    /**
     * The server handler has a channel for the outbound connection to a site like www.ntnu.no in ctx.channel().
     * But it also needs a reference to the local channel which is reached through localhost, in order to write stuff
     * to it.
     * @param inboundChannel Reference to the local channel reachable through localhost.
     */
    public ProxyServerHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.debug(String.format("ProxyServerHandler activated with inbound channel %s", inboundChannel));
        logger.debug(String.format("ProxyServerHandler activated with channel %s", ctx.channel()));
        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        logger.debug("ProxyServerHandler reading");
        logger.debug(String.format("Class of message: %s", msg.getClass()));
        inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    ctx.channel().read();
                } else {
                    channelFuture.channel().close();
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.debug("ProxyServerHandler deactivated");
        ProxyFrontendHandler.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
