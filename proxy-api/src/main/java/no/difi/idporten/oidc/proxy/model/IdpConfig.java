package no.difi.idporten.oidc.proxy.model;

import java.util.List;
import java.util.Optional;

public interface IdpConfig {

    String getPublicSignature();

    String getIdentifier();

    String getIdpClass();

    String getPassword();

    String getScope();

    String getRedirectUri();

    String getClientId();

    List<String> getUserDataNames();

    Optional<String> getParameter(String key);

}
