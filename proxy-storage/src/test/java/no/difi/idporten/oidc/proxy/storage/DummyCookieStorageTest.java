package no.difi.idporten.oidc.proxy.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.DefaultProxyCookie;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Optional;

public class DummyCookieStorageTest {

    private Injector injector;

    @Test
    public void beforeClass() {
        injector = Guice.createInjector(new StorageModule());
    }

    @Test
    public void createSimpleCookieInStorage() {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        ProxyCookie proxyCookie = cookieStorage.generateCookieAsObject("PROXYCOOKIE", "example.com", "/app1", null);
        Assert.assertNotNull(proxyCookie);

        Assert.assertTrue(cookieStorage.findCookie(proxyCookie.getUuid(), "example.com", "/app1").isPresent());
        Assert.assertFalse(cookieStorage.findCookie(proxyCookie.getUuid(), "example.com", "/app2").isPresent());
        Assert.assertFalse(cookieStorage.findCookie(proxyCookie.getUuid(), "domain.com", "/app1").isPresent());
    }

    @Test
    public void findingCookieExtendsExpiryDate() throws Exception {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        ProxyCookie proxyCookie = cookieStorage.generateCookieAsObject("PROXYCOOKIE", "example.com", "/app1", null);
        Date initialExpiry = proxyCookie.getExpiry();

        Thread.sleep(100);

        Optional<ProxyCookie> foundCookieOptional = cookieStorage.findCookie(proxyCookie.getUuid(), proxyCookie.getHost(), proxyCookie.getPath());
        Assert.assertTrue(foundCookieOptional.isPresent());
        Date newExpiry = foundCookieOptional.get().getExpiry();

        System.out.println(initialExpiry);
        System.out.println(newExpiry);

        Assert.assertNotEquals(newExpiry, initialExpiry);
        Assert.assertTrue(newExpiry.after(initialExpiry));
    }

    @Test
    public void removeExpiredCookies() throws Exception {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        Date dateNow = new Date();

        DefaultProxyCookie notExpiredCookie = cookieStorage.generateCookieAsObject("notExpired", "example.com", "/app1", null);
        DefaultProxyCookie expiredCookie = cookieStorage.generateCookieAsObject("expired", "example.com", "/app1", null);
        ProxyCookie maxExpiredCookie = cookieStorage.generateCookieAsObject("maxExpired", "example.com", "/app1", null);

        // Using reflection to change private field
        Field maxExpiryField = DefaultProxyCookie.class.getDeclaredField("maxExpiry");
        maxExpiryField.setAccessible(true);
        Date maxExpiryDate = new Date(dateNow.getTime() - 24 * 60 * 60 * 1000);
        maxExpiryField.set(maxExpiredCookie, maxExpiryDate);


        maxExpiredCookie = cookieStorage.findCookie(maxExpiredCookie.getUuid(), maxExpiredCookie.getHost(), maxExpiredCookie.getPath()).get();
        // the expiry date should now be updated to be equal to the max expiry
        Assert.assertEquals(maxExpiredCookie.getExpiry(), maxExpiryDate);


        expiredCookie.setExpiry(new Date(dateNow.getTime() - 24 * 60 * 60 * 1000)); // 24 hours ago
        cookieStorage.removeExpiredCookies();

        Assert.assertFalse(cookieStorage.findCookie(maxExpiredCookie.getUuid(), maxExpiredCookie.getHost(), maxExpiredCookie.getPath()).isPresent());
        Assert.assertFalse(cookieStorage.findCookie(expiredCookie.getUuid(), expiredCookie.getHost(), expiredCookie.getPath()).isPresent());
        Assert.assertTrue(cookieStorage.findCookie(notExpiredCookie.getUuid(), notExpiredCookie.getHost(), notExpiredCookie.getPath()).isPresent());
    }

    @Test
    public void expiredDateIsNeverHigherThanMaxExpiryDate() {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        Date dateNow = new Date();

        DefaultProxyCookie proxyCookie = cookieStorage.generateCookieAsObject("PROXYCOOKIE", "example.com", "/app1", null);

        Date maxExpiryDate = proxyCookie.getMaxExpiry();

        proxyCookie.setExpiry(new Date(dateNow.getTime() + 24 * 60 * 60 * 1000)); // 24 hours is longer than max expiry

        Assert.assertEquals(maxExpiryDate, proxyCookie.getExpiry());
    }
}
