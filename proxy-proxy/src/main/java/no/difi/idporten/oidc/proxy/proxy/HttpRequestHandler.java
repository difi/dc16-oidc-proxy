package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The first handler that can manipulate incoming HTTP messages and change the remote address we send the request to.
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<HttpMessage> {
    private static Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug(String.format("Http handler activating on channel %s", ctx.channel()));
        super.channelActive(ctx);
    }

    /**
     * @param ctx:
     * @param msg: Either a HttpRequest, HttpContent or HttpLastContent object.
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpMessage msg) throws Exception {
        logger.debug(String.format("HttpRequestHandler reading message: %s", msg));
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String host = request.headers().getAsString(HttpHeaderNames.HOST);

            logger.debug(String.format("Trimmed host name: %s", host));
        }
        ctx.fireChannelRead(msg);
    }
}
