package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OIDCHttpFiltersSourceAdapter extends HttpFiltersSourceAdapter {

    private static Logger logger = LoggerFactory.getLogger(OIDCHttpFiltersSourceAdapter.class);

    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        return new HttpFiltersAdapter(originalRequest) {
            /**
             * Intercept an incoming request from the client here
             * @param httpObject
             * @return
             */
            @Override
            public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                // TODO: implement your filtering here
                logger.info(String.format("Http message coming from client to proxy:\n%s", httpObject));
                return null;
            }

            /**
             * Intercept a returning response coming from the server here
             * @param httpObject
             * @return
             */
            @Override
            public HttpObject serverToProxyResponse(HttpObject httpObject) {
                // TODO: implement your filtering here
                logger.info(String.format("Http message coming from server to proxy:\n%s", httpObject));
                return httpObject;
            }
        };
    }
}
