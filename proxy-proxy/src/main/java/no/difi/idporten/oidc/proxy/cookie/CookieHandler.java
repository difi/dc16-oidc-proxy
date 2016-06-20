package no.difi.idporten.oidc.proxy.cookie;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.model.Cookie;

import java.util.*;
import java.util.stream.Collectors;

public class CookieHandler implements CookieStorage {

    List<Cookie> cookies = new ArrayList<>();
    private static final int MINUTE = 60*1000;
    private int initialValidPeriod = 30;
    private int expandSessionPeriod = 30;
    private int maxValidPeriod = 120;

    public CookieHandler() {
        System.out.println("CookieHandler instantiated with an empty cookie list");
    }

    @Override
    public String generateCookie(String host){
        Date created = new Date();
        Date lastUpdated = new Date();
        Date expiry = new Date(new Date().getTime() + initialValidPeriod * MINUTE);
        Date maxExpiry = new Date(new Date().getTime() + maxValidPeriod * MINUTE);
        UUID uuid = UUID.randomUUID(); // Generates a 128 bit, type 4 (pseudo randomly generated) UUID
        cookies.add(new Cookie(uuid.toString(), host, expiry, maxExpiry, created, lastUpdated));
        return uuid.toString();
    }

    @Override
    public boolean containsCookie(String uuid){
        return cookies.stream().anyMatch(c -> c.getUuid().equals(uuid));
    }

    @Override
    public Cookie findCookie(String uuid){
        for (Cookie cookie : cookies){
            if (cookie.getUuid().equals(uuid)) return cookie;
        } return null;
    }

    @Override
    public boolean hasExpired(String uuid){
        Cookie cookie = findCookie(uuid);
        if (cookie == null) return true;
        return cookie.hasExpired();
    }

    @Override
    public void extendCookieExpiry(String uuid){
        // The cookie's 'lastUpdated' variable is automatically updated in the Cookie object
        Cookie cookie = findCookie(uuid);
        if (cookie != null){
            // 'expandedExpiry' is the cookie's current 'expiry' Date plus the amount of minutes in 'expandSessionPeriod'
            Date expandedExpiry = new Date(cookie.getExpiry().getTime() + expandSessionPeriod * MINUTE);
            // If expandedExpiry.compareTo(maxExpiry) equals -1, 'expandedExpiry' Date is before 'maxExpiry' Date
            // If expandedExpiry.compareTo(maxExpiry) equals 0, 'expandedExpiry' Date equals 'maxExpiry' Date
            if (expandedExpiry.compareTo(cookie.getMaxExpiry()) < 1) cookie.setExpiry(expandedExpiry);
            // If 'expandedExpiry' is after the cookie's 'maxExpiry', 'expiry' is set to 'maxExpiry'
            else cookie.setExpiry(cookie.getMaxExpiry());
        } // Handle cookieNotFoundException?
    }

    @Override
    public List<Cookie> getValidCookies(){
        // Does not mutate existing 'cookies' list
        return cookies.stream().filter(c -> !c.hasExpired()).collect(Collectors.toCollection(ArrayList::new));
    }

    public void removeExpiredCookies(){
        // Mutates the 'cookies' list by removing all expired Cookie objects
        cookies.removeIf(c -> c.hasExpired());
    }

    @Override
    public boolean hasHost(String uuid, String host){
        Cookie cookie = findCookie(uuid);
        if (cookie != null)
            if (cookie.getHost().equals(host)) return true;
        return false; // Besides throwing exception, is there a better way to handle cookie == null?
    }

    public String toString(){
        String print = "";
        for (Cookie cookie : cookies){
            print += String.format( "%-65s %30s %n", cookie.toString(), cookie.getExpiry() );
        } return print;
    }

    public String toString(List<Cookie> c){
        String print = "";
        for (Cookie cookie : c){
            print += String.format( "%-65s %30s %n", cookie.toString(), cookie.getExpiry() );
        } return print;
    }








}
