package no.difi.idporten.oidc.proxy.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handles incoming responses from the outbound server.
 */
public class OutboundHandlerAdapter extends AbstractHandlerAdapter {

    private final Channel inboundChannel;

    private static Logger logger = LoggerFactory.getLogger(OutboundHandlerAdapter.class);

    /**
     * @param inboundChannel Channel on which to write responses
     */
    public OutboundHandlerAdapter(Channel inboundChannel) {
        logger.debug(String.format("Initializing target pool with inbound channel %s", inboundChannel));
        this.inboundChannel = inboundChannel;

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Activating outbound channel {}", ctx.channel());
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
    }


    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug(String.format("Receiving response from server: %s", msg.getClass()));
        inboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                ctx.channel().read();
            } else {
                future.channel().close();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        closeOnFlush(inboundChannel);
    }
}
