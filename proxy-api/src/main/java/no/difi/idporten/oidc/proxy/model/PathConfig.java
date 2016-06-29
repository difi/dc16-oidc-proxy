package no.difi.idporten.oidc.proxy.model;


public interface PathConfig {

    String getPath();

    String getIdp();

    String getSecurity();

    String getRedirect_uri();

    String getScope();
}
