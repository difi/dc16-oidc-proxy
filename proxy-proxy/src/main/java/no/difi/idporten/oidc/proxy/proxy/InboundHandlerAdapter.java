package no.difi.idporten.oidc.proxy.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.*;
import io.netty.handler.codec.http.cookie.Cookie;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import no.difi.idporten.oidc.proxy.storage.InMemoryCookieStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Optional;
import java.util.Set;

/**
 * Handler for incoming requests. This handler creates the channel which connects to a outbound server.
 */
public class InboundHandlerAdapter extends AbstractHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(InboundHandlerAdapter.class);

    private volatile Channel outboundChannel;

    private SecurityConfigProvider securityConfigProvider;
    private ResponseGenerator responseGenerator;
    private CookieHandler cookieHandler = new CookieHandler();
    private CookieStorage cookieStorage = new InMemoryCookieStorage();
    private String cookieName, host, path;

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
        this.host = httpRequest.headers().getAsString(HttpHeaderNames.HOST);

        // host = "www.difi.no"; // edit host here if you want to test different configurations

        Optional<SecurityConfig> securityConfigOptional = securityConfigProvider.getConfig(host, path);

        if (!securityConfigOptional.isPresent()) {
            logger.debug("Could not get SecurityConfig of host {}", host);
            responseGenerator.generateDefaultResponse(ctx, host);
        }
        securityConfigOptional.ifPresent(securityConfig -> {
            // do this if security config is present (not null)
            logger.debug("Has security config: {}", securityConfig);

            if (!securityConfig.getSecurity().equals("0")) { // the requested resource IS secured
                logger.debug("{}{} is secured", host, path);
                /* Probably refactor this. Currently all cookie handling in this method.
                if (CookieHandler.checkHeaderForCookie(httpRequest.headers(), cookieName, "DefaultProxyCookie")) {
                    //TODO: Check database for cookieName
                    System.out.println("FOUND COOKIE: " + cookieName);
                }
                */


                CookieConfig cookieConfig = securityConfig.getCookieConfig();
                cookieName = cookieConfig.getName();
                CookieStorage cookieStorage = cookieConfig.getCookieStorage();

                // getting correct cookie from request
                Optional<Cookie> nettyCookieOptional = Optional.empty();
                if (httpRequest.headers().contains(HttpHeaderNames.COOKIE)) {

                    String cookieString = httpRequest.headers().get(HttpHeaderNames.COOKIE);
                    Set<Cookie> cookieSet = ServerCookieDecoder.STRICT.decode(cookieString);
                    nettyCookieOptional = cookieSet.stream()
                            .filter(nettyCookie -> nettyCookie.name().equals(cookieName))
                            .findFirst();
                }


                if (/*request has the cookie we want*/ nettyCookieOptional.isPresent()) {
                    logger.debug("HTTP request has the cookie we are looking for", nettyCookieOptional.get());
                    // get CookieObject from database with the uuid in the HttpCookie
                    String UUID = nettyCookieOptional.get().value();
                    Optional<ProxyCookie> proxyCookieOptional = cookieStorage.findCookie(UUID, host);
                    if (!proxyCookieOptional.isPresent()) {
                        logger.warn("Could not find cookie {}@{}", UUID, host);
                        // Cookie contains an UUID, but it is not found in the storage.
                        // This is an exception and it means something is wrong with either getting cookies or
                        // creating and writing UUIDs  ...or the user is messing with us
                        // TODO Create new expired cookie with the UUID we found?
                    } else {
                        ProxyCookie proxyCookieObject = proxyCookieOptional.get();
                        logger.debug("Has proxyCookieObject {}", proxyCookieObject);

                        if (/*the CookieObject we found has not expired*/ proxyCookieObject.isValid()) {
                            logger.debug("Cookie is valid");
                            // we need handle exceptions and nullPointers either in this class or somewhere else

                            // generate a JWTResponse with the user data inside the cookie
                            try {
                                responseGenerator.generateJWTResponse(ctx, proxyCookieObject.getUserData());
                            } catch (IdentityProviderException exc) {
                                logger.warn("Could not generate JWTResponse with cookie {} and UserData\n{}", proxyCookieObject, proxyCookieObject.getUserData());
                                exc.printStackTrace();
                            }
                            // update CookieObject's expiry
                            cookieStorage.saveOrUpdateCookie(proxyCookieObject);
                            // stop this function from continuing
                        } else { // we found a cookie, got it from the database, but it is expired
                            logger.debug("Cookie is not valid");
                            // continue the normal flow of authorization with idp
                            // update the current CookieObject or create a new one(?) after
                        }
                    }
                } else { // the request does not contain the cookie we want
                    // continue the normal flow of authorization with idp
                    // create a new CookieObject with its own UUID etc. after token is collected
                    // remember to save the new/updated cookie to the database!
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
                            responseGenerator.generateJWTResponse(ctx, idp.getToken(path).getUserData());
                            // Generating new cookie and saving it
                            // Remember to put a Set-Cookie in the response
                            cookieStorage.saveOrUpdateCookie(cookieStorage.generateCookieAsObject(host, idp.getToken(path).getUserData()));
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
