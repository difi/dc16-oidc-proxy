package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.UserData;

public interface IdentityProvider {

    String generateURI();

    UserData getToken(String url) throws Exception;
}
