package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.ConfigFactory;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStreamReader;


public class TypesafeHostConfigTest {

    private static final String patternIp = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}:[0-9]{1,5}";

    private HostConfig config;

    @BeforeClass
    public void injectHostConfigProvider() {
        this.config = new TypesafeHostConfig(ConfigFactory.parseReader(new InputStreamReader(getClass().getResourceAsStream("/hostConfig/simple.conf"))));
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
