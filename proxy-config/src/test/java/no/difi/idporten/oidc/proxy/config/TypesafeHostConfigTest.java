package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.ConfigFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;


public class TypesafeHostConfigTest {

    private TypesafeHostConfig config;

    @BeforeTest
    public void injectHostConfigProvider() {
        this.config = new TypesafeHostConfig(ConfigFactory.parseReader(new InputStreamReader(getClass().getResourceAsStream("/hostConfig/simple.conf"))));
    }

    @Test
    public void returnBackendRouting() throws Exception {
        Assert.assertNotNull(config.getBackend());
        Assert.assertNotNull(config.getBackend());

        Field field = TypesafeHostConfig.class.getDeclaredField("backendIndex");
        field.setAccessible(true);
        AtomicInteger atomicInteger = (AtomicInteger) field.get(config);
        atomicInteger.addAndGet(Integer.MAX_VALUE);

        Assert.assertNotNull(config.getBackend());
        Assert.assertNotNull(config.getBackend());
    }

    @Test
    public void returnPath() {
        Assert.assertTrue(config.getPathFor("/app1/test").isPresent());
        Assert.assertTrue(config.getPathFor("/app1/").isPresent());
        Assert.assertTrue(config.getPathFor("/app1").isPresent());

        Assert.assertTrue(config.getPathFor("/app2/test").isPresent());
        Assert.assertTrue(config.getPathFor("/app2/").isPresent());
        Assert.assertTrue(config.getPathFor("/app2").isPresent());

        Assert.assertFalse(config.getPathFor("/app3").isPresent());
        Assert.assertFalse(config.getPathFor("/").isPresent());
        Assert.assertFalse(config.getPathFor("/about/").isPresent());
    }
}