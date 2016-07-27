package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RedirectCookieHandler {

    private static Logger logger = LoggerFactory.getLogger(RedirectCookieHandler.class);

    private final static String redirectCookieName = "redirectCookie";

    private final String path;

    private static Map<String, String> hashToPathMap = new HashMap<>();

    /**
     * Instantiates a new CookieHandler based on some parameters from a HTTP request much like a SecurityConfig
     *
     * @param path:
     */
    public RedirectCookieHandler(String path) {
        this.path = path;
    }

    public Cookie insertCookieToResponse(HttpResponse response, String salt, String userAgent) {
        String value = CookieHandler.encodeValue(path, salt, userAgent) + path;
        logger.debug("Inserting redirect cookie to response ({})", value);
        Cookie cookieToInsert = new DefaultCookie(redirectCookieName, value);
        cookieToInsert.setPath("/");
        response.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookieToInsert));
        hashToPathMap.put(value, path);
        System.out.println("FANDEN:"+hashToPathMap);
        return cookieToInsert;
    }


    public static Optional<String> findRedirectCookiePath(HttpRequest httpRequest, String salt, String userAgent) {
        Optional<Cookie> nettyCookieOptional = CookieHandler.getCookieFromRequest(httpRequest, redirectCookieName);
        if (nettyCookieOptional.isPresent()) {
            String redirectCookieValue = nettyCookieOptional.get().value();
            logger.debug("Found redirect cookie: {}", redirectCookieValue);
            System.out.println("FUCKTARDS"+hashToPathMap.entrySet());
            String path = redirectCookieValue.substring(redirectCookieValue.indexOf('/'));
            if (hashToPathMap.containsKey(redirectCookieValue) && checkEncodedRedirectCookie(redirectCookieValue, path, salt, userAgent)) {
                String result = hashToPathMap.get(redirectCookieValue);
                hashToPathMap.remove(redirectCookieValue);
                logger.debug("Found original path for request after redirect: {}", result);
                return Optional.of(result);
            } else {
                logger.error("Cookie is not valid and as such didn't find original path after redirect ({})", redirectCookieValue);
            }
        }
        return Optional.empty();
    }

    private static boolean checkEncodedRedirectCookie(String hash, String path, String salt, String userAgent) {
        String encoded = CookieHandler.encodeValue(path, salt, userAgent) + path;
        return hash.equals(encoded);
    }

    public static void deleteRedirectCookieFromBrowser(HttpRequest httpRequest, HttpResponse httpResponse, SecurityConfig securityConfig, String value){
        String cookieValue = CookieHandler.encodeValue(value, securityConfig.getSalt(), httpRequest.headers().get("User-Agent")) + value;
        System.out.println("COOKIEVALUE" + cookieValue);
        Cookie cookie = new DefaultCookie(redirectCookieName, cookieValue);
        cookie.setMaxAge(0);

        httpResponse.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));

    }
}
