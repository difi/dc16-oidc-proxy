package no.difi.idporten.oidc.proxy.model;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SecurityConfig {

    CookieConfig getCookieConfig();

    Optional<IdentityProvider> createIdentityProvider();

    SocketAddress getBackend();

    String getHostname();

    String getPath();

    int getSecurity();

    List<String> getPreferredIdps();

    List<Map.Entry<String, String>> getPreferredIdpData();

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

    List<String> getUserDataNames();

    List<String> getUnsecuredPaths();


    boolean isSecured();

    /**
     * Checks if the path is explicitly completely unsecured and should not receive the Difi headers.
     *
     * @param path
     * @return
     */
    boolean isTotallyUnsecured(String path);
}
