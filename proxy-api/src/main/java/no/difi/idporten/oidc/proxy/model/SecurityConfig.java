package no.difi.idporten.oidc.proxy.model;

import com.nimbusds.jose.jwk.JWKSet;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SecurityConfig {

    String getHostname();

    String getPath();

    String getRedirectUri();

    String getLogoutRedirectUri();

    String getLogoutPostUri();

    String getScope();

    String getIdp();

    String getIdpClass();

    String getParameter(String key);

    String getClientId();

    String getPassword();

    String getSalt();

    String getApiUri();

    String getLoginUri();

    String getIssuer();

    List<String> getUserDataNames();

    List<String> getUnsecuredPaths();

    List<String> getPreferredIdps();

    List<Map.Entry<String, String>> getPreferredIdpData();

    JWKSet getJSONWebKeys();

    SocketAddress getBackend();

    Optional<IdentityProvider> createIdentityProvider();

    CookieConfig getCookieConfig();

    int getSecurity();

    boolean isLogoutPath();

    boolean isSecured();

    /**
     * Checks if the path is explicitly completely unsecured and should not receive the Difi headers.
     *
     * @param path
     * @return
     */
    boolean isTotallyUnsecured(String path);
}
