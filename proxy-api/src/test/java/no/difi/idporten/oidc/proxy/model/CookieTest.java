package no.difi.idporten.oidc.proxy.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.UUID;

public class CookieTest {

    private String identifier;

    @Test
    public void testSetExpiry(){
        Date expiry = new Date();
        Date maxExpiry = new Date();
        Cookie newCookie = new Cookie(identifier, "domain.com", expiry, maxExpiry, null);

        Date testExpiry = new Date();
        testExpiry.setYear(1999);
        newCookie.setExpiry(testExpiry);
        Assert.assertNotEquals(newCookie.getExpiry(),testExpiry);

        Date newExpiry = new Date();
        newExpiry.setYear(2050);
        newCookie.setExpiry(newExpiry);
        Assert.assertNotEquals(newCookie.getExpiry(),newExpiry);
    }

    @Test
    public void checkIsValid(){
        Date expiry = new Date();
        Date maxExpiry = new Date();
        Cookie cookie = new Cookie(identifier, "domain.com", expiry, maxExpiry, null);

        Assert.assertTrue(cookie.isValid());
        expiry.setYear(2550);
        maxExpiry.setYear(2007);
        Assert.assertFalse(cookie.isValid());
    }
    @Test
    public void simple() {
        String identifier = UUID.randomUUID().toString();
        Date expiry = new Date();
        expiry.setTime(255);
        Date maxExpiry = new Date();
        maxExpiry.setTime(100);

        Cookie cookie = new Cookie(identifier, "domain.com", expiry, maxExpiry, null);

        Assert.assertEquals(cookie.getUuid(), identifier);

        Assert.assertTrue(cookie.toString().contains(identifier));
        Assert.assertTrue(cookie.toString().contains("domain.com"));
        Assert.assertTrue(cookie.isValid());
        expiry.setYear(2550);
        maxExpiry.setYear(2007);
        Assert.assertFalse(cookie.isValid());





    }
}
