package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

import java.util.Optional;

public class DefaultSecurityConfigProvider implements SecurityConfigProvider {

    private HostConfigProvider hostConfigProvider;

    private IdpConfigProvider idpConfigProvider;

    @Inject
    public DefaultSecurityConfigProvider(HostConfigProvider hostConfigProvider, IdpConfigProvider idpConfigProvider) {
        this.hostConfigProvider = hostConfigProvider;
        this.idpConfigProvider = idpConfigProvider;
    }

    @Override
    public Optional<SecurityConfig> getConfig(String hostname, String path) {
        if (hostConfigProvider.getByHostname(hostname) == null) {
            return Optional.empty();
        }
        return Optional.of(new DefaultSecurityConfig(hostname, path, hostConfigProvider, idpConfigProvider));
    }
}
