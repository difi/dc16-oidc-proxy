package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.IdpConfig;

public interface IdpConfigProvider {

    IdpConfig getByIdentifier(String identifier);
}
