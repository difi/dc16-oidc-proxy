package no.difi.idporten.oidc.proxy.config;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import no.difi.idporten.oidc.proxy.model.IdpConfig;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

import java.net.SocketAddress;

public class TypesafeSecurityConfig implements SecurityConfig {

    private IdpConfig idpConfig;
    private HostConfig hostConfig;

    public TypesafeSecurityConfig(IdpConfig idpConfig, HostConfig hostConfig) {
        this.idpConfig = idpConfig;
        this.hostConfig = hostConfig;
    }

    @Override
    public IdentityProvider getIdp(String path) {
        return idpConfig.getIdp();
    }

    @Override
    public SocketAddress getBackend() {
        return this.hostConfig.getBackend();
    }
}
