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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class CookieHandler {

    private static Logger logger = LoggerFactory.getLogger(CookieHandler.class);

    private final CookieStorage cookieStorage;

    private final String cookieName;

    private final String host;

    private final String path;

    /**
     * Instantiates a new CookieHandler based on some parameters from a HTTP request much like a SecurityConfig
     *
     * @param cookieConfig:
     * @param host:
     * @param path:
     */
    public CookieHandler(CookieConfig cookieConfig, String host, String path) {
        this.cookieStorage = cookieConfig.getCookieStorage();
        this.cookieName = cookieConfig.getName();
        this.host = host;
        this.path = path;
    }

    public ProxyCookie generateCookie(Map<String, String> userData, int touchPeriod, int maxExpiry) {
        return cookieStorage.generateCookieInDb(cookieName, host, path, touchPeriod, maxExpiry, userData);
    }

    /**
     * Convenient function for getting a valid proxy cookie or an empty optional which eases the flow of InboundHandler.
     *
     * @param httpRequest:
     * @return
     */
    public Optional<ProxyCookie> getValidProxyCookie(HttpRequest httpRequest, String salt, String userAgent) {
        logger.debug("Looking for cookie with name {}", cookieName);

        Optional<Cookie> nettyCookieOptional = getCookieFromRequest(httpRequest);
        if (nettyCookieOptional.isPresent()) {
            System.out.println(nettyCookieOptional.get().value());
            String uuid = nettyCookieOptional.get().value().substring(64);
            logger.debug("HTTP request has the cookie we are looking for", nettyCookieOptional.get());
            Optional<ProxyCookie> proxyCookieOptional = cookieStorage.findCookie(uuid, host, path);
            if (proxyCookieOptional.isPresent() && isCorrectHash(nettyCookieOptional.get(), salt, userAgent)) {
                return proxyCookieOptional;
            } else {
                logger.warn("Could not find valid cookie {}@{}{}", uuid, host, path);
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
     * @param httpResponse:
     * @param cookieName:
     * @param value:
     */
    public static void insertCookieToResponse(HttpResponse httpResponse, String cookieName, String value, String salt, String userAgent) {
        String cookieValue = encodeValue(value, salt, userAgent) + value;
        httpResponse.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookieName, cookieValue));
    }

    /**
     * Looks for a cookie with the name of this CookieHandler's cookieName in a request and returns a Netty cookie
     * object or an empty optional.
     *
     * @param httpRequest:
     * @return
     */
    private Optional<Cookie> getCookieFromRequest(HttpRequest httpRequest) {
        return getCookieFromRequest(httpRequest, cookieName);
    }

    /**
     * Looks for a cookie with the name of this CookieHandler's cookieName in a request and returns a Netty cookie
     * object or an empty optional.
     *
     * @param httpRequest:
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

    public static String encodeValue(String value, String salt, String userAgent) {
        System.out.println(value + " " + salt + " " + userAgent);
        String stringToBeHashed = value + userAgent;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(salt.getBytes());

            byte[] bytes = messageDigest.digest(stringToBeHashed.getBytes());

            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < bytes.length; i++) {
                stringBuilder.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));

            }
            return stringBuilder.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Checks if the hash in the cookie matches the uuid made with the parameters given in the cookie.
     *
     * @param nettyCookieOptional:
     * @param salt:
     * @return a boolean that tells whether the hash is correct or not.
     */

    public static boolean isCorrectHash(Cookie nettyCookieOptional, String salt, String userAgent) {
        String hash = nettyCookieOptional.value().substring(0, 64);
        String value = nettyCookieOptional.value().substring(64);
        System.out.println(hash + " " + encodeValue(value, salt, userAgent));
        return (hash.equals(encodeValue(value, salt, userAgent)));
    }


    /**
     * A utility method that can be used by anyone.
     *
     * @param httpRequest:
     * @param cookieName:
     * @param cookieValue:
     */
    public static void insertCookieToRequest(HttpRequest httpRequest, String cookieName, String cookieValue) {
        httpRequest.headers().set(HttpHeaderNames.COOKIE, ClientCookieEncoder.STRICT.encode(cookieName, cookieValue));

    }

}