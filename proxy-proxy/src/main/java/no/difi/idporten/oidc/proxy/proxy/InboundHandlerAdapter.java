package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;

/**
 * Handler for incoming requests. This handler creates the channel which connects to a outbound server.
 */
public class InboundHandlerAdapter extends AbstractHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(InboundHandlerAdapter.class);

    private volatile Channel outboundChannel;

    private SecurityConfigProvider securityConfigProvider;

    private ProxyCookie proxyCookie;

    public InboundHandlerAdapter(SecurityConfigProvider securityConfigProvider) {
        this.securityConfigProvider = securityConfigProvider;
    }

    /**
     * Only activates the channel and starts the first incoming read which is necessary.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info(String.format("Activating source handler channel %s", ctx.channel()));
        ctx.read();
    }

    private boolean redirectedFromIdp(String path) {
        return (path.contains("?code="));
    }


    /**
     * Bootstraps the backend channel which is the one connected to the outbound server. If the connection is
     * successful, it writes the first message to the outbound server and starts reading the first response back to the
     * source client.
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest httpRequest) throws Exception {
        logger.info("Handle HTTP request '{}{}'", httpRequest.headers().getAsString(HttpHeaderNames.HOST), httpRequest.uri());

        String path = httpRequest.uri();
        String trimmedPath = path.contains("?") ? path.split("\\?")[0] : path;
        String host = httpRequest.headers().getAsString(HttpHeaderNames.HOST);

        ResponseGenerator responseGenerator = new ResponseGenerator();

        Optional<SecurityConfig> securityConfigOptional = securityConfigProvider.getConfig(host, path);


        try {
            if (securityConfigOptional.isPresent()) {
                SecurityConfig securityConfig = securityConfigOptional.get();
                CookieHandler cookieHandler = new CookieHandler(securityConfig.getCookieConfig(), host, trimmedPath);
                Optional<ProxyCookie> validProxyCookieOptional = cookieHandler.getValidProxyCookie(httpRequest, securityConfig.getSalt());

                logger.debug("Has security config: {}", securityConfig);

                if (securityConfig.isSecured()) {
                    Optional<IdentityProvider> idpOptional = securityConfig.createIdentityProvider();

                    logger.debug("{}{} is secured", host, path);

                    if (validProxyCookieOptional.isPresent()) {
                        logger.debug("Has valid ProxyCookie {}", proxyCookie);

                        proxyCookie = validProxyCookieOptional.get();
                        outboundChannel = responseGenerator.generateProxyResponse(ctx, httpRequest, securityConfig, proxyCookie);

                    } else if (idpOptional.isPresent()) {
                        IdentityProvider idp = idpOptional.get();

                        logger.debug("Has identity provider: {}", idp);

                        if (redirectedFromIdp(path)) {
                            logger.debug("TypesafePathConfig contains code: {}", path);

                            HashMap<String, String> userData = idp.getToken(path).getUserData();

                            int maxExpiry = securityConfig.getCookieConfig().getMaxExpiry();
                            int touchPeriod = securityConfig.getCookieConfig().getTouch();

                            proxyCookie = cookieHandler.generateCookie(userData, touchPeriod, maxExpiry);
                            outboundChannel = responseGenerator.generateProxyResponse(ctx, httpRequest, securityConfig, proxyCookie);
                        } else {
                            responseGenerator.generateRedirectResponse(ctx, idp, securityConfig, httpRequest.uri());
                        }
                    } else {
                        responseGenerator.generateDefaultResponse(ctx, host);
                    }
                } else {
                    logger.debug("TypesafePathConfig is not secured: {}{}", host, path);

                    if (validProxyCookieOptional.isPresent()) {
                        proxyCookie = validProxyCookieOptional.get();
                    }
                    outboundChannel = responseGenerator.generateProxyResponse(ctx, httpRequest, securityConfig, proxyCookie);
                }
            } else {
                logger.debug("Could not get SecurityConfig of host {}", host);
                responseGenerator.generateDefaultResponse(ctx, host);
            }

        } catch (Exception e) {
            responseGenerator.generateDefaultResponse(ctx, host);
        }

    }


    /**
     * Reading incoming messages from the local client. The first message for a new incoming connection will bootstrap
     * the outbound channel. The next messages will just be written to the outbound channel.
     */
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug(String.format("Reading incoming request: %s", msg.getClass()));

        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg);
        } else if (outboundChannel != null && outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        } else {
            logger.debug("Outbound Channel Not Active");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Channel Inactive");
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }
}
