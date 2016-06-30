package no.difi.idporten.oidc.proxy.model;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;

import java.util.Map;
import java.util.Optional;

public interface IdpConfig {


    String getIdentifier();

    String getIdpClass();

    String getPassword();

    String getScope();

    String getRedirect_uri();

    String getClient_Id();

    Optional<String> getParameter(String key);

}
