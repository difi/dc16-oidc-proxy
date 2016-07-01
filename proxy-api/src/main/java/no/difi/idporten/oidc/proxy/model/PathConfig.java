package no.difi.idporten.oidc.proxy.model;


public interface PathConfig {


    String getPath();

    String getIdentityProvider();

    String getSecurity();

    String getRedirect_uri();

    String getScope();
}
