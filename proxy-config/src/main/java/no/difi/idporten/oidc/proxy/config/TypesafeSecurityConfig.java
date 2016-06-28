package no.difi.idporten.oidc.proxy.config;

import com.sun.istack.internal.NotNull;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import no.difi.idporten.oidc.proxy.model.IdpConfig;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

import java.net.SocketAddress;

public class TypesafeSecurityConfig implements SecurityConfig {

    private IdpConfig idpConfig;
    private HostConfig hostConfig;

    public TypesafeSecurityConfig(@NotNull IdpConfig idpConfig, @NotNull HostConfig hostConfig) {
        System.out.println(String.format("Initiating security config with idpConfig:\n%s\nand hostConfig:\n%s", idpConfig, hostConfig));
        if (idpConfig == null) throw new IllegalArgumentException("Cannot initiate SecurityConfig with idpConfig as null");
        if (hostConfig == null) throw new IllegalArgumentException("Cannot initiate SecurityConfig with hostConfig as null");
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
