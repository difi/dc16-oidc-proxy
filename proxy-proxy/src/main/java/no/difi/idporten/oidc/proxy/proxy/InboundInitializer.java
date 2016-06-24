package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Inject;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;

/**
 * ChannelInitializer for inbound messages.
 */
public class InboundInitializer extends ChannelInitializer<SocketChannel> {

    private SecurityConfigProvider securityConfigProvider;

    private int connections = 64;

    @Inject
    public InboundInitializer(SecurityConfigProvider securityConfigProvider) {
        this.securityConfigProvider = securityConfigProvider;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new HttpServerCodec(102400, 102400, 102400))
                .addLast(new HttpRequestHandler(connections))
                .addLast(new InboundHandler(securityConfigProvider))
        ;
    }
}
