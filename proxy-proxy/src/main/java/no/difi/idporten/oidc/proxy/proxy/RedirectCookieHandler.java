package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class RedirectCookieHandler {

    private static Logger logger = LoggerFactory.getLogger(RedirectCookieHandler.class);

    private final static String redirectCookieName = "redirectCookie";

    private final String host;

    private final String path;

    private static HashMap<String, String> hashToPathMap = new HashMap<>();

    /**
     * Instantiates a new CookieHandler based on some parameters from a HTTP request much like a SecurityConfig
     *
     * @param cookieConfig:
     * @param host:
     * @param path:
     */
    public RedirectCookieHandler(CookieConfig cookieConfig, String host, String path) {
        this.host = host;
        this.path = path;
    }

    public Cookie insertCookieToResponse(HttpResponse response) {
        String value = CookieHandler.encodeValue(path, "INSERTSALTHERE", new ArrayList<>()) + path;
        Cookie cookieToInsert = new DefaultCookie(redirectCookieName, value);
        CookieHandler.insertCookieToResponse(response, redirectCookieName, path, "INSERTSALTHERE", new ArrayList<>());
        System.err.println(value);
        hashToPathMap.put(value, path);
        return cookieToInsert;
    }


    public static Optional<String> findRedirectCookiePath(HttpRequest httpRequest) {
        Optional<Cookie> nettyCookieOptional = CookieHandler.getCookieFromRequest(httpRequest, redirectCookieName);
        if (nettyCookieOptional.isPresent()) {
            String redirectCookieValue = nettyCookieOptional.get().value();
            String hash = redirectCookieValue.substring(0, 64);
            String uuid = redirectCookieValue.substring(64);
            if (hashToPathMap.containsKey(redirectCookieValue) && CookieHandler.isCorrectHash(hash, uuid, "INSERTSALTHERE", new ArrayList<>())) {
                String result = hashToPathMap.get(redirectCookieValue);
                hashToPathMap.remove(redirectCookieValue);
                logger.debug("Found original path for request after redirect: {}", result);
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }
}
