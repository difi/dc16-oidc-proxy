package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.UserData;

public interface IdentityProvider {

    public String generateURI();

    public UserData getToken(String url) throws Exception;
}
