package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

public class DefaultSecurityConfigProvider implements SecurityConfigProvider {

    private HostConfigProvider hostConfigProvider; //Interface hostConfigProvider
    private IdpConfigProvider idpConfigProvider; //Interface idpConfigProvider

    @Inject
    public DefaultSecurityConfigProvider(HostConfigProvider hostConfigProvider, IdpConfigProvider idpConfigProvider) {
        this.hostConfigProvider = hostConfigProvider; //Setters
        this.idpConfigProvider = idpConfigProvider;   //Setters
    }

    @Override
    public SecurityConfig getConfig(String hostname, String path) {
        SecurityConfig securityConfig = new SecurityConfig();
        return securityConfig.getConfig(hostname, path);
        // TODO Implement this. Make SecurityConfig getConfig()-method return something other then null
    }
}
