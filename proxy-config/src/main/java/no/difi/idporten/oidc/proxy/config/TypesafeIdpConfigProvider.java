package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.model.IdpConfig;

public class TypesafeIdpConfigProvider implements IdpConfigProvider {

    @Inject
    public TypesafeIdpConfigProvider(Config config) {
        // TODO Initiate cache.
    }

    @Override
    public IdpConfig getByIdentifier(String identifier) {
        // TODO Implement this.
        return null;
    }
}
