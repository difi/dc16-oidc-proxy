package no.difi.idporten.oidc.proxy.model;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;

import java.net.SocketAddress;
import java.util.List;
import java.util.Optional;

public interface SecurityConfig {

    CookieConfig getCookieConfig();

    Optional<IdentityProvider> createIdentityProvider();

    SocketAddress getBackend();

    String getHostname();

    String getPath();

    String getSecurity();

    String getRedirectUri();

    String getScope();

    String getIdp();

    String getIdpClass();

    String getParameter(String key);

    String getClientId();

    String getPassword();

    List<String> getUserDataNames();

    boolean isSecured();
}
