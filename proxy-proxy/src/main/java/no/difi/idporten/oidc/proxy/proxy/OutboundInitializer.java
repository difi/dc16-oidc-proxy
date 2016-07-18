package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

public class OutboundInitializer extends ChannelInitializer<SocketChannel> {

    private Channel inbound;

    private ProxyCookie proxyCookie;

    private boolean setCookie;

    private SecurityConfig securityConfig;

    public OutboundInitializer(Channel inbound, ProxyCookie proxyCookie, boolean setCookie, SecurityConfig securityConfig) {
        this.inbound = inbound;
        this.proxyCookie = proxyCookie;
        this.setCookie = setCookie;
        this.securityConfig = securityConfig;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //Create a default pipeline implementation.
        ch.pipeline()
                //Enable HTTPS if necessary.
                .addLast("codec", new HttpClientCodec(102400, 102400, 102400))
                .addLast(new HttpResponseHandler())
                .addLast(new OutboundHandlerAdapter(inbound, proxyCookie, securityConfig, setCookie))
        ;
    }
}
