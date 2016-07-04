package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;

import java.util.Optional;
import java.util.Set;


public class CookieHandler {

    /**
     * Inserts a cookie with a cookieName fetched from the configuration, and a uuid from generating
     * the cookie, into the response's header. The response is sent to the client and the cookie is
     * stored int he clients browser.
     * @param httpResponse
     * @param cookieName
     * @param uuid
     */
    public static void insertCookieIntoHeader(HttpResponse httpResponse, String cookieName, String uuid) {
        httpResponse.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookieName, uuid));
    }

    /**
     * Checks either e response or a request for a specific cookie.
     * @param httpHeaders
     * @param sessionID
     * @param headerField
     * @return
     */
    public static boolean checkHeaderForCookie(HttpHeaders httpHeaders, String sessionID, String headerField) {
        if (httpHeaders.contains(headerField)) {
            if (httpHeaders.get(headerField).contains(sessionID)) {
                return true;
            }
        }
        return false;
    }

    protected static Optional<Cookie> getCookieFromRequest(HttpRequest httpRequest, String cookieName) {
        if (httpRequest.headers().contains(HttpHeaderNames.COOKIE)) {

            String cookieString = httpRequest.headers().get(HttpHeaderNames.COOKIE);
            Set<Cookie> cookieSet = ServerCookieDecoder.STRICT.decode(cookieString);
            return cookieSet.stream()
                    .filter(nettyCookie -> nettyCookie.name().equals(cookieName))
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

}