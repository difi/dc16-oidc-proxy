package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.DefaultProxyCookie;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryCookieStorage {
    /*
    private Map<String, DefaultProxyCookie> cookies = new HashMap<>();
    private static final int MINUTE = 60 * 1000;
    private int initialValidPeriod = 30;
    private int expandSessionPeriod = 30;
    private int maxValidPeriod = 120;


    @Override
    public Optional<ProxyCookie> findCookie(String uuid, String host) {
        return Optional.ofNullable(cookies.get(indexValue(uuid, host)));
    }

    @Override
    public void extendCookieExpiry(DefaultProxyCookie cookie) {
        // 'expandedExpiry' is the cookie's current 'expiry' Date plus the amount of minutes in 'expandSessionPeriod'
        Date expandedExpiry = new Date(cookie.getExpiry().getTime() + expandSessionPeriod * MINUTE);
        // If expandedExpiry.compareTo(maxExpiry) equals -1, 'expandedExpiry' Date is before 'maxExpiry' Date
        // If expandedExpiry.compareTo(maxExpiry) equals 0, 'expandedExpiry' Date equals 'maxExpiry' Date
        if (expandedExpiry.compareTo(cookie.getMaxExpiry()) < 1) cookie.setExpiry(expandedExpiry);
            // If 'expandedExpiry' is after the cookie's 'maxExpiry', 'expiry' is set to 'maxExpiry'
        else cookie.setExpiry(cookie.getMaxExpiry());
    }

    @Override
    public void removeExpiredCookies() {
        // Mutates the 'cookies' list by removing all expired Cookie objects
        cookies.values().removeIf(ProxyCookie::isValid); // TODO
    }

    @Override
    public String toString() {
        return cookies.values().stream()
                .map(cookie -> String.format("%-65s %30s %n", cookie.toString(), cookie.getExpiry()))
                .collect(Collectors.joining(", "));
    }

    private String indexValue(String uuid, String host) {
        return String.format("%s@%s", uuid, host);
    }

    @Override
    public Optional<ProxyCookie> saveOrUpdateCookie(ProxyCookie cookie) {
        return null;
    }

    @Override
    public ProxyCookie generateCookieAsObject(String host, String path, String name, HashMap<String, String> userData) {
        return null;
    }
    */
}
