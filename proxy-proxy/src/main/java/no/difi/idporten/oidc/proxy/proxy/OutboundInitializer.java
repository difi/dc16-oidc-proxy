package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;

public class OutboundInitializer extends ChannelInitializer<SocketChannel> {

    private Channel inbound;

    private ProxyCookie proxyCookie;

    private boolean setCookie;

    public OutboundInitializer(Channel inbound, ProxyCookie proxyCookie, boolean setCookie) {
        this.inbound = inbound;
        this.proxyCookie = proxyCookie;
        this.setCookie = setCookie;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //Create a default pipeline implementation.
        ch.pipeline()
                //Enable HTTPS if necessary.
                .addLast("codec", new HttpClientCodec(102400, 102400, 102400))
                .addLast(new HttpResponseHandler())
                .addLast(new OutboundHandlerAdapter(inbound, proxyCookie, setCookie))
        ;
    }
}
