package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.DefaultProxyCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.plugin2.message.Message;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class DummyCookieStorage implements CookieStorage {

    private static Logger logger = LoggerFactory.getLogger(DummyCookieStorage.class);


    private static final int MINUTE = 60 * 1000;
    private int initialValidPeriod = 30;
    private int expandSessionPeriod = 30;
    private int maxValidPeriod = 120;

    private static DummyCookieStorage ourInstance = new DummyCookieStorage();

    public static DummyCookieStorage getInstance() {
        return ourInstance;
    }

    private static HashMap<String, String> defaultUserData; // just used for testing purposes

    private Map<String, DefaultProxyCookie> storedCookies;

    /**
     * Generates a MD5 hash based on a random new UUID and the parameters
     *
     * @param host
     * @param cookieName
     * @return
     */
    private static String generateDatabaseKey(String cookieName, String host, String path) {
        String uuid = UUID.randomUUID().toString();
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.reset();
            md.update(uuid.getBytes());
            md.update(cookieName.getBytes());
            md.update(host.getBytes());
            md.update(path.getBytes());
            byte[] array = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException exc) {
            exc.printStackTrace();
        }
        return null;
    }

    private DummyCookieStorage() {
        defaultUserData = new HashMap<String, String>();
        defaultUserData.put("pid", "08023549930");
        storedCookies = new HashMap<String, DefaultProxyCookie>();
    }


    @Override
    public ProxyCookie generateCookieAsObject(String name, String host, String path, HashMap<String, String> userData) {
        logger.debug("Generating cookie object {}@{}{}", name, host, path);
        Date dateNow = new Date();
        String newDatabaseKey = generateDatabaseKey(name, host, path);
        DefaultProxyCookie newCookieObject = new DefaultProxyCookie(newDatabaseKey, name, host, path, new Date(dateNow.getTime() + 60 * MINUTE), new Date(dateNow.getTime() + 60 * 12 * MINUTE), userData);
        storedCookies.put(newDatabaseKey, newCookieObject);
        return newCookieObject;
    }

    private void extendCookieExpiry(DefaultProxyCookie cookie) {
        Date dateNow = new Date();
        Date newExpiry = new Date(dateNow.getTime() + 60 * MINUTE);
        if (newExpiry.after(cookie.getMaxExpiry())) {
            cookie.setExpiry(cookie.getMaxExpiry());
        } else {
            cookie.setExpiry(newExpiry);
        }
    }

    @Override
    public Optional<ProxyCookie> findCookie(String uuid, String host, String path) {
        Date dateNow = new Date();
        ProxyCookie result = storedCookies.get(uuid);
        if (uuid.equals("uuidForValidCookie") && host.equals("localhost:8080")) { // only used for testing
            return Optional.of(new DefaultProxyCookie(uuid, "PROXYCOOKIE", host, path, new Date(dateNow.getTime() + 60 * 24 * MINUTE), new Date(dateNow.getTime() + 60 * 24 * MINUTE), defaultUserData));
        } else if (result != null && result.getHost().equals(host) && result.getPath().equals(path)) {
            extendCookieExpiry(storedCookies.get(uuid));
            return Optional.of(storedCookies.get(uuid));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void removeExpiredCookies() {
        Date dateNow = new Date();
        storedCookies = storedCookies.entrySet().stream()
                .filter(entry -> entry.getValue().getExpiry().before(dateNow))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
