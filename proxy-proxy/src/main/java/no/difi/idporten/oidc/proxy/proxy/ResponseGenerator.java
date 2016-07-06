package no.difi.idporten.oidc.proxy.proxy;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.HashMap;

public class ResponseGenerator {

    private static Logger logger = LoggerFactory.getLogger(InboundHandlerAdapter.class);


    /**
     * Generates redirect response for initial request to server. This is the response containing idp, scope, client_id etc.
     *
     * @return
     */
    protected void generateRedirectResponse(ChannelHandlerContext ctx, IdentityProvider identityProvider) {
        try {
            String redirectUrl = identityProvider.generateRedirectURI();
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
    protected void generateDefaultResponse(ChannelHandlerContext ctx, String host) {
        StringBuilder content = new StringBuilder();
        content.append(String.format("no cannot use %s", host));
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.BAD_REQUEST,
                Unpooled.copiedBuffer(HTMLGenerator.getErrorPage(content.toString()), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(
                HttpHeaderNames.CONTENT_TYPE,
                String.format("%s; %s=%s", HttpHeaderValues.TEXT_PLAIN, HttpHeaderValues.CHARSET, CharsetUtil.UTF_8));
        logger.debug(String.format("Created default response:\n%s", response));
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Generates and writes an appropriate JSON response based on userData with a correct 'Set-Cookie' header.
     * @param ctx
     * @param userData
     * @param proxyCookieObject
     * @throws IdentityProviderException
     */
    protected void generateJWTResponse(ChannelHandlerContext ctx, HashMap<String, String> userData, ProxyCookie proxyCookieObject) throws IdentityProviderException {
        FullHttpResponse result = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(new Gson().toJson(userData), CharsetUtil.UTF_8));
        result.headers().set(HttpHeaderNames.CONTENT_LENGTH, result.content().readableBytes());
        result.headers().set(HttpHeaderNames.CONTENT_TYPE, String.format("%s; %s=%s", HttpHeaderValues.TEXT_PLAIN, HttpHeaderValues.CHARSET, CharsetUtil.UTF_8));
        logger.debug("Setting Set-Cookie to the response");
        CookieHandler.insertCookieToResponse
                (result, proxyCookieObject.getName(), proxyCookieObject.getUuid());
        logger.debug(String.format("Created JWT response:\n%s", result));
        ctx.writeAndFlush(result).addListener(ChannelFutureListener.CLOSE);
    }

    protected void generateUnsecuredRespone(ChannelHandlerContext ctx, SocketAddress socketAddress, HttpRequest httpRequest){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, false);
        //response.headers().set(HttpHeaderNames.LOCATION, socketAddress);
        //response.headers().set(HttpHeaderNames.HOST, "localhost:8080");
        logger.debug(String.format("Created unsecured configured response: %s", response));
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

    }
}
