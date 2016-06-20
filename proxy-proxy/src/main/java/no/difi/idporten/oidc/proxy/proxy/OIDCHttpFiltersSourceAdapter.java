package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeSet;

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
                if (httpObject instanceof HttpRequest) {
                    logger.debug("Got HTTP request");

                    HttpRequest request = (HttpRequest) httpObject;

                    if (request.headers().contains(HttpHeaderNames.COOKIE)) {
                        if (!hasIdProxyCookie(request)) {
                            logger.debug(String.format("Request does not have ID proxy cookie so we redirect it to xkcd"));
                            insertIdProxyCookie(request);
                            redirectRequest(request);
                        }
                        TreeSet<Cookie> cookies = (TreeSet<Cookie>) ServerCookieDecoder.LAX.decode(request.headers().get(HttpHeaderNames.COOKIE));
                        logger.debug(String.format("HTTP request has cookies:\n%s", cookies));
                    }
                }
                HttpRequest newRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "http://www.example.com/");
                newRequest.headers().set(HttpHeaderNames.HOST, "www.example.com");

                return proxyToServerRequest(newRequest);

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

            /**
             * Changes Host header and URI to redirect a HTTP request when it enters the proxy.
             * @param request
             */
            private void redirectRequest(HttpRequest request) {
                request.headers().set(HttpHeaderNames.HOST, "www.xkcd.com");
                request.setUri("http://xkcd.com/");
                logger.debug(String.format("Manipulated request:\n%s", request));
            }

            /**
             *
             * @param request
             * @return Whether a request contains an IDProxy cookie.
             */
            private boolean hasIdProxyCookie(HttpRequest request) {
                return request.headers().get(HttpHeaderNames.COOKIE).contains(OIDCConstants.IDPROXY);
            }

            /**
             *
             * @param request
             */
            private void insertIdProxyCookie(HttpRequest request) {
                TreeSet<Cookie> cookies = (TreeSet<Cookie>) ServerCookieDecoder.LAX.decode(request.headers().get(HttpHeaderNames.COOKIE));
                cookies.add(new DefaultCookie(OIDCConstants.IDPROXY.toString(), "123abc"));
                request.headers().set(HttpHeaderNames.COOKIE, cookies);
            }
        };
    }
}
