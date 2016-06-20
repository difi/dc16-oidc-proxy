package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CookieHandlerTest {

    @Test
    public void simple() {
        CookieStorage cookieStorage = new CookieHandler();

        String identifier = cookieStorage.generateCookie("example.com");
        Assert.assertNotNull(identifier);

        Assert.assertTrue(cookieStorage.findCookie(identifier, "example.com").isPresent());
        Assert.assertFalse(cookieStorage.findCookie(identifier, "domain.com").isPresent());
    }
}
