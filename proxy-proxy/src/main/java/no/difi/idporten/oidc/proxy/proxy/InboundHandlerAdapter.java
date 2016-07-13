package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
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

    private ResponseGenerator responseGenerator;

    private ProxyCookie validProxyCookie;

    public InboundHandlerAdapter(SecurityConfigProvider securityConfigProvider) {
        this.securityConfigProvider = securityConfigProvider;
        this.responseGenerator = new ResponseGenerator();
    }

    /**
     * Only activates the channel and starts the first incoming read which is necessary.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info(String.format("Activating source handler channel %s", ctx.channel()));
        ctx.read();
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

        Optional<SecurityConfig> securityConfigOptional = securityConfigProvider.getConfig(host, path);

        if (!securityConfigOptional.isPresent()) {
            logger.debug("Could not get SecurityConfig of host {}", host);
            responseGenerator.generateDefaultResponse(ctx, host);
        }

        securityConfigOptional.ifPresent(securityConfig -> {
            // do this if security config is present (not null)
            logger.debug("Has security config: {}", securityConfig);

            if (securityConfig.isSecured()) { // the requested resource IS secured
                logger.debug("{}{} is secured", host, path);
                CookieHandler cookieHandler = new CookieHandler(securityConfig.getCookieConfig(), host, trimmedPath);

                // getting correct cookie from request
                Optional<ProxyCookie> validProxyCookieOptional = cookieHandler.getValidProxyCookie(httpRequest);

                // This is the expression where a query to the database is necessary
                if (validProxyCookieOptional.isPresent()) {
                    validProxyCookie = validProxyCookieOptional.get();
                    logger.debug("Has validProxyCookie {}", validProxyCookie);
                    logger.debug("Cookie is valid");
                    // we need handle exceptions and nullPointers either in this class or somewhere else
                    // generate a JWTResponse with the user data inside the cookie
                    try {
                        outboundChannel = responseGenerator.generateProxyResponse(ctx, httpRequest,
                                securityConfig, validProxyCookie);
                        return;
                        // stop this function from continuing
                    } catch (Exception exc) {
                        logger.warn("Could not generate ProxyResponse with cookie {} and UserData\n{}", validProxyCookie, validProxyCookie.getUserData());
                        exc.printStackTrace();
                    }
                } else {
                    logger.debug("Could not find valid ProxyCookie in storage");
                }
                Optional<IdentityProvider> idpOptional = securityConfig.createIdentityProvider();
                if (!idpOptional.isPresent()) { // for some reason, the path's IdentityProvider does not exist
                    responseGenerator.generateDefaultResponse(ctx, host);
                }
                idpOptional.ifPresent(idp -> {
                    logger.debug("Has identity provider: {}", idp);
                    if (path.contains("?code=")) {
                        logger.debug("TypesafePathConfig contains code: {}", path);
                        // need to get token here
                        try {
                            HashMap<String, String> userData = idp.getToken(path).getUserData();
                            outboundChannel = responseGenerator.generateProxyResponse(ctx, httpRequest,
                                    securityConfig, cookieHandler.generateCookie(userData));
                        } catch (IdentityProviderException exc) {
                            exc.printStackTrace();
                            responseGenerator.generateDefaultResponse(ctx, host);
                        }
                    } else {
                        // redirect response
                        responseGenerator.generateRedirectResponse(ctx, idp);
                        // should not continue life of request after this
                    }
                });
            } else {
                // path is not secured
                logger.debug("TypesafePathConfig is not secured: {}{}", host, path);
                outboundChannel = responseGenerator.generateProxyResponse(ctx, httpRequest,
                        securityConfig, validProxyCookie);
            }
        });
    }


    /**
     * Reading incoming messages from the local client. The first message for a new incoming connection will bootstrap
     * the outbound channel. The next messages will just be written to the outbound channel.
     */
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug(String.format("Reading incoming request: %s", msg.getClass()));

        // First message is always HttpRequest, use it to bootstrap outbound channel.
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg);
        } else if (outboundChannel != null && outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    // was able to flush out data, start to read the next chunk
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
