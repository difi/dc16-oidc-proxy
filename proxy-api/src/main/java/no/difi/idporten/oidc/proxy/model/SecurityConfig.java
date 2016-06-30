package no.difi.idporten.oidc.proxy.model;

import java.util.Map;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;

import java.net.SocketAddress;
import java.util.Optional;

public interface SecurityConfig {

    CookieConfig getCookieConfig();

    IdentityProvider createIdentityProvider();

    SocketAddress getBackend();

    String getHostname();

    String getPath();

    String getSecurity();

    String getRedirect_uri();

    String getScope();

    String getIdp();

    String getIdpClass();

    String getParameter(String key);

    String getClient_id();

    String getPassword();
}
