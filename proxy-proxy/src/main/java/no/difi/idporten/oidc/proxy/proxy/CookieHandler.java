package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.CookieConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class CookieHandler {

    private static Logger logger = LoggerFactory.getLogger(CookieHandler.class);

    private final CookieStorage cookieStorage;

    private final String cookieName;

    private final String host;

    private final String path;

    /**
     * Instantiates a new CookieHandler based on some parameters from a HTTP request much like a SecurityConfig
     *
     * @param cookieConfig
     * @param host
     * @param path
     */
    public CookieHandler(CookieConfig cookieConfig, String host, String path) {
        this.cookieStorage = cookieConfig.getCookieStorage();
        this.cookieName = cookieConfig.getName();
        this.host = host;
        this.path = path;
    }

    public ProxyCookie generateCookie(HashMap<String, String> userData, int touchPeriod, int maxExpiry) {
//        System.out.println("\nCookieHandler.generateCookie(HashMap<String, String> userData, int touchPeriod, int maxExpiry)");
        System.err.println("\nCookieHandler.generateCookie()");
        return cookieStorage.generateCookieInDb(cookieName, host, path, touchPeriod, maxExpiry, userData);
    }

    /**
     * Convenient function for getting a valid proxy cookie or an empty optional which eases the flow of InboundHandler.
     *
     * @param httpRequest
     * @return
     */

    public Optional<ProxyCookie> getValidProxyCookie(HttpRequest httpRequest) {
        System.err.println("\nCookieHandler.getValidProxyCookie()");
        System.err.println("cookieName: "+cookieName+"\ncookieStorage: "+cookieStorage);
        System.err.println("host: "+host+"\npath: "+path);
        logger.debug("Looking for cookie with name {}", cookieName);

        Optional<List<String>> cookieOptional = getCookiesFromRequest(httpRequest, cookieName);
        if (cookieOptional.isPresent()) {
            Optional<ProxyCookie> pc;
            for (int i = 0; i < cookieOptional.get().size(); i++){
                pc = cookieStorage.findCookie(cookieOptional.get().get(i), host, path);
                if (pc.isPresent()) {
                    System.err.println("Valid cookie was found: "+pc.get().getUuid());
                    return pc;
                }
            }
        }
        System.err.println("Valid cookie was not found");
        logger.debug("Http request does not contain cookie {}", cookieName);
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
        System.err.println("\nCookieHandler.insertCookieToResponse(): (uuid: "+uuid+")");
        httpResponse.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookieName, uuid));
    }

    /**
     * Looks for a cookie with the name of this CookieHandler's cookieName in a request and returns a Netty cookie
     * object or an empty optional.
     *
     * @param httpRequest
     * @return
     */
    private Optional<Cookie> getCookieFromRequest(HttpRequest httpRequest) {
        return getCookieFromRequest(httpRequest, cookieName);
    }

    /**
     * Looks for a cookie with the name of this CookieHandler's cookieName in a request and returns a Netty cookie
     * object or an empty optional.
     *
     * @param httpRequest
     * @return
     */
    public static Optional<Cookie> getCookieFromRequest(HttpRequest httpRequest, String cookieName) {
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

    public static Optional<List<String>> getCookiesFromRequest(HttpRequest httpRequest, String cookieName) {
        if (httpRequest.headers().contains(HttpHeaderNames.COOKIE)) {
            System.err.println("HttpHeaderNames.COOKIE: "+HttpHeaderNames.COOKIE);
            System.err.println("cookieName: "+cookieName);

            List<String> uuids = new ArrayList<>();

            String cookieString = httpRequest.headers().getAsString(HttpHeaderNames.COOKIE);

            System.err.println("CookieHandler.cookieString: "+cookieString);
            for (String keyValue : cookieString.split("; ")){
//                System.err.println("keyValue: "+keyValue);
                if (keyValue.contains(HttpHeaderNames.COOKIE)){
//                    System.err.println("uuid: "+keyValue.split("=")[1]);
                    uuids.add(keyValue.split("=")[1]);
                }
            }
            System.err.println("uuids: "+uuids.toString());

            return Optional.of(uuids);
        }
        return Optional.empty();
    }


    /**
     * A utility method that can be used by anyone.
     *
     * @param httpRequest
     * @param cookieName
     * @param cookieValue
     */
    public static void insertCookieToRequest(HttpRequest httpRequest, String cookieName, String cookieValue) {
        httpRequest.headers().set(HttpHeaderNames.COOKIE, ClientCookieEncoder.STRICT.encode(cookieName, cookieValue));
    }

    public void removeCookie(String uuid){
        cookieStorage.removeCookie(uuid);
    }
}
