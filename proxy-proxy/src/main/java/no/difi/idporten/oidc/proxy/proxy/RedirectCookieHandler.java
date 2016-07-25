package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RedirectCookieHandler {

    private static Logger logger = LoggerFactory.getLogger(RedirectCookieHandler.class);

    private final static String redirectCookieName = "redirectCookie";

    private final String host;

    private final String path;

    private static Map<String, String> hashToPathMap = new HashMap<>();

    /**
     * Instantiates a new CookieHandler based on some parameters from a HTTP request much like a SecurityConfig
     *
     * @param cookieConfig:
     * @param host:
     * @param path:
     */
    public RedirectCookieHandler(CookieConfig cookieConfig, String host, String path) {
        System.err.println("Constructor hashToPathMap: "+hashToPathMap.toString());
        this.host = host;
        this.path = path;
    }

    public Cookie insertCookieToResponse(HttpResponse response, String salt, String userAgent) {
        System.err.println("insertCookieToResponse hashToPathMap: "+hashToPathMap.toString());
        System.out.println("\ninsertCookieToResponse()\nSalt: "+salt+"\nuserAgent: "+userAgent);
        String value = CookieHandler.encodeValue(path, salt, userAgent) + path;
        //String value = CookieHandler.encodeValue(path, salt, userAgent) + userAgent; fikser error i integrationTest av merkelig grunn
        System.out.println("Hashed value [CookieHandler.encodeValue(path, salt, userAgent) + path]: "+value);
        Cookie cookieToInsert = new DefaultCookie(redirectCookieName, value);
        CookieHandler.insertCookieToResponse(response, redirectCookieName, path, salt, userAgent);
        System.err.println(value);
        hashToPathMap.put(value, path);
        //System.out.println("All hashToPathMap: "+hashToPathMap.toString());
        return cookieToInsert;
    }


    public static Optional<String> findRedirectCookiePath(HttpRequest httpRequest, String salt, String userAgent) {
        System.err.println("findRedirectCookiePath hashToPathMap: "+hashToPathMap.toString());
        System.out.println("\nRedirectCookieHandler.findRedirectCookiePath()");
        System.out.println("Salt: "+salt+"\nuserAgent: "+userAgent);

        //System.out.println("All hashToPathMap: "+hashToPathMap.toString());

        Optional<Cookie> nettyCookieOptional = CookieHandler.getCookieFromRequest(httpRequest, redirectCookieName);
        if (nettyCookieOptional.isPresent()) {
//            System.out.println("nettyCookieOptional.isPresent() == true");
//            System.out.println("nettyCookieOptional.get().toString():" + nettyCookieOptional.get().toString());
//            System.out.println("nettyCookieOptional.get().value():" + nettyCookieOptional.get().value());
            String redirectCookieValue = nettyCookieOptional.get().value();
            System.out.println("redirectCookieValue: " + nettyCookieOptional.get().value());
            String uuid = nettyCookieOptional.get().value().substring(64);
//            System.out.println("UUID: "+uuid);
//            System.out.println("encoded hash value: "+ CookieHandler.encodeValue());
//            String path = redirectCookieValue.split("/")[1];
//            System.out.println("path: "+path);
            String path = redirectCookieValue.substring(redirectCookieValue.indexOf('/'));
//            System.out.println("path2: "+path2);
//            System.out.println("Is correct hash: "+CookieHandler.isCorrectHash(nettyCookieOptional.get().toString(), salt, userAgent));
//            System.out.println("Is correct hash: "+CookieHandler.isCorrectHash(path, salt, userAgent));
            System.out.println("CookieHandler.encodeValue(path, salt, userAgent)+path: " + (CookieHandler.encodeValue(path, salt, userAgent)) + path);
            System.out.println("redirectCookieValue: "+redirectCookieValue);
            System.out.println("isEqual as FUUUUCK: "+checkThisEncodedShit(redirectCookieValue, path, salt, userAgent));
//            if (hashToPathMap.containsKey(redirectCookieValue) && CookieHandler.isCorrectHash(nettyCookieOptional.get().value(), salt, userAgent)) {
            if (hashToPathMap.containsKey(redirectCookieValue) && checkThisEncodedShit(redirectCookieValue, path, salt, userAgent) ){//&& CookieHandler.isCorrectHash(path, salt, userAgent)) {
                System.out.println("hashToPathMap contains redirectCookieValue");
                String result = hashToPathMap.get(redirectCookieValue);
                System.out.println("Original path: "+result);
                hashToPathMap.remove(redirectCookieValue);
                logger.debug("Found original path for request after redirect: {}", result);
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }

    private static boolean checkThisEncodedShit(String hash, String path, String salt, String userAgent){
        String encoded = CookieHandler.encodeValue(path, salt, userAgent)+path;
        return hash.equals(encoded);
    }
}
