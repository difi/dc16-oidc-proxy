package no.difi.idporten.oidc.proxy.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ResponseGenerator {

    public static final AsciiString TEXT_HTML = new AsciiString("text/html");

    public static final AsciiString APPLICATION_JSON = new AsciiString("application/json");

    private static Logger logger = LoggerFactory.getLogger(InboundHandlerAdapter.class);


    /**
     * Generates redirect response for initial request to server. This is the response containing idp, scope,
     * client_id etc.
     *
     * @return
     */
    protected void generateRedirectResponse(
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
                    httpRequest.headers().get("User-Agent"));

            logger.debug(String.format("Created redirect response:\n%s", response));
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

        } catch (IdentityProviderException exc) {
            exc.printStackTrace();
            generateServerErrorResponse(ctx, String.format("Could not create redirect response to %s", securityConfig.getIdp()));
        }
    }

    protected void generateLogoutResponse(ChannelHandlerContext ctx, SecurityConfig securityConfig) {
        logger.debug("ResponseGenerator.generateLogoutResponse()");
        try {
            String redirectUrl = securityConfig.getLogoutRedirectUri();
            logger.debug("logoutRedirectUri: {}", redirectUrl);

            StringBuilder content = new StringBuilder(redirectUrl);

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.FOUND, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));

            response.headers().set(HttpHeaderNames.LOCATION, redirectUrl);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, String.format(
                    "%s; %s=%s", HttpHeaderValues.TEXT_PLAIN, HttpHeaderValues.CHARSET, CharsetUtil.UTF_8));

            logger.debug(String.format("Created logout response:\n%s", response));
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    protected void generateServerErrorResponse(ChannelHandlerContext ctx, String message) {
        generateDefaultResponse(ctx, message, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    protected void generateUnknownHostResponse(ChannelHandlerContext ctx, String message) {
        generateDefaultResponse(ctx, message, HttpResponseStatus.BAD_REQUEST);
    }

    /**
     * Generates redirect response with a 'Set-Cookie' header for a ProxyCookie.
     * Is used when doing a second redirect to the original path after successfully logging in.
     *
     * @return
     */
    protected void generateRedirectResponse(ChannelHandlerContext ctx,
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
        response.headers().set(HttpHeaderNames.SET_COOKIE,
                ServerCookieEncoder.STRICT.encode(proxyCookie.getName(), proxyCookie.getUuid()));

        CookieHandler.insertCookieToResponse(
                response,
                proxyCookie.getName(),
                proxyCookie.getUuid(),
                securityConfig.getSalt(),
                httpRequest.headers().getAsString(HttpHeaderNames.USER_AGENT));


        logger.debug(String.format("Created redirect response:\n%s", response));
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Default response for when nothing is configured for the host
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


    /**
     * This is what happens when the proxy needs to work as a normal proxy.
     * We could also direct IDP traffic this way instead of the the apache.http.HttpClient, but then we would need
     * SSL set up.
     *
     * @param ctx:
     * @param securityConfig:
     * @param httpRequest:
     * @param proxyCookie:
     */

    public Channel generateProxyResponse(ChannelHandlerContext ctx, HttpRequest httpRequest,
                                         SecurityConfig securityConfig, ProxyCookie proxyCookie) {
        int connect_timeout_millis = 15000;
        int so_buf = 1048576;

        if (proxyCookie != null && !securityConfig.isTotallyUnsecured(httpRequest.uri())) {
            RequestInterceptor.insertUserDataToHeader(httpRequest, proxyCookie.getUserData());
        }

        Channel outboundChannel;
        logger.debug(String.format("Bootstrapping channel %s", ctx.channel()));
        final Channel inboundChannel = ctx.channel();

        boolean setCookie = proxyCookie != null;

        RedirectCookieHandler.findRedirectCookiePath(httpRequest,
                securityConfig.getSalt(),
                httpRequest.headers().get("User-Agent")).ifPresent(originalPath -> {
            logger.debug("Changing path of request because we found the original path: {}", originalPath);
            httpRequest.setUri(originalPath);
            logger.debug(httpRequest.toString());
        });



        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass());
        b.handler(new OutboundInitializer(inboundChannel, proxyCookie, setCookie, securityConfig, httpRequest))
                .option(ChannelOption.AUTO_READ, false);

        b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        b.option(ChannelOption.TCP_NODELAY, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connect_timeout_millis);

        b.option(ChannelOption.SO_SNDBUF, so_buf);
        b.option(ChannelOption.SO_RCVBUF, so_buf);

        ChannelFuture f = b.connect(securityConfig.getBackend());

        outboundChannel = f.channel();
        logger.debug(String.format("Made outbound channel: %s", outboundChannel));
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.debug("Outbound channel operation success");
                    outboundChannel.writeAndFlush(httpRequest).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                ctx.channel().read();
                            } else {
                                future.channel().close();
                            }
                        }
                    });
                } else {
                    logger.debug("Outbound channel operation failure");
                    inboundChannel.close();
                }
            }
        });
        return outboundChannel;

    }

    /**
     * Help method for generateProxyResponse.
     * Checks if the path is unsecured and should not receive the userdata.
     *
     * @param unsecuredPaths:
     * @param path:
     * @return
     */

    private boolean checkForUnsecuredPaths(List<String> unsecuredPaths, String path) {
        return unsecuredPaths.stream().filter(path::startsWith).findFirst().isPresent();
    }


}
