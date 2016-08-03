package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.*;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
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

    private final String idp;

    private List<Map.Entry<String, String>> preferredIdps;


    /**
     * Instantiates a new CookieHandler based on some parameters from a HTTP request much like a SecurityConfig
     *
     * @param cookieConfig:
     * @param host:
     * @param preferredIdps: first idp on the list is the path you're requesting access, or first on preferred list if not found
     */
    public CookieHandler(CookieConfig cookieConfig, String host, List<Map.Entry<String, String>> preferredIdps) {
        this.cookieStorage = cookieConfig.getCookieStorage();
        this.cookieName = cookieConfig.getName();
        this.host = host;
        this.idp = preferredIdps.get(0).getKey();
        this.preferredIdps = preferredIdps;
    }

    public ProxyCookie generateCookie(Map<String, String> userData, int security, int touchPeriod, int maxExpiry) {
        logger.debug("Generating new cookie for idp ({}) on host ({}) with security {}", idp, host, security);
        return cookieStorage.generateCookieInDb(cookieName, host, idp, security, touchPeriod, maxExpiry, userData);
    }

    public ProxyCookie generateIdpCookie(String uuid, Map<String, String> userData, int security, int touchPeriod, int maxExpiry) {
        logger.debug("Generating IdpCookie with uuid ({}) for idp ({}) on host ({}) with security {}", uuid, idp, host, security);
        return cookieStorage.generateCookieInDb(uuid, cookieName, host, idp, security, touchPeriod, maxExpiry, userData);
    }

    /**
     * Convenient function for getting a valid proxy cookie or an empty optional which eases the flow of InboundHandler.
     *
     * @param httpRequest:
     * @return
     */
    public Optional<ProxyCookie> getValidProxyCookie(HttpRequest httpRequest, String salt, String userAgent) {
        logger.debug("Looking for valid cookie with name {}", cookieName);

        Optional<List<String>> cookieOptional = getCookiesFromRequest(httpRequest, cookieName);
        try {
            if (cookieOptional.isPresent()) {
                Optional<ProxyCookie> pc;
                for (int i = 0; i < cookieOptional.get().size(); i++) {

                    String uuid = cookieOptional.get().get(i).substring(64);
                    logger.debug("Searching database for cookie (UUID: {})", uuid);
                    pc = cookieStorage.findCookie(uuid, host, preferredIdps);
                    if (pc.isPresent() && isCorrectHash(cookieOptional.get().get(i), salt, userAgent)) {
                        logger.info("Valid cookie was found ({}) for idp ({})", pc.get(), pc.get().getIdp());
                        return pc;
                    } else {
                        logger.debug("Cookie was not found or not valid (UUID: {})", uuid);
                    }
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            logger.error("Cookie was found but is not on valid format - an error occurred while trying to get substring(64) of cookie hash");
            logger.error("Cookies found: " + cookieOptional);
            logger.error("Length of cookies: " + Arrays.toString(cookieOptional.get().stream().mapToInt(String::length).toArray()));
        }
        logger.info("Http request does not contain valid cookie {}", cookieName);
        return Optional.empty();
    }

    /**
     * Inserts a cookie with a cookieName fetched from the configuration, and a uuid from generating
     * the cookie, into the response's header. The response is sent to the client and the cookie is
     * stored int he clients browser.
     *
     * @param httpResponse: The response the cookie is inserted into.
     * @param cookieName:   The name of the cookie.
     * @param value:        The value of the cookie.
     * @param salt:         The salt used to hash the cookie.
     * @param userAgent:    The useragent from the request header.
     */

    public static void insertCookieToResponse(HttpResponse httpResponse, String cookieName, String value, String salt, String userAgent) {
        String cookieValue = encodeValue(value, salt, userAgent) + value;
        //logger.info("Inserting cookie in response with value: {}", cookieName);
        //httpResponse.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookieName, cookieValue));
        Cookie cookieToInsert = new DefaultCookie(cookieName, cookieValue);
        cookieToInsert.setPath("/");
        httpResponse.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookieToInsert));
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
        logger.debug("CookieHandler.getCookiesFromRequest() - host cookie");

        String cookieString = httpRequest.headers().getAsString(HttpHeaderNames.COOKIE);
        if (httpRequest.headers().contains(HttpHeaderNames.COOKIE) && cookieString.contains(cookieName)) {

            List<String> cookieValues = new ArrayList<>();

            for (String keyValue : cookieString.split("; ")) {
                if (keyValue.contains(cookieName)) {
                    String cookie = keyValue.split("=")[1];
                    if (!cookieValues.contains(cookie)) {
                        cookieValues.add(cookie);
                    } else {
                        logger.warn("Request contains two or more equal cookies ({})", cookie);
                    }
                }
            }
            logger.info("Found cookie(s) in browser: {}", cookieValues.toString());

            return Optional.of(cookieValues);
        }
        logger.debug("Found no cookie with name {} in request", cookieName);
        return Optional.empty();
    }


    /**
     * Encodes the value of the cookie given the value, salt and useragent of the requesting client.
     * The salt is collected from the configuration file
     *
     * @param value:
     * @param salt:
     * @param userAgent:
     * @return Returns the encoded value
     */
    public static String encodeValue(String value, String salt, String userAgent) {
        String stringToBeHashed = value + userAgent;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(salt.getBytes());

            byte[] bytes = messageDigest.digest(stringToBeHashed.getBytes());

            StringBuilder stringBuilder = new StringBuilder();

            for (byte aByte : bytes) {
                stringBuilder.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));

            }
            logger.debug("Encoded value ({}) to hash ({})", stringToBeHashed, stringBuilder.toString());
            return stringBuilder.toString();

        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
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


    /**
     * Removes the cookie with the given uuid from the database.
     *
     * @param uuid:
     */
    public void removeCookie(String uuid) {
        cookieStorage.removeCookie(uuid);
    }


    /**
     * Deletes a ProxyCookie from the browser of the requesting client.
     *
     * @param securityConfig:
     * @param proxyCookie:
     * @param httpRequest:
     * @param httpResponse:
     */
    public static void deleteProxyCookieFromBrowser(SecurityConfig securityConfig, ProxyCookie proxyCookie, HttpRequest httpRequest, HttpResponse httpResponse) {
        String cookieName = proxyCookie.getName();
        String value = proxyCookie.getUuid();
        String cookieValue = encodeValue(value, securityConfig.getSalt(), httpRequest.headers().getAsString(HttpHeaderNames.USER_AGENT)) + value;

        Cookie cookie = new DefaultCookie(cookieName, cookieValue);
        cookie.setMaxAge(0);

        httpResponse.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }

    public static void deleteCookieFromBrowser(String cookieName, HttpResponse httpResponse) {
        Cookie nettyCookie = new DefaultCookie(cookieName, "");
        nettyCookie.setMaxAge(0);
        httpResponse.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(nettyCookie));
    }

}
