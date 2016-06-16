package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServerInitializer extends ChannelInitializer<SocketChannel> {
    private static Logger logger = LoggerFactory.getLogger(ProxyServerInitializer.class);

    private final String hostName;
    private final int port;

    public ProxyServerInitializer(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    @Override
    public void initChannel(SocketChannel channel) {
        logger.info(String.format("Initializing channel on outbound %s:%d", hostName, port));
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new ProxyFrontendHandler(this.hostName, this.port));
    }
}
