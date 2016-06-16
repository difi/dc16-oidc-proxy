package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServerInitializer extends ChannelInitializer<SocketChannel> {
    private static Logger logger = LoggerFactory.getLogger(ProxyServerInitializer.class);


    @Override
    public void initChannel(SocketChannel channel) {
        logger.info("Initializing channel");
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new ProxyFrontendHandler("www.ntnu.no", 80));
    }
}
