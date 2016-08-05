package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

public class OutboundInitializer extends ChannelInitializer<SocketChannel> {

    private Channel inbound;

    private SecurityConfig securityConfig;

    private ProxyCookie proxyCookie;

    private final boolean logout;

    public OutboundInitializer(Channel inbound, SecurityConfig securityConfig, ProxyCookie proxyCookie, boolean logout) {
        this.inbound = inbound;
        this.securityConfig = securityConfig;
        this.logout = logout;
        this.proxyCookie = proxyCookie;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast("codec", new HttpClientCodec(102400, 102400, 102400))
                .addLast(new HttpResponseHandler())
                .addLast(new OutboundHandlerAdapter(inbound, securityConfig, proxyCookie, logout))
        ;
    }
}
