package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.UserData;

public interface IdentityProvider {

    String generateURI() throws IdentityProviderException;

    UserData getToken(String url) throws IdentityProviderException;
}
