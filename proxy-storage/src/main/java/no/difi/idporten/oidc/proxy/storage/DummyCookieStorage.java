package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.DefaultProxyCookie;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class DummyCookieStorage implements CookieStorage {

    private static final int MINUTE = 60 * 1000;

    private static DummyCookieStorage ourInstance = new DummyCookieStorage();

    public static DummyCookieStorage getInstance() {
        return ourInstance;
    }

    private static HashMap<String, String> defaultUserData;

    private DummyCookieStorage() {
        defaultUserData = new HashMap<String, String>();
        defaultUserData.put("pid", "08023549930");
    }

    @Override
    public String generateCookie(String host, HashMap<String, String> userData) {
        return null;
    }

    @Override
    public ProxyCookie generateCookieAsObject(String host, HashMap<String, String> userData) {
        Date dateNow = new Date();
        return new DefaultProxyCookie(UUID.randomUUID().toString(), host, new Date(dateNow.getTime() + 60 * MINUTE), new Date(dateNow.getTime() + 60 * 12 * MINUTE), userData);
    }

    @Override
    public void extendCookieExpiry(DefaultProxyCookie cookie) {

    }

    @Override
    public Optional<ProxyCookie> saveOrUpdateCookie(ProxyCookie cookie) {
        return Optional.empty();
    }

    @Override
    public Optional<ProxyCookie> findCookie(String uuid, String host) {
        Date dateNow = new Date();
        if (uuid.equals("uuidForValidCookie") && host.equals("localhost:8080")) {
            return Optional.of(new DefaultProxyCookie(uuid, host, new Date(dateNow.getTime() + 60 * 24 * MINUTE), new Date(dateNow.getTime() + 60 * 24 * MINUTE), defaultUserData));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void removeExpiredCookies() {

    }
}
