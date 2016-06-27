package no.difi.idporten.oidc.proxy.model;

import java.util.List;

public interface IdpConfig {

    String getIdentifier();

    String getIdpClass();

    String getPassword();

    String getScope();

    String getRedriect_Uri();

    String getClient_Id();

    List<String> getParameters();

}
