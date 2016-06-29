package no.difi.idporten.oidc.proxy.config;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import no.difi.idporten.oidc.proxy.model.IdpConfig;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

import java.net.SocketAddress;
import java.util.Map;

public class TypesafeSecurityConfig implements SecurityConfig {

    private IdpConfig idpConfig;
    private HostConfig hostConfig;

    public TypesafeSecurityConfig(IdpConfig idpConfig, HostConfig hostConfig) {
        System.out.println(String.format("Initiating security config with idpConfig:\n%s\nand hostConfig:\n%s", idpConfig, hostConfig));
        if (idpConfig == null) throw new IllegalArgumentException("Cannot initiate SecurityConfig with idpConfig as null");
        if (hostConfig == null) throw new IllegalArgumentException("Cannot initiate SecurityConfig with hostConfig as null");
        this.idpConfig = idpConfig;
        this.hostConfig = hostConfig;
    }

    @Override
    public IdentityProvider getIdp(String path) {
        return idpConfig.getIdp(path);
    }

    @Override
    public SocketAddress getBackend() {
        return this.hostConfig.getBackend();
    }

    @Override
    public String getHostname() {
        return null;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public String getSecurity() {
        return null;
    }

    @Override
    public String getRedirect_uri() {
        return null;
    }

    @Override
    public String getScope() {
        return null;
    }

    @Override
    public String getIdp() {
        return null;
    }

    @Override
    public String getIdpClass() {
        return null;
    }

    @Override
    public String getClient_id() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }
}
