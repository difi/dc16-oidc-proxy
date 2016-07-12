package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.CookieConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;


public class CookieHandler {

    private static Logger logger = LoggerFactory.getLogger(CookieHandler.class);

    private final CookieStorage cookieStorage;
    private final CookieConfig cookieConfig;
    private final String cookieName;
    private final String host;
    private final String path;

    /**
     * Instantiates a new CookieHandler based on some parameters from a HTTP request much like a SecurityConfig
     * @param cookieConfig
     * @param host
     * @param path
     */
    public CookieHandler(CookieConfig cookieConfig, String host, String path) {
        this.cookieConfig = cookieConfig;
        this.cookieStorage = cookieConfig.getCookieStorage();
        this.cookieName = cookieConfig.getName();
        this.host = host;
        this.path = path;
    }

    public ProxyCookie generateCookie(HashMap<String, String> userData, int touchPeriod, int maxExpiry) {
//        System.out.println("\nCookieHandler.generateCookie(HashMap<String, String> userData, int touchPeriod, int maxExpiry)");
        return cookieStorage.generateCookieInDb(cookieName, host, path, touchPeriod, maxExpiry, userData);
    }

    /**
     * Convenient function for getting a valid proxy cookie or an empty optional which eases the flow of InboundHandler.
     * @param httpRequest
     * @return
     */
    public Optional<ProxyCookie> getValidProxyCookie(HttpRequest httpRequest) {
        logger.debug("Looking for cookie with name {}", cookieName);

        // get CookieObject from database with the uuid in the HttpCookie
        Optional<Cookie> nettyCookieOptional = getCookieFromRequest(httpRequest);
        if (nettyCookieOptional.isPresent()) {
            String uuid = nettyCookieOptional.get().value();
            logger.debug("HTTP request has the cookie we are looking for", nettyCookieOptional.get());
            Optional<ProxyCookie> proxyCookieOptional = cookieStorage.findCookie(uuid, host, path);
            // cookieStorage.findCookie() validates on expiry, maxExpiry, host and path, so if a
            // cookie is present in the optional, it is also a valid cookie
            if (proxyCookieOptional.isPresent()) {
                    return proxyCookieOptional;
            } else {
                logger.warn("Could not find valid cookie {}@{}{}", uuid, host, path);
                // Cookie contains an UUID, but is either not found in the storage or not valid.
            }
        } else {
            logger.debug("Http request does not contain cookie {}", cookieName);
        }
        return Optional.empty();
    }

    /**
     * Inserts a cookie with a cookieName fetched from the configuration, and a uuid from generating
     * the cookie, into the response's header. The response is sent to the client and the cookie is
     * stored int he clients browser.
     *
     * @param httpResponse
     * @param cookieName
     * @param uuid
     */
    public static void insertCookieToResponse(HttpResponse httpResponse, String cookieName, String uuid) {
        httpResponse.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookieName, uuid));
    }

    /**
     * Checks either e response or a request for a specific cookie.
     *
     * @param httpHeaders
     * @param sessionID
     * @param headerField
     * @return
     */
    public boolean checkHeaderForCookie(HttpHeaders httpHeaders, String sessionID, String headerField) {
        if (httpHeaders.contains(headerField)) {
            if (httpHeaders.get(headerField).contains(sessionID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Looks for a cookie with the name of this CookieHandler's cookieName in a request and returns a Netty cookie
     * object or an empty optional.
     * @param httpRequest
     * @return
     */
    private Optional<Cookie> getCookieFromRequest(HttpRequest httpRequest) {
        if (httpRequest.headers().contains(HttpHeaderNames.COOKIE)) {

            String cookieString = httpRequest.headers().getAsString(HttpHeaderNames.COOKIE);
            Set<Cookie> cookieSet = ServerCookieDecoder.STRICT.decode(cookieString);
            return cookieSet.stream()
                    .filter(nettyCookie -> nettyCookie.name().equals(cookieName))
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

}