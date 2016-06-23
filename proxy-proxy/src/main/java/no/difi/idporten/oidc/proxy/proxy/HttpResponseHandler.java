package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseHandler extends ChannelInboundHandlerAdapter {
    Logger logger = LoggerFactory.getLogger(HttpResponseHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info(String.format("Channel active %s", ctx.channel()));

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info(String.format("Reading incoming response %s", msg.getClass()));
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            // logger.info(String.format("Message is HttpResponse\n%s", response));
            CookieInHeader.insertCookieIntoHeader(response, "This is a session ID", "This is a UUID");
        }
        super.channelRead(ctx, msg);
    }
}
