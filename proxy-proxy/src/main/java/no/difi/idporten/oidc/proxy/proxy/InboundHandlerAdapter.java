package no.difi.idporten.oidc.proxy.proxy;

import com.google.gson.Gson;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.CookieDecoder;
import io.netty.util.CharsetUtil;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import no.difi.idporten.oidc.proxy.model.UserData;
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
    private CookieInHeader cookieInHeader = new CookieInHeader();
    private CookieStorage cookieStorage = new InMemoryCookieStorage();
    private String cookieName, host, path;

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

    /**
     * Generates redirect response for initial request to server. This is the response containing idp, scope, client_id etc.
     *
     * @return
     */
    private void generateRedirectResponse(ChannelHandlerContext ctx, IdentityProvider identityProvider) {
        try {
            String redirectUrl = identityProvider.generateURI();
            StringBuilder content = new StringBuilder(redirectUrl);
            FullHttpResponse result = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
            result.headers().set(HttpHeaderNames.LOCATION, redirectUrl);
            result.headers().set(HttpHeaderNames.CONTENT_LENGTH, result.content().readableBytes());
            result.headers().set(HttpHeaderNames.CONTENT_TYPE, String.format("%s; %s=%s", HttpHeaderValues.TEXT_PLAIN, HttpHeaderValues.CHARSET, CharsetUtil.UTF_8));
            logger.debug(String.format("Created redirect response:\n%s", result));
            ctx.writeAndFlush(result).addListener(ChannelFutureListener.CLOSE);
        } catch (IdentityProviderException exc) {
            exc.printStackTrace();
            generateDefaultResponse(ctx, "");
        }
    }

    /**
     * Default response for when nothing is configured for the host
     */
    private void generateDefaultResponse(ChannelHandlerContext ctx, String host) {
        System.out.println("NEI");
        StringBuilder content = new StringBuilder();
        content.append(String.format("no cannot use %s", host));
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, String.format("%s; %s=%s", HttpHeaderValues.TEXT_PLAIN, HttpHeaderValues.CHARSET, CharsetUtil.UTF_8));
        logger.debug(String.format("Created default response:\n%s", response));
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void generateJWTResponse(ChannelHandlerContext ctx, UserData userData) throws IdentityProviderException {
        FullHttpResponse result = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(new Gson().toJson(userData.getUserData()), CharsetUtil.UTF_8));
        result.headers().set(HttpHeaderNames.CONTENT_LENGTH, result.content().readableBytes());
        result.headers().set(HttpHeaderNames.CONTENT_TYPE, String.format("%s; %s=%s", HttpHeaderValues.TEXT_PLAIN, HttpHeaderValues.CHARSET, CharsetUtil.UTF_8));
        System.out.println("INSERTING COOKIE");
        CookieInHeader.insertCookieIntoHeader(result, cookieName, cookieStorage.generateCookie(host, userData.getUserData()));
        System.out.println("COOKIE INSERTED");
        logger.debug(String.format("Created JWT response:\n%s", result));
        ctx.writeAndFlush(result).addListener(ChannelFutureListener.CLOSE);
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
            generateDefaultResponse(ctx, host);
        }
        securityConfigOptional.ifPresent(securityConfig -> {
            // do this if security config is present (not null)
            logger.debug("Has security config: {}", securityConfig);
            cookieName = securityConfig.getCookieConfig().getName();

            if (!securityConfig.getSecurity().equals("0")) { // the requested resource IS secured
                logger.debug("{} is secured");
                if (CookieInHeader.checkHeaderForCookie(httpRequest.headers(), cookieName, "Cookie")) {
                    //TODO: Check database for cookieName
                    System.out.println("FOUND COOKIE: " + cookieName);
                }

                // getting correct cookie from request
                String cookieString = httpRequest.headers().get(HttpHeaderNames.COOKIE);
                Set<Cookie> cookieSet = ServerCookieDecoder.STRICT.decode(cookieString);
                Optional<Cookie> nettyCookieOptional = cookieSet.stream()
                        .filter(nettyCookie -> nettyCookie.name().equals(cookieName))
                        .findFirst();


                if (/*request has the cookie we want*/ nettyCookieOptional.isPresent()) {
                    // get CookieObject from database with the uuid in the HttpCookie
                    String UUID = nettyCookieOptional.get().value();
                    // TODO no.difi.idporten.oidc.proxy.model.Cookie cookieObject = CookieStorageInstance.getCookie(UUID)
                    if (/*the CookieObject we found has not expired*/ true) { // TODO cookieObject.isValid() or cookieObject.isExpired()
                        // we need handle exceptions and nullPointers either in this class or somewhere else

                        // generate a JWTResponse with the user data inside the cookie
                        // TODO generateJWTResponse(ctx, cookieObject.getUserData());
                        // update CookieObject's expiry
                        // TODO cookieObject.touch();
                        // stop this function from continuing
                    } else { // we found a cookie, got it from the database, but it is expired
                        // continue the normal flow of authorization with idp
                        // update the current CookieObject or create a new one(?) after
                    }
                } else { // the request does not contain the cookie we want
                    // continue the normal flow of authorization with idp
                    // create a new CookieObject with its own UUID etc. after token is collected
                    // remember to save the new/updated cookie to the database!
                }
                Optional<IdentityProvider> idpOptional = securityConfig.createIdentityProvider();
                if (!idpOptional.isPresent()) { // for some reason, the path's IdentityProvider does not exist
                    generateDefaultResponse(ctx, host);
                }
                idpOptional.ifPresent(idp -> {
                    logger.debug("Has identity provider: {}", idp);
                    if (path.contains("?code=")) {
                        logger.debug("TypesafePathConfig contains code: {}", path);
                        // need to get token here
                        try {
                            generateJWTResponse(ctx, idp.getToken(path));
                        } catch (Exception exc) {
                            exc.printStackTrace();
                            generateDefaultResponse(ctx, "no cannot");
                        }
                    } else {
                        // redirect response
                        generateRedirectResponse(ctx, idp);
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
