package no.difi.idporten.oidc.proxy.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

public class DefaultProxyCookieTest {

    @Test
    public void createSimpleCookie() {
        String identifier = UUID.randomUUID().toString();
        DefaultProxyCookie cookie = new DefaultProxyCookie(identifier, "PROXYCOOKIE", "domain.com", "/", null, null, null);

        Assert.assertEquals(cookie.getUuid(), identifier);

        Assert.assertTrue(cookie.toString().contains(identifier));
        Assert.assertTrue(cookie.toString().contains("domain.com"));
    }
}
