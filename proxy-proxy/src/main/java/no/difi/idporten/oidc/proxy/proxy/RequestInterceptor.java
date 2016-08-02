package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpRequest;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Utility class for manipulating HttpRequest
 * We probably need a different instance of this class for every SecurityConfig in order to know what information
 * should be in the header.
 */
public class RequestInterceptor {

    private static Logger logger = LoggerFactory.getLogger(RequestInterceptor.class);

    public static final String HEADERNAME = "X-DifiProxy-";

    /**
     * @param httpRequest:
     * @param userData:
     */
    public static void insertUserDataToHeader(HttpRequest httpRequest, Map<String, String> userData, SecurityConfig securityConfig) {
        userData.entrySet()
                .stream()
                .filter(entry -> securityConfig.getUserDataNames().contains(entry.getKey()))
                .forEach(userDataEntry -> {
            httpRequest.headers().add(HEADERNAME + userDataEntry.getKey(), userDataEntry.getValue());
        });
        logger.debug("Inserted header to request:\n{}", httpRequest);
    }

}
