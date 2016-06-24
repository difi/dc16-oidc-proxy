package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;

public class OutboundInitializer extends ChannelInitializer<SocketChannel> {

    private Channel inbound;

    public OutboundInitializer(Channel inbound) {
        this.inbound = inbound;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //Create a default pipeline implementation.
        ch.pipeline()
                //Enable HTTPS if necessary.
                .addLast("codec", new HttpClientCodec(102400, 102400, 102400))
                .addLast(new HttpResponseHandler())
                .addLast(new OutboundHandler(inbound))
        ;
    }
}
