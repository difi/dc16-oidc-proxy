package no.difi.idporten.oidc.proxy.model;


public interface PathConfig {


    String getPath();

    String getIdentityProvider();

    String getRedirectUri();

    String getScope();

    int getSecurity();


}
