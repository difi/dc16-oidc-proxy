package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Inject;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import no.difi.idporten.oidc.proxy.api.ConfigProvider;

/**
 * ChannelInitializer for inbound messages.
 */
public class InboundInitializer extends ChannelInitializer<SocketChannel> {

    private ConfigProvider configProvider;

    private int connections = 64;

    @Inject
    public InboundInitializer(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new HttpServerCodec(102400, 102400, 102400))
                .addLast(new HttpRequestHandler(connections))
                .addLast(new InboundHandler(configProvider))
        ;
    }
}
