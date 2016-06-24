package no.difi.idporten.oidc.proxy.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

public class CookieTest {

    @Test
    public void simple() {
        String identifier = UUID.randomUUID().toString();
        Cookie cookie = new Cookie(identifier, "domain.com", null, null, null);

        Assert.assertEquals(cookie.getUuid(), identifier);

        Assert.assertTrue(cookie.toString().contains(identifier));
        Assert.assertTrue(cookie.toString().contains("domain.com"));
    }
}
