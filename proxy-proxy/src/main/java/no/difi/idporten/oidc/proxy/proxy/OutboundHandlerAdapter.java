package no.difi.idporten.oidc.proxy.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handles incoming responses from the outbound server.
 */
public class OutboundHandlerAdapter extends AbstractHandlerAdapter {

    private final Channel inboundChannel;

    private static Logger logger = LoggerFactory.getLogger(OutboundHandlerAdapter.class);

    private ProxyCookie proxyCookie;

    private SecurityConfig securityConfig;

    private boolean setCookie;

    /**
     * @param inboundChannel Channel on which to write responses
     */
    public OutboundHandlerAdapter(Channel inboundChannel, ProxyCookie proxyCookie, SecurityConfig securityConfig, boolean setCookie) {
        logger.info(String.format("Initializing target pool with inbound channel %s", inboundChannel));
        this.inboundChannel = inboundChannel;
        this.proxyCookie = proxyCookie;
        this.setCookie = setCookie;
        this.securityConfig = securityConfig;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info(String.format("Activating outbound channel %s", ctx.channel()));
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
    }


    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse && proxyCookie != null && setCookie) {
            CookieHandler.insertCookieToResponse((HttpResponse) msg,
                    proxyCookie.getName(), proxyCookie.getUuid(), securityConfig.getSalt());
        }

        logger.debug(String.format("Receiving response from server: %s", msg.getClass()));
        inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        closeOnFlush(inboundChannel);
    }
}
