package no.difi.idporten.oidc.proxy.model;

import java.util.Map;

public interface SecurityConfig {

    String getHostname();

    String getPath();

    String getSecurity();

    String getRedirect_uri();

    String getScope();

    String getIdp();

    String getIdpClass();

    String getClient_id();

    String getPassword();

    Map<String, String> getParameters();


}
