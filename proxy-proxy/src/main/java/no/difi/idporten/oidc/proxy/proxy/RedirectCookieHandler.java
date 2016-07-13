package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;

public class RedirectCookieHandler {

    private static Logger logger = LoggerFactory.getLogger(RedirectCookieHandler.class);

    private final String cookieName;

    private final String host;

    private final String path;

    private static HashMap<String, String> hashToPathMap = new HashMap<>();

    /**
     * Instantiates a new CookieHandler based on some parameters from a HTTP request much like a SecurityConfig
     *
     * @param cookieConfig
     * @param host
     * @param path
     */
    public RedirectCookieHandler(CookieConfig cookieConfig, String host, String path) {
        this.cookieName = cookieConfig.getName();
        this.host = host;
        this.path = path;
    }

    public void insertCookieToResponse(HttpResponse response) {
        String hash = generateCookieHash(path);
        CookieHandler.insertCookieToResponse(response, "redirectCookie", hash);
        hashToPathMap.put(hash, path);
    }

    private static String generateCookieHash(String path) {
        return ("HASHASHASH" + path);
    }

    public static Optional<String> findRedirectCookiePath(HttpRequest request) {
        if (hashToPathMap.containsKey(generateCookieHash(request.uri()))) {
            String hashForRequest = generateCookieHash(request.uri());
            String result = hashToPathMap.get(hashForRequest);
            hashToPathMap.remove(hashForRequest);
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }
}
