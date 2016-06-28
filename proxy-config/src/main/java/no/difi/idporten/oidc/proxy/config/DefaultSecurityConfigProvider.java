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
        if (hostConfigProvider.getByHostname(hostname) != null){

        }

        //System.out.println(idpConfigProvider.getByIdentifier(hostConfigProvider.getByHostname(hostname).getPathFor(path).get().getIdp()).toString());



        // TODO Implement this.
        return null;
    }

    public static void main(String[] args) {
        HostConfigProvider hs = new TypesafeHostConfigProvider(ConfigFactory.load());
        IdpConfigProvider is = new TypesafeIdpConfigProvider(ConfigFactory.load());
        new DefaultSecurityConfigProvider(hs, is).getConfig("www.xkcd.com", "/");
    }
}
