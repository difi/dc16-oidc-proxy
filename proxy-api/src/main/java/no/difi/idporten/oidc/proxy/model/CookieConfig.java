package no.difi.idporten.oidc.proxy.model;

import no.difi.idporten.oidc.proxy.api.CookieStorage;

public interface CookieConfig {

    String getName();

    CookieStorage getCookieStorage();
}
