package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        logger.debug("CookieHandler.generateCookie()");
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

        Optional<List<String>> cookieOptional = getCookiesFromRequest(httpRequest, cookieName);
        if (cookieOptional.isPresent()) {
            Optional<ProxyCookie> pc;
            for (int i = 0; i < cookieOptional.get().size(); i++){
                String uuid = cookieOptional.get().get(i).substring(64);
                logger.debug("Looking for cookie (UUID: {})"+uuid);
                pc = cookieStorage.findCookie(uuid, host, path);
                if (pc.isPresent() && isCorrectHash(cookieOptional.get().get(i), salt, userAgent)) {
                    logger.info("Valid cookie was found ({})", pc);
                    return pc;
                } else {
                    logger.debug("This cookie was not found valid (UUID: {})", uuid);
                }
            }
        }
        logger.info("Http request does not contain valid cookie {}", cookieName);
        return Optional.empty();
    }

    /**
     * Inserts a cookie with a cookieName fetched from the configuration, and a uuid from generating
     * the cookie, into the response's header. The response is sent to the client and the cookie is
     * stored int he clients browser.
     *
     * @param httpResponse:
     * @param cookieName:
     */
    public static void insertCookieToResponse(HttpResponse httpResponse, String cookieName, String uuid) {
        httpResponse.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookieName, uuid));
    }

    public static void insertCookieToResponse(HttpResponse httpResponse, String cookieName, String value, String salt, String userAgent) {
        String cookieValue = encodeValue(value, salt, userAgent) + value;
        logger.info("Inserting cookie in response with value: {}", cookieName);
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
        logger.debug("CookieHandler.getCookieFromRequest() - redirect cookie");
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
        logger.debug("CookieHandler.getCookieFromRequest() - host cookie");
        if (httpRequest.headers().contains(HttpHeaderNames.COOKIE)) {

            List<String> cookieValues = new ArrayList<>();

            String cookieString = httpRequest.headers().getAsString(HttpHeaderNames.COOKIE);

            for (String keyValue : cookieString.split("; ")) {
                if (keyValue.contains(HttpHeaderNames.COOKIE)) {
                    cookieValues.add(keyValue.split("=")[1]);
                }
            }
            logger.info("Found cookies in browser: {}", cookieValues.toString());

            return Optional.of(cookieValues);
        }
        logger.debug("Found no cookies for in request");
        return Optional.empty();
    }

    public static String encodeValue(String value, String salt, String userAgent) {
        logger.debug("CookieHandler.encodeValue()");
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
     * @param browserValue:
     * @param salt:
     * @return a boolean that tells whether the hash is correct or not.
     */

    public static boolean isCorrectHash(String browserValue, String salt, String userAgent) {
        String hash = browserValue.substring(0, 64);
        String value = browserValue.substring(64);
        //System.out.println(hash + " " + encodeValue(value, salt, userAgent));
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

    public void removeCookie(String uuid){
        cookieStorage.removeCookie(uuid);
    }

}
