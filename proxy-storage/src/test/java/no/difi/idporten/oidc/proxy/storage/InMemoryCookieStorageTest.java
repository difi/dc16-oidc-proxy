package no.difi.idporten.oidc.proxy.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class InMemoryCookieStorageTest {

    private Injector injector;

    @Test
    public void beforeClass() {
        injector = Guice.createInjector(new StorageModule());
    }

    @Test
    public void simple() {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        String identifier = cookieStorage.generateCookie("example.com", null);
        Assert.assertNotNull(identifier);

        Assert.assertTrue(cookieStorage.findCookie(identifier, "example.com").isPresent());
        Assert.assertFalse(cookieStorage.findCookie(identifier, "domain.com").isPresent());
    }
}
