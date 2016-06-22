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


    private final int maxConnectionsQueued;

    public HttpRequestHandler(int maxConnectionsQueued) {
        this.maxConnectionsQueued = maxConnectionsQueued;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info(String.format("Http handler activating on channel %s", ctx.channel()));
        super.channelActive(ctx);
    }

    /**
     * Currently configured to connect to xkcd for demonstration purposes.
     *
     * @param ctx
     * @param msg Either a HttpRequest, HttpContent or HttpLastContent object.
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpMessage msg) throws Exception {
        logger.debug(String.format("HttpRequestHandler reading message: %s", msg));
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String host = request.headers().get(HttpHeaderNames.HOST).replaceAll(":\\d+", "").trim();
            logger.debug(String.format("Trimmed host name: %s", host));
            if (host.equals("www.xkcd.com")) {
                logger.debug("Host equals www.xkcd.com - changing connection to xkcd's IP address.");
                NettyHttpListener.setHost("23.235.37.67");
            } else if (host.equals("localhost")) {
                logger.debug("Host equals localhost - changing connection to xkcd's IP address.");
                msg.headers().set(HttpHeaderNames.HOST, "www.xkcd.com");
                NettyHttpListener.setHost("23.235.37.67");
            }
        }

        // This notifies the next handler in the pipeline that this message is read and ready to move on
        ctx.fireChannelRead(msg);
    }
}
