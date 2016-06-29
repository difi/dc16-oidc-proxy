package no.difi.idporten.oidc.proxy.model;

import java.util.Map;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import java.net.SocketAddress;

public interface SecurityConfig {

    IdentityProvider getIdp(String path);

    SocketAddress getBackend();

    String getHostname();

    String getPath();

    String getSecurity();

    String getRedirect_uri();

    String getScope();

    String getIdp();

    String getIdpClass();

    String getClient_id();

    String getPassword();

    Map<String, String> getParameters();

}
