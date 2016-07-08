package no.difi.idporten.oidc.proxy.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
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
    private String cookieName, host, path;
    private String trimmedPath; // path without any parameters

    public InboundHandlerAdapter(SecurityConfigProvider securityConfigProvider) {
        this.securityConfigProvider = securityConfigProvider;
        responseGenerator = new ResponseGenerator();
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

        this.path = httpRequest.uri();
        this.trimmedPath = path.contains("?") ? path.split("\\?")[0] : path;
        this.host = httpRequest.headers().getAsString(HttpHeaderNames.HOST);

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
                    ProxyCookie validProxyCookie = validProxyCookieOptional.get();
                    logger.debug("Has validProxyCookie {}", validProxyCookie);

                    logger.debug("Cookie is valid");
                    // we need handle exceptions and nullPointers either in this class or somewhere else

                    // generate a JWTResponse with the user data inside the cookie
                    try {
                        responseGenerator.generateJWTResponse(ctx, validProxyCookie.getUserData(), validProxyCookie);
                        // stop this function from continuing
                    } catch (IdentityProviderException exc) {
                        logger.warn("Could not generate JWTResponse with cookie {} and UserData\n{}", validProxyCookie, validProxyCookie.getUserData());
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
                            // Generating JWT response. CookieHandler creates and saves cookie with CookieStorage
                            // and generateJWTResponse sets the correct 'Set-Cookie' header.
                            responseGenerator.generateJWTResponse(ctx, userData, cookieHandler.generateCookie(userData));
                        } catch (IdentityProviderException exc) {
                            exc.printStackTrace();
                            responseGenerator.generateDefaultResponse(ctx, "no cannot");
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
                bootstrapOutboundChannel(ctx, securityConfig.getBackend(), httpRequest);
            }
        });
    }

    /**
     * This is what happens when the proxy needs to work as a normal proxy.
     * We could also direct IDP traffic this way instead of the the apache.http.HttpClient, but then we would need
     * SSL set up.
     *
     * @param ctx
     * @param outboundAddress
     * @param httpRequest
     */
    private void bootstrapOutboundChannel(ChannelHandlerContext ctx, SocketAddress outboundAddress, HttpRequest httpRequest) {
        logger.info(String.format("Bootstrapping channel %s", ctx.channel()));
        final Channel inboundChannel = ctx.channel();


        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass());
        b.handler(new OutboundInitializer(inboundChannel))
                .option(ChannelOption.AUTO_READ, false);

        b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        b.option(ChannelOption.TCP_NODELAY, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000);

        b.option(ChannelOption.SO_SNDBUF, 1048576);
        b.option(ChannelOption.SO_RCVBUF, 1048576);

        ChannelFuture f = b.connect(outboundAddress);

        outboundChannel = f.channel();
        logger.debug(String.format("Made outbound channel: %s", outboundChannel));
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                logger.debug("Outbound channel operation complete");
                if (future.isSuccess()) {
                    // connection complete start to read first data
                    logger.debug("Outbound channel operation success");
                    outboundChannel.writeAndFlush(httpRequest).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {

                            if (future.isSuccess()) {
                                // was able to flush out data, start to read the next chunk
                                ctx.channel().read();
                            } else {
                                future.channel().close();
                            }
                        }
                    });
                } else {
                    // Close the connection if the connection attempt has failed.
                    logger.debug("Outbound channel operation failure");
                    inboundChannel.close();
                }
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
