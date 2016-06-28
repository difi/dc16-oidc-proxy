package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.sun.istack.internal.NotNull;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import no.difi.idporten.oidc.proxy.model.IdpConfig;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

import java.util.Optional;

public class DefaultSecurityConfigProvider implements SecurityConfigProvider {

    private HostConfigProvider hostConfigProvider;
    private IdpConfigProvider idpConfigProvider;

    @Inject
    public DefaultSecurityConfigProvider(@NotNull HostConfigProvider hostConfigProvider, @NotNull IdpConfigProvider idpConfigProvider) {
        this.hostConfigProvider = hostConfigProvider;
        this.idpConfigProvider = idpConfigProvider;
    }


    @Override
    public Optional<SecurityConfig> getConfig(String hostname, String path) {
            HostConfig hostConfig = hostConfigProvider.getByHostname(hostname);
            if (hostConfig == null)
                return Optional.empty();
            // or else should maybe give default config?
            IdpConfig idpConfig = idpConfigProvider.getByIdentifier("idporten");
            return Optional.of(new TypesafeSecurityConfig(idpConfig, hostConfig));
    }
}
