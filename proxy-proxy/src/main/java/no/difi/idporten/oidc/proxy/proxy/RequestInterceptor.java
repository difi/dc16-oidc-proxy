package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpRequest;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for manipulating HttpRequest
 * We probably need a different instance of this class for every SecurityConfig in order to know what information
 * should be in the header.
 */
public class RequestInterceptor {

    private static Logger logger = LoggerFactory.getLogger(RequestInterceptor.class);

    public static final String HEADERNAME = "X-DifiProxy-";

    /**
     *
     * @param httpRequest
     * @param userData
     * @param securityConfig
     */
    public static void insertUserDataToHeader(
            HttpRequest httpRequest, Map<String, String> userData, SecurityConfig securityConfig) {
        List<String> userDataNames = securityConfig.getUserDataNames();

        userDataNames.stream().forEach(userDataName -> {
            httpRequest.headers().add(HEADERNAME + userDataName, encodeUserDataForHeader(userData, userDataName));
        });
        logger.debug("Inserted header to request:\n{}", httpRequest);
    }

    /**
     * Encodes a Map to a string in the standard format of HTTP headers.
     * @param userData
     * @param userDataName
     * @return
     */
    private static String encodeUserDataForHeader(Map<String, String> userData, String userDataName) {
        return userData.entrySet().stream()
                .filter(e -> userDataName.equals(e.getKey()))
                .map(entry -> String.format("%s", entry.getValue()))
                .collect(Collectors.joining(""));
    }


}
