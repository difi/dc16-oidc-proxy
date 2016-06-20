package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.model.Cookie;

import java.util.*;
import java.util.stream.Collectors;

public class CookieHandler implements CookieStorage {

    private Map<String, Cookie> cookies = new HashMap<>();
    private static final int MINUTE = 60 * 1000;
    private int initialValidPeriod = 30;
    private int expandSessionPeriod = 30;
    private int maxValidPeriod = 120;

    @Override
    public String generateCookie(String host) {
        Date expiry = new Date(new Date().getTime() + initialValidPeriod * MINUTE);
        Date maxExpiry = new Date(new Date().getTime() + maxValidPeriod * MINUTE);

        Cookie cookie = new Cookie(UUID.randomUUID().toString(), host, expiry, maxExpiry);
        cookies.put(indexValue(cookie.getUuid(), host), cookie);

        return cookie.getUuid();
    }

    @Override
    public Optional<Cookie> findCookie(String uuid, String host) {
        return Optional.ofNullable(cookies.get(indexValue(uuid, host)));
    }

    @Override
    public void extendCookieExpiry(Cookie cookie) {
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
        cookies.values().removeIf(Cookie::isValid); // TODO
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
}
