package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpRequest;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

import java.util.Map;

/**
 * Utility class for manipulating HttpRequest
 * We probably need a different instance of this class for every SecurityConfig in order to know what information
 * should be in the header.
 */
public class RequestInterceptor {

    public static final String HEADERNAME = "X-DifiProxy-";

    public static final String ADDITIONAL_DATA_HEADERNAME = "X-additional-data";

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
        userData.entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith(ADDITIONAL_DATA_HEADERNAME))
                .forEach(entry -> httpRequest.headers().add(entry.getKey(), entry.getValue()));
    }

}
