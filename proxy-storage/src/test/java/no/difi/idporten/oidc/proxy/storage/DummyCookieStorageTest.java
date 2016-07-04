package no.difi.idporten.oidc.proxy.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DummyCookieStorageTest {

    private Injector injector;

    @Test
    public void beforeClass() {
        injector = Guice.createInjector(new StorageModule());
    }

    @Test
    public void simple() {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        ProxyCookie proxyCookie = cookieStorage.generateCookieAsObject("PROXYCOOKIE", "example.com", "/", null);
        Assert.assertNotNull(proxyCookie);

        Assert.assertTrue(cookieStorage.findCookie(proxyCookie.getUuid(), "example.com", "/").isPresent());
        Assert.assertFalse(cookieStorage.findCookie(proxyCookie.getUuid(), "domain.com", "/").isPresent());
    }
}
