package no.difi.idporten.oidc.proxy.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handles incoming responses from the outbound server.
 */
public class OutboundHandlerAdapter extends AbstractHandlerAdapter {

    private final Channel inboundChannel;

    private final boolean logout;

    private SecurityConfig securityConfig;

    private ProxyCookie proxyCookie;

    private static Logger logger = LoggerFactory.getLogger(OutboundHandlerAdapter.class);

    /**
     * @param inboundChannel Channel on which to write responses
     */
    public OutboundHandlerAdapter(Channel inboundChannel, SecurityConfig securityConfig, ProxyCookie proxyCookie, boolean logout) {
        logger.debug(String.format("Initializing target pool with inbound channel %s", inboundChannel));
        this.inboundChannel = inboundChannel;
        this.securityConfig = securityConfig;
        this.proxyCookie = proxyCookie;
        this.logout = logout;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Activating outbound channel {}", ctx.channel());
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse && proxyCookie != null && logout) {
            CookieHandler.deleteCookieFromBrowser(proxyCookie.getName(), (HttpResponse) msg);
            logger.debug("Deleted cookie ({}) from user's browser after logout proxy request and passing on response from service");
        }
        if (msg instanceof HttpResponse && securityConfig.getLogoutHeader() != null && ((HttpResponse) msg).headers().contains(securityConfig.getLogoutHeader())) {
            if (proxyCookie != null && ((HttpResponse) msg).headers().get(securityConfig.getLogoutHeader()).equals("true")) {
                new CookieHandler(securityConfig.getCookieConfig(), securityConfig.getHostname(), securityConfig.getPreferredIdpData()).removeCookie(proxyCookie.getUuid());
                CookieHandler.deleteCookieFromBrowser(proxyCookie.getName(), ((HttpResponse) msg));
                logger.info("Cookie ({}) deleted when response from host contained logoutHeader ({}: true)", proxyCookie, securityConfig.getLogoutHeader());
            }
        }
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
