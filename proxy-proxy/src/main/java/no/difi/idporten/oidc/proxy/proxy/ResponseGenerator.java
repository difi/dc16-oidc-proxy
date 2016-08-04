package no.difi.idporten.oidc.proxy.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ResponseGenerator {

    public static final AsciiString TEXT_HTML = new AsciiString("text/html");

    private static Logger logger = LoggerFactory.getLogger(InboundHandlerAdapter.class);


    /**
     * Generates redirect response for initial request to server. This is the response containing idp, scope,
     * client_id etc.
     */
    protected void generateRedirectToIdentityProviderResponse(
            ChannelHandlerContext ctx,
            IdentityProvider identityProvider,
            SecurityConfig securityConfig,
            String requestPath,
            HttpRequest httpRequest) {
        try {
            String redirectUrl = identityProvider.generateRedirectURI();

            StringBuilder content = new StringBuilder(redirectUrl);

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.FOUND, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));

            response.headers().set(HttpHeaderNames.LOCATION, redirectUrl);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, String.format(
                    "%s; %s=%s", HttpHeaderValues.TEXT_PLAIN, HttpHeaderValues.CHARSET, CharsetUtil.UTF_8));

            new RedirectCookieHandler(requestPath)
                    .insertCookieToResponse(
                            response,
                            securityConfig.getSalt(),
                            httpRequest.headers().getAsString(HttpHeaderNames.USER_AGENT));

            logger.debug(String.format("Created redirect response:\n%s", response));
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

        } catch (IdentityProviderException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            generateServerErrorResponse(ctx, String.format("Could not create redirect response to %s", securityConfig.getIdp()));
        }
    }

    protected void generateLogoutProxyResponse(ChannelHandlerContext ctx, SecurityConfig securityConfig,
                                                       HttpRequest httpRequest, ProxyCookie proxyCookie) {
            generateProxyResponse(ctx, httpRequest, securityConfig, proxyCookie.getUserData(), true);
    }

    protected void generateLogoutRedirectResponse(ChannelHandlerContext ctx, SecurityConfig securityConfig,
                                                                                   ProxyCookie proxyCookie) {
        try {
            String redirectUrl = securityConfig.getLogoutRedirectUri();

            StringBuilder content = new StringBuilder(redirectUrl);

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.FOUND, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));

            if (proxyCookie != null) {
                CookieHandler.deleteCookieFromBrowser(proxyCookie.getName(), response);
            }

            response.headers().set(HttpHeaderNames.LOCATION, redirectUrl);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, String.format(
                    "%s; %s=%s", HttpHeaderValues.TEXT_PLAIN, HttpHeaderValues.CHARSET, CharsetUtil.UTF_8));

            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }


    /**
     * Generates a response when theres a problem with the server.
     *
     * @param ctx:
     * @param message:
     */

    protected void generateServerErrorResponse(ChannelHandlerContext ctx, String message) {
        generateDefaultResponse(ctx, message, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param ctx:
     * @param message:
     */

    protected void generateUnknownHostResponse(ChannelHandlerContext ctx, String message) {
        generateDefaultResponse(ctx, message, HttpResponseStatus.BAD_REQUEST);
    }

    /**
     * Generates redirect response with a 'Set-Cookie' header for a ProxyCookie.
     * Is used when doing a second redirect to the original path after successfully logging in.
     *
     * @return
     */
    protected void generateRedirectBackToOriginalPathResponse(ChannelHandlerContext ctx,
                                                              SecurityConfig securityConfig,
                                                              HttpRequest httpRequest,
                                                              String redirectUrlPath,
                                                              ProxyCookie proxyCookie) {
        StringBuilder content = new StringBuilder(httpRequest.setUri(redirectUrlPath).uri());

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.FOUND, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));

        response.headers().set(HttpHeaderNames.LOCATION, httpRequest.setUri(redirectUrlPath).uri());
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, String.format(
                "%s; %s=%s", HttpHeaderValues.TEXT_PLAIN, HttpHeaderValues.CHARSET, CharsetUtil.UTF_8));

        if (proxyCookie != null) {

            CookieHandler.insertCookieToResponse(
                    response,
                    proxyCookie.getName(),
                    proxyCookie.getUuid(),
                    securityConfig.getSalt(),
                    httpRequest.headers().getAsString(HttpHeaderNames.USER_AGENT));
        }
        RedirectCookieHandler.deleteRedirectCookieFromBrowser(response);


        logger.debug(String.format("Created redirect response:\n%s", response));
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Default response for when nothing is configured for the requested host
     */
    protected void generateDefaultResponse(ChannelHandlerContext ctx, String message, HttpResponseStatus responseStatus) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                responseStatus,
                Unpooled.copiedBuffer(HTMLGenerator.getErrorPage(message), CharsetUtil.UTF_8));

        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(
                HttpHeaderNames.CONTENT_TYPE,
                String.format("%s; %s=%s", TEXT_HTML, HttpHeaderValues.CHARSET, CharsetUtil.UTF_8));

        logger.debug(String.format("Created default response:\n%s", response));

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    public Channel generateProxyResponse(ChannelHandlerContext ctx, HttpRequest httpRequest,
                                         SecurityConfig securityConfig) {
        return generateProxyResponse(ctx, httpRequest, securityConfig, new HashMap<>(), false);
    }

    public Channel generateProxyResponse(ChannelHandlerContext ctx, HttpRequest httpRequest,
                                         SecurityConfig securityConfig, ProxyCookie proxyCookie) {
        return generateProxyResponse(ctx, httpRequest, securityConfig, proxyCookie.getUserData(), false);
    }

    /**
     * This is what happens when the proxy needs to work as a normal proxy.
     * We could also direct IDP traffic this way instead of the the apache.http.HttpClient, but then we would need
     * SSL set up.
     *
     * @param ctx:
     * @param securityConfig:
     * @param httpRequest:
     * @param userData:
     */
    public Channel generateProxyResponse(ChannelHandlerContext ctx, HttpRequest httpRequest,
                                         SecurityConfig securityConfig, Map<String, String> userData, boolean logout) {
        int connect_timeout_millis = 15000;
        int so_buf = 1048576;

        RequestInterceptor.insertUserDataToHeader(httpRequest, userData, securityConfig);
        logger.debug("UserData inserted to response: " + userData);

        Channel outboundChannel;
        logger.debug(String.format("Bootstrapping channel %s", ctx.channel()));
        final Channel inboundChannel = ctx.channel();

        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass());
        b.handler(new OutboundInitializer(inboundChannel, logout))
                .option(ChannelOption.AUTO_READ, false);

        b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        b.option(ChannelOption.TCP_NODELAY, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connect_timeout_millis);

        b.option(ChannelOption.SO_SNDBUF, so_buf);
        b.option(ChannelOption.SO_RCVBUF, so_buf);

        ChannelFuture f = b.connect(securityConfig.getBackend());

        outboundChannel = f.channel();
        logger.debug(String.format("Made outbound channel: %s", outboundChannel));
        f.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.debug("Outbound channel operation success");
                outboundChannel.writeAndFlush(httpRequest).addListener((ChannelFutureListener) future1 -> {
                    if (future1.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        future1.channel().close();
                    }
                });
            } else {
                logger.debug("Outbound channel operation failure");
                inboundChannel.close();
            }
        });
        return outboundChannel;

    }

}
