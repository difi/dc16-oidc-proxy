package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.Cookie;
import java.util.List;

public interface CookieStorage {
    long HOUR = 3600*1000;
    long MINUTE = 60*1000;

    String createCookie(String host);
    boolean hasCookie(String uuid);
    boolean hasExpired(String uuid);
    void touchCookie(String uuid);
    List<Cookie> getValidCookies();
    Cookie findCookie(String uuid);
}
