package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.model.HostConfig;

public class TypesafeHostConfigProvider implements HostConfigProvider {

    @Inject
    public TypesafeHostConfigProvider(Config config) {
        // TODO Initiate cache.
    }

    @Override
    public HostConfig getByHostname(String hostname) {
        // TODO Implement this.
        return null;
    }
}
