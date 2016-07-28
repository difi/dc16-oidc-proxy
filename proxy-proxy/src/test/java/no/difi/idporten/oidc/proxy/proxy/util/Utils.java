package no.difi.idporten.oidc.proxy.proxy.util;

import org.apache.http.Header;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {
    public static String specificPathWithGoogle = "/google/a/specific/path";

    public static String unsecuredPath = "/unsecured";

    public static String contentOfASpecificPath = "content of a specific path";

    public static String contentOfAnUnsecuredPath = "content of an unsecured path";

    public static String mockServerHostName = "www.mockhost.com";

    public static String mockServerAddress = "http://localhost:8081";

    public static String cookieName = "PROXYCOOKIE";

    public static String googleApiPath = "/oauth2/v3/token";

    public static String googleLoginPath = "/o/oauth2/auth";

    public static String idportenApiPath = "/idporten-oidc-provider/token";

    public static String idportenLoginPath = "/idporten-oidc-provider/authorize";

    public static String logoutPath = "/logout";

    public static Map<String, String> getHeadersAsMap(Header[] headers) {
        return Arrays.stream(headers)
                .collect(Collectors.toMap(Header::getName, Header::getValue));
    }
}
