package no.difi.idporten.oidc.proxy.model;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;

import java.util.Map;

public interface IdpConfig {

    IdentityProvider getIdp(String path);

    String getIdentifier();

    String getIdpClass();

    String getPassword();

    String getScope();

    String getRedirect_uri();

    String getClient_Id();

    Map<String ,String> getParameters();

    String getValueFromParametersWithKey(String key);

}
