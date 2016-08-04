package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;

public class OutboundInitializer extends ChannelInitializer<SocketChannel> {

    private Channel inbound;

    private final boolean logout;

    public OutboundInitializer(Channel inbound, boolean logout) {
        this.inbound = inbound;
        this.logout = logout;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast("codec", new HttpClientCodec(102400, 102400, 102400))
                .addLast(new HttpResponseHandler())
                .addLast(new OutboundHandlerAdapter(inbound, logout))
        ;
    }
}
