package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.ConfigFactory;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TypesafeCookieConfigTest {


    private HostConfigProvider hostConfigProvider = new TypesafeHostConfigProvider(ConfigFactory.load());

    private HostConfig hostConfig;

    private CookieConfig cookieConfig;

    @BeforeTest
    public void injectHostConfigProvider() {
        this.hostConfig = hostConfigProvider.getByHostname("www.difi.no");
        this.cookieConfig = hostConfig.getCookieConfig();
    }


    @Test
    public void testGetMaxExpiry() {
        Assert.assertEquals(cookieConfig.getMaxExpiry(), 120);
        Assert.assertNotEquals(cookieConfig.getMaxExpiry(), 160);
    }

    @Test
    public void testGetTouchPeriod() {
        Assert.assertEquals(cookieConfig.getTouchPeriod(), 30);
        Assert.assertNotEquals(cookieConfig.getTouchPeriod(), 50);
    }


}
