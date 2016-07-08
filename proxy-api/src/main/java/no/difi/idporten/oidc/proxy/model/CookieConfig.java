package no.difi.idporten.oidc.proxy.model;

import no.difi.idporten.oidc.proxy.api.CookieStorage;

public interface CookieConfig {
    String getName();

    int getMaxExpiry();

    int getTouch();

    CookieStorage getCookieStorage();
}
