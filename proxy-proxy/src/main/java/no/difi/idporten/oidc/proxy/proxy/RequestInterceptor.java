package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for manipulating HttpRequest
 * We probably need a different instance of this class for every SecurityConfig in order to know what information
 * should be in the header.
 * TODO make instantiable with SecurityConfig or similar
 */
public class RequestInterceptor {

    private static Logger logger = LoggerFactory.getLogger(RequestInterceptor.class);

    public static final String HEADERNAME = "X-DIFICAMP-PROXY-USER-DATA";


    public static void insertUserDataToHeader(HttpRequest httpRequest, Map<String, String> userData) {
        httpRequest.headers().add(HEADERNAME, encodeUserDataForHeader(userData));
        logger.debug("Inserted header to request:\n{}", httpRequest);
    }

    /**
     * Encodes a Map to a string in the standard format of HTTP headers.
     * @param userData
     * @return
     */
    private static String encodeUserDataForHeader(Map<String, String> userData) {
        return userData.entrySet().stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("; "));
    }


}
