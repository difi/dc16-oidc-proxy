package no.difi.idporten.oidc.proxy.model;

import com.nimbusds.jose.jwk.JWKSet;

import java.util.List;
import java.util.Optional;

public interface IdpConfig {

    String getIdentifier();

    String getIdpClass();

    String getPassword();

    String getScope();

    String getRedirectUri();

    String getClientId();

    Optional<String> getApiUri();

    Optional<String> getIssuer();

    Optional<String> getLoginUri();

    Optional<String> getParameter(String key);

    Optional<String> getPassAlongData();

    List<String> getUserDataNames();

    Optional<JWKSet> getJSONWebKeys();

    Optional<String> getJwkUri();

}
