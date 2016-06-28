package no.difi.idporten.oidc.proxy.model;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;

import java.util.List;
import java.util.Map;

public interface IdpConfig {

    public IdentityProvider getIdp();

    String getIdentifier();

    String getIdpClass();

    String getPassword();

    String getScope();

    String getRedirect_Uri();

    String getClient_Id();

    Map<String ,String> getParameters();

    String getValueFromParametersWithKey(String key);

}
