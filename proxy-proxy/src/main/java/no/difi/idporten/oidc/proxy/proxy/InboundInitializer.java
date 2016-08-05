package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * ChannelInitializer for inbound messages.
 */
public class InboundInitializer extends ChannelInitializer<SocketChannel> {

    private Injector injector;

    @Inject
    public InboundInitializer(Injector injector) {
        this.injector = injector;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new HttpServerCodec(102400, 102400, 102400))
                .addLast(injector.getInstance(InboundHandlerAdapter.class))
        ;
    }
}
