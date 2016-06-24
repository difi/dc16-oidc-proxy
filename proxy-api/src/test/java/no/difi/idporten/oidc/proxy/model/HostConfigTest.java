package no.difi.idporten.oidc.proxy.model;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;


public class HostConfigTest {

    private static final String patternIp = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}:[0-9]{1,5}";

    private HostConfig config;

    @BeforeClass
    public void injectHostConfigProvider() {
        config = new HostConfig(Arrays.asList("127.0.0.1:80", "127.0.1.1:80"), Arrays.asList(new Path("/app1"), new Path("/app2")));
    }

    @Test
    public void returnBackendRouting() {
        Assert.assertTrue(config.getBackend().matches(patternIp));
        Assert.assertTrue(config.getBackend().matches(patternIp));
        Assert.assertTrue(config.getBackend().matches(patternIp));
    }

    @Test
    public void returnPath() {
        Assert.assertTrue(config.getForPath("/app1/test").isPresent());
        Assert.assertTrue(config.getForPath("/app1/").isPresent());
        Assert.assertTrue(config.getForPath("/app1").isPresent());

        Assert.assertTrue(config.getForPath("/app2/test").isPresent());
        Assert.assertTrue(config.getForPath("/app2/").isPresent());
        Assert.assertTrue(config.getForPath("/app2").isPresent());

        Assert.assertFalse(config.getForPath("/app3").isPresent());
        Assert.assertFalse(config.getForPath("/").isPresent());
        Assert.assertFalse(config.getForPath("/about/").isPresent());
    }
}
