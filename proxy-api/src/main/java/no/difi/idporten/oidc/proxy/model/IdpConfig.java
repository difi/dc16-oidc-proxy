package no.difi.idporten.oidc.proxy.model;

import com.nimbusds.jose.jwk.JWKSet;

import java.util.List;
import java.util.Optional;

public interface IdpConfig {

    JWKSet getJSONWebKeys();

    String getIdentifier();

    String getIdpClass();

    String getPassword();

    String getScope();

    String getRedirectUri();

    String getClientId();

    List<String> getUserDataNames();

    Optional<String> getParameter(String key);

}
