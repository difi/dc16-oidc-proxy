package no.difi.idporten.oidc.proxy.model;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;

import java.net.SocketAddress;

public interface SecurityConfig {
    public IdentityProvider getIdp(String path);
    public SocketAddress getBackend();
}
