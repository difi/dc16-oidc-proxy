package no.difi.idporten.oidc.proxy.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class DatabaseCookieStorageTest {

    private static final int MINUTE = 1000 * 60;

    private int touchPeriod = 20;

    private int maxExpiry = 120;

    private int security = 3;

    private String idp = "google";

    private String idp2 = "idporten";

    private HashMap<String, String> userData = new HashMap<>();

    private Injector injector;

    @Test
    public void beforeClass() {
        injector = Guice.createInjector(new StorageModule());
        userData.put("pid", "31120012345");
        userData.put("aud", "abcdefghij");
        userData.put("sub", "1234567890");
        userData.put("name", "Ola Nordmann");
    }

    @Test
    public void createSimpleCookieInStorage() {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        ProxyCookie proxyCookie = cookieStorage.generateCookieInDb("PROXYCOOKIE", "example.com", idp, security, touchPeriod, maxExpiry, null);
        Assert.assertNotNull(proxyCookie);

        Assert.assertTrue(cookieStorage.findCookie(proxyCookie.getUuid(), "example.com", idp).isPresent());
        Assert.assertTrue(cookieStorage.findCookie(proxyCookie.getUuid(), "example.com", idp).isPresent());
        Assert.assertFalse(cookieStorage.findCookie(proxyCookie.getUuid(), "example.com", idp2).isPresent());
        Assert.assertFalse(cookieStorage.findCookie(proxyCookie.getUuid(), "domain.com", idp).isPresent());
    }

    @Test
    public void findingCookieExtendsExpiryDate() throws Exception {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        Date dateNow = new Date();

        ProxyCookie proxyCookie = cookieStorage.generateCookieInDb("PROXYCOOKIE", "example.com", idp, security, touchPeriod, maxExpiry, null);
        Date initialExpiry = calculateDate(proxyCookie.getLastUpdated(), proxyCookie.getTouchPeriod());
        ProxyCookie maxExpiredCookie = ((DatabaseCookieStorage) cookieStorage).generateCookieInDb(new DefaultProxyCookie(UUID.randomUUID().toString(),
                "maxExpired", "example.com", idp, security, touchPeriod, maxExpiry, null, calculateDate(dateNow, -(maxExpiry - 5)), calculateDate(dateNow, -5)));

        Thread.sleep(100); // ms

        Date initialMaxExpiry = calculateDate(maxExpiredCookie.getCreated(), maxExpiredCookie.getMaxExpiry());
        Date expiryUponInstantiation = calculateDate(maxExpiredCookie.getLastUpdated(), maxExpiredCookie.getTouchPeriod());

        Optional<ProxyCookie> foundCookieOptional = cookieStorage.findCookie(proxyCookie.getUuid(), proxyCookie.getHost(), proxyCookie.getIdp());
        Optional<ProxyCookie> foundMaxExpiredOptional = cookieStorage.findCookie(maxExpiredCookie.getUuid(), maxExpiredCookie.getHost(), maxExpiredCookie.getIdp());

        Date extendedExpiry = calculateDate(foundMaxExpiredOptional.get().getLastUpdated(), foundMaxExpiredOptional.get().getTouchPeriod());
        Date newMaxExpiry = calculateDate(foundMaxExpiredOptional.get().getCreated(), foundMaxExpiredOptional.get().getMaxExpiry());

        Assert.assertTrue(foundCookieOptional.isPresent());
        Assert.assertTrue(foundMaxExpiredOptional.isPresent());

        Assert.assertEquals(proxyCookie.toString(), foundCookieOptional.get().toString());
        Assert.assertEquals(maxExpiredCookie.toString(), foundMaxExpiredOptional.get().toString());

        Assert.assertNotEquals(expiryUponInstantiation, extendedExpiry);
        Assert.assertNotEquals(initialMaxExpiry, extendedExpiry);
        Assert.assertEquals(initialMaxExpiry, newMaxExpiry);

        Date newExpiry = calculateDate(foundCookieOptional.get().getLastUpdated(), proxyCookie.getTouchPeriod());

        Assert.assertNotEquals(newExpiry, initialExpiry);
        Assert.assertTrue(newExpiry.after(initialExpiry));
    }


    @Test
    public void findValidCookies() throws Exception {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        Date dateNow = new Date();

        // Either needs to cast CookieStorage to DatabaseCookieStorage, or implement generateCookieInDb(ProxyCookie) in CookieStorage interface
        ProxyCookie validCookie = ((DatabaseCookieStorage) cookieStorage).generateCookieInDb(new DefaultProxyCookie(UUID.randomUUID().toString(),
                "valid", "example.com", idp, security, touchPeriod, maxExpiry, null, dateNow, dateNow));
        ProxyCookie expiredCookie = ((DatabaseCookieStorage) cookieStorage).generateCookieInDb(new DefaultProxyCookie(UUID.randomUUID().toString(),
                "expired", "example.com", idp, security, touchPeriod, maxExpiry, null, calculateDate(dateNow, -(touchPeriod + 1)), calculateDate(dateNow, -(touchPeriod + 1))));
        ProxyCookie maxExpiredCookie = ((DatabaseCookieStorage) cookieStorage).generateCookieInDb(new DefaultProxyCookie(UUID.randomUUID().toString(),
                "maxExpired", "example.com", idp, security, touchPeriod, maxExpiry, null, calculateDate(dateNow, -(maxExpiry + 1)), dateNow));

        Assert.assertTrue(validCookie.isValid());
        Assert.assertFalse(expiredCookie.isValid());
        Assert.assertFalse(maxExpiredCookie.isValid());

        Optional<ProxyCookie> foundValidCookie = cookieStorage.findCookie(validCookie.getUuid(), validCookie.getHost(), validCookie.getIdp());
        Optional<ProxyCookie> foundExpiredCookie = cookieStorage.findCookie(expiredCookie.getUuid(), expiredCookie.getHost(), expiredCookie.getIdp());
        Optional<ProxyCookie> foundMaxExpiredCookie = cookieStorage.findCookie(maxExpiredCookie.getUuid(), maxExpiredCookie.getHost(), maxExpiredCookie.getIdp());

        Assert.assertTrue(foundValidCookie.isPresent());
        Assert.assertFalse(foundExpiredCookie.isPresent());
        Assert.assertFalse(foundMaxExpiredCookie.isPresent());
    }

    @Test
    public void removeCookie() {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        ProxyCookie cookie = cookieStorage.generateCookieInDb("test-cookie", "host.com", idp, security, touchPeriod, maxExpiry, userData);
        ProxyCookie cookie2 = cookieStorage.generateCookieInDb("test-cookie2", "google.com", idp2, security, touchPeriod, maxExpiry, userData);

        Optional<ProxyCookie> foundCookie = cookieStorage.findCookie(cookie.getUuid(), cookie.getHost(), cookie.getIdp());
        Optional<ProxyCookie> foundCookie2 = cookieStorage.findCookie(cookie2.getUuid(), cookie2.getHost(), cookie2.getIdp());

        Assert.assertTrue(foundCookie.isPresent());
        Assert.assertTrue(foundCookie2.isPresent());
        // Testing userData input in database is correctly stored and returned
        Assert.assertTrue(foundCookie.get().getUserData().containsKey("pid"));
        Assert.assertTrue(foundCookie.get().getUserData().containsKey("aud"));
        Assert.assertTrue(foundCookie2.get().getUserData().containsKey("sub"));
        Assert.assertTrue(foundCookie2.get().getUserData().containsKey("name"));

        cookieStorage.removeCookie(cookie.getUuid());

        Optional<ProxyCookie> foundCookieAfterRemoval = cookieStorage.findCookie(cookie.getUuid(), cookie.getHost(), cookie.getIdp());
        Optional<ProxyCookie> foundCookie2BeforeRemoval = cookieStorage.findCookie(cookie2.getUuid(), cookie2.getHost(), cookie2.getIdp());

        Assert.assertFalse(foundCookieAfterRemoval.isPresent());
        Assert.assertTrue(foundCookie2BeforeRemoval.isPresent());

        cookieStorage.removeCookie(cookie.getUuid()); // Removing non-existent cookie
        cookieStorage.removeCookie(cookie2.getUuid());

        Optional<ProxyCookie> foundCookieAfterTwoRemovals = cookieStorage.findCookie(cookie.getUuid(), cookie.getHost(), cookie.getIdp());
        Optional<ProxyCookie> foundCookie2AfterRemoval = cookieStorage.findCookie(cookie2.getUuid(), cookie2.getHost(), cookie2.getIdp());

        Assert.assertFalse(foundCookieAfterTwoRemovals.isPresent());
        Assert.assertFalse(foundCookie2AfterRemoval.isPresent());

    }

    private Date calculateDate(Date date, int minutes) {
        return new Date(date.getTime() + minutes * MINUTE);
    }
}
