package no.difi.idporten.oidc.proxy.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
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
    protected void generateRedirectResponse(ChannelHandlerContext ctx, IdentityProvider identityProvider, SecurityConfig securityConfig, String requestPath) {
        try {
            String redirectUrl = identityProvider.generateRedirectURI();

            StringBuilder content = new StringBuilder(redirectUrl);

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.FOUND, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));

            response.headers().set(HttpHeaderNames.LOCATION, redirectUrl);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, String.format(
                    "%s; %s=%s", HttpHeaderValues.TEXT_PLAIN, HttpHeaderValues.CHARSET, CharsetUtil.UTF_8));

            new RedirectCookieHandler(
                    securityConfig.getCookieConfig(),
                    securityConfig.getHostname(),
                    requestPath).insertCookieToResponse(response, securityConfig.getSalt());

            logger.debug(String.format("Created redirect response:\n%s", response));
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

        } catch (IdentityProviderException exc) {
            exc.printStackTrace();
            generateDefaultResponse(ctx, "");
        }
    }

    /**
     * Default response for when nothing is configured for the host
     */
    protected void generateDefaultResponse(ChannelHandlerContext ctx, String host) {
        String content = String.format("Unknown host:  %s", host);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.BAD_REQUEST,
                Unpooled.copiedBuffer(HTMLGenerator.getErrorPage(content.toString()), CharsetUtil.UTF_8));

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
            RequestInterceptor.insertUserDataToHeader(httpRequest, proxyCookie.getUserData(), securityConfig);
        }

        Channel outboundChannel;
        logger.info(String.format("Bootstrapping channel %s", ctx.channel()));
        final Channel inboundChannel = ctx.channel();

        boolean setCookie = proxyCookie != null;

        RedirectCookieHandler.findRedirectCookiePath(httpRequest, securityConfig.getSalt()).ifPresent(originalPath -> {
            logger.debug("Changing path of request because we found the original path: {}", originalPath);
            httpRequest.setUri(originalPath);
            logger.debug(httpRequest.toString());
        });


        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass());
        b.handler(new OutboundInitializer(inboundChannel, proxyCookie, setCookie, securityConfig))
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
