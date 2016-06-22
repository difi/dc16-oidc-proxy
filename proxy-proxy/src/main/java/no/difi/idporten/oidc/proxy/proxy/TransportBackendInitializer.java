package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportBackendInitializer extends ChannelInitializer<SocketChannel> {

    private static Logger logger = LoggerFactory.getLogger(TransportFrontendHandler.class);

    private Channel inbound;

    public TransportBackendInitializer(Channel inbound) {
        this.inbound = inbound;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //Create a default pipeline implementation.
        ChannelPipeline p = ch.pipeline();

        //Enable HTTPS if necessary.
        p.addLast("codec", new HttpClientCodec(102400, 102400, 102400));
        p.addLast(new HttpResponseHandler());
        p.addLast(new TransportBackendHandler(inbound));
    }
}
