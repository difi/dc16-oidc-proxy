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

    private ProxyCookie validProxyCookie;

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

        Optional<SecurityConfig> securityConfigOptional = securityConfigProvider.getConfig(host, path);


        /////////////////////////////////////////////////////////////////////////

        try {
            if (securityConfigOptional.isPresent()) {
                securityConfigOptional.ifPresent(securityConfig -> {
                    logger.debug("Has security config: {}", securityConfig);

                    if (securityConfig.isSecured()) {
                        logger.debug("{}{} is secured", host, path);
                        CookieHandler cookieHandler = new CookieHandler(securityConfig.getCookieConfig(), host, trimmedPath);

                        Optional<ProxyCookie> validProxyCookieOptional = cookieHandler.getValidProxyCookie(httpRequest);

                        if (validProxyCookieOptional.isPresent()) {
                            validProxyCookie = validProxyCookieOptional.get();
                            logger.debug("Has validProxyCookie {}", validProxyCookie);
                            logger.debug("Cookie is valid");

                            try {
                                outboundChannel = ResponseGenerator.generateProxyResponse(ctx, httpRequest,
                                        securityConfig, validProxyCookie);
                                return;
                            } catch (Exception exc) {
                                logger.warn("Could not generate ProxyResponse with cookie {} and UserData\n{}", validProxyCookie, validProxyCookie.getUserData());
                                exc.printStackTrace();
                            }
                        } else {
                            logger.debug("Could not find valid ProxyCookie in storage");
                        }
                        Optional<IdentityProvider> idpOptional = securityConfig.createIdentityProvider();
                        if (!idpOptional.isPresent()) {
                            ResponseGenerator.generateDefaultResponse(ctx, host);
                        }
                        idpOptional.ifPresent(idp -> {
                            logger.debug("Has identity provider: {}", idp);
                            if (path.contains("?code=")) {
                                logger.debug("TypesafePathConfig contains code: {}", path);
                                try {
                                    HashMap<String, String> userData = idp.getToken(path).getUserData();
                                    outboundChannel = ResponseGenerator.generateProxyResponse(ctx, httpRequest,
                                            securityConfig, cookieHandler.generateCookie(userData));
                                } catch (IdentityProviderException exc) {
                                    exc.printStackTrace();
                                    ResponseGenerator.generateDefaultResponse(ctx, host);
                                }
                            } else {
                                ResponseGenerator.generateRedirectResponse(ctx, idp);
                            }
                        });
                    } else {
                        logger.debug("TypesafePathConfig is not secured: {}{}", host, path);
                        outboundChannel = ResponseGenerator.generateProxyResponse(ctx, httpRequest,
                                securityConfig, validProxyCookie);
                    }
                });



                /*SecurityConfig securityConfig = securityConfigOptional.get();

                if (securityConfig.isSecured()) {
                    CookieHandler cookieHandler = new CookieHandler(securityConfig.getCookieConfig(), host, trimmedPath);
                    Optional<ProxyCookie> validProxyCookieOptional = cookieHandler.getValidProxyCookie(httpRequest);
                    Optional<IdentityProvider> idpOptional = securityConfig.createIdentityProvider();

                    if (validProxyCookieOptional.isPresent()) {
                        proxyCookie = validProxyCookieOptional.get();

                    } else if (idpOptional.isPresent()) {
                        System.out.println("Is secured");
                        IdentityProvider idp = idpOptional.get();
                        HashMap<String, String> userData = idp.getToken(path).getUserData();
                        proxyCookie = cookieHandler.generateCookie(userData);

                        if (redirectedFromIdp(path)) {
                            outboundChannel = ResponseGenerator.generateProxyResponse(ctx, httpRequest, securityConfig, proxyCookie);
                        } else {
                            ResponseGenerator.generateRedirectResponse(ctx, idp);
                        }
                    } else {
                        ResponseGenerator.generateDefaultResponse(ctx, host);
                    }
                } else {
                    outboundChannel = ResponseGenerator.generateProxyResponse(ctx, httpRequest, securityConfig, proxyCookie);
                }
            } else {
                logger.debug("Could not get SecurityConfig of host {}", host);
                ResponseGenerator.generateDefaultResponse(ctx, host);
            }*/
            }

        } catch (Exception e){
            System.out.println("EXCEPTION YO");
            ResponseGenerator.generateDefaultResponse(ctx, host);
        }


        /////////////////////////////////////////////////////////////////////////



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
