package no.difi.idporten.oidc.proxy.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.DefaultProxyCookie;
import org.h2.store.Data;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Sondre on 13/07/16.
 */
public class DatabaseCookieStorageTest {

    private static final int MINUTE = 1000 * 60;
    private int touchPeriod= 20;
    private int maxExpiry= 120;

    private Injector injector;

    @Test
    public void beforeClass() {
        injector = Guice.createInjector(new StorageModule());
    }

    @Test
    public void createSimpleCookieInStorage() {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        ProxyCookie proxyCookie = cookieStorage.generateCookieInDb("PROXYCOOKIE", "example.com", "/app1", touchPeriod, maxExpiry, null);
        Assert.assertNotNull(proxyCookie);

        Assert.assertTrue(cookieStorage.findCookie(proxyCookie.getUuid(), "example.com", "/app1").isPresent());
        Assert.assertTrue(cookieStorage.findCookie(proxyCookie.getUuid(), "example.com", "/app2").isPresent());
        Assert.assertFalse(cookieStorage.findCookie(proxyCookie.getUuid(), "domain.com", "/app1").isPresent());
    }

    @Test
    public void findingCookieExtendsExpiryDate() throws Exception {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        Date dateNow = new Date();

        ProxyCookie proxyCookie = cookieStorage.generateCookieInDb("PROXYCOOKIE", "example.com", "/app1", touchPeriod, maxExpiry, null);
        Date initialExpiry = calculateDate(proxyCookie.getLastUpdated(), proxyCookie.getTouchPeriod());
        ProxyCookie maxExpiredCookie = ((DatabaseCookieStorage) cookieStorage).generateCookieInDb(new DefaultProxyCookie(UUID.randomUUID().toString(),
                "maxExpired", "example.com", "/app1", touchPeriod, maxExpiry, null, calculateDate(dateNow, -(maxExpiry-5)), calculateDate(dateNow, -5)));

        Thread.sleep(100); // ms

        Date initialMaxExpiry = calculateDate(maxExpiredCookie.getCreated(), maxExpiredCookie.getMaxExpiry());
        Date expiryUponInstantiation = calculateDate(maxExpiredCookie.getLastUpdated(), maxExpiredCookie.getTouchPeriod());

        Optional<ProxyCookie> foundCookieOptional = cookieStorage.findCookie(proxyCookie.getUuid(), proxyCookie.getHost(), proxyCookie.getPath());
        Optional<ProxyCookie> foundMaxExpiredOptional= cookieStorage.findCookie(maxExpiredCookie.getUuid(), maxExpiredCookie.getHost(), maxExpiredCookie.getPath());

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
                "valid", "example.com", "/app1", touchPeriod, maxExpiry, null, dateNow, dateNow));
        ProxyCookie expiredCookie = ((DatabaseCookieStorage) cookieStorage).generateCookieInDb(new DefaultProxyCookie(UUID.randomUUID().toString(),
                "expired", "example.com", "/app1", touchPeriod, maxExpiry, null, calculateDate(dateNow, -(touchPeriod+1)), calculateDate(dateNow, -(touchPeriod+1))));
        ProxyCookie maxExpiredCookie = ((DatabaseCookieStorage) cookieStorage).generateCookieInDb(new DefaultProxyCookie(UUID.randomUUID().toString(),
                "maxExpired", "example.com", "/app1", touchPeriod, maxExpiry, null, calculateDate(dateNow, -(maxExpiry+1)), dateNow));

        Assert.assertTrue(validCookie.isValid());
        Assert.assertFalse(expiredCookie.isValid());
        Assert.assertFalse(maxExpiredCookie.isValid());

        Optional<ProxyCookie> foundValidCookie = cookieStorage.findCookie(validCookie.getUuid(), validCookie.getHost(), validCookie.getPath());
        Optional<ProxyCookie> foundExpiredCookie = cookieStorage.findCookie(expiredCookie.getUuid(), expiredCookie.getHost(), expiredCookie.getPath());
        Optional<ProxyCookie> foundMaxExpiredCookie = cookieStorage.findCookie(maxExpiredCookie.getUuid(), maxExpiredCookie.getHost(), maxExpiredCookie.getPath());

        Assert.assertTrue(foundValidCookie.isPresent());
        Assert.assertFalse(foundExpiredCookie.isPresent());
        Assert.assertFalse(foundMaxExpiredCookie.isPresent());

    }

    private Date calculateDate(Date date, int minutes){
        return new Date(date.getTime() + minutes * MINUTE);
    }
}
