package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

public class DefaultSecurityConfigProvider implements SecurityConfigProvider {

    private HostConfigProvider hostConfigProvider;
    private IdpConfigProvider idpConfigProvider;

    @Inject
    public DefaultSecurityConfigProvider(HostConfigProvider hostConfigProvider, IdpConfigProvider idpConfigProvider) {
        this.hostConfigProvider = hostConfigProvider;
        this.idpConfigProvider = idpConfigProvider;
    }

    @Override
    public SecurityConfig getConfig(String hostname, String path) {
        if (hostConfigProvider.getByHostname(hostname) != null) {
            return new DefaultSecurityConfig(hostname, path, hostConfigProvider, idpConfigProvider);
        }
        return null;
    }

}
