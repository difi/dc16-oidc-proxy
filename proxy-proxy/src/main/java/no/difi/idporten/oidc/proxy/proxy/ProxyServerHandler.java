package no.difi.idporten.oidc.proxy.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServerHandler extends SimpleChannelInboundHandler<Object> {

    private static Logger logger = LoggerFactory.getLogger(ProxyServerHandler.class);

    private HttpRequest httpRequest;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Object msg) throws Exception {
        logger.info("Received ({}): {}", msg.getClass(), msg);

        if (msg instanceof HttpRequest) {
            httpRequest = (HttpRequest) msg;
        } else if (msg instanceof HttpContent) {
            if (msg instanceof LastHttpContent) {
                FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                httpResponse.content().writeBytes(String.format("Hello %s!", httpRequest.uri()).getBytes());

                context.write(httpResponse);
                context.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
    }
}
