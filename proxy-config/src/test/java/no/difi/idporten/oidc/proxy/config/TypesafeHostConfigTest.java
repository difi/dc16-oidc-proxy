package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.ConfigFactory;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;


public class TypesafeHostConfigTest {

    private HostConfig hostConfig;

    private HostConfigProvider hostConfigProvider = new TypesafeHostConfigProvider(ConfigFactory.load());

    @BeforeTest
    public void injectHostConfigProvider() {
        this.hostConfig = hostConfigProvider.getByHostname("www.difi.no");
    }

    @Test
    public void returnBackendRouting() throws Exception {
        Assert.assertNotNull(hostConfig.getBackend());
        Assert.assertNotNull(hostConfig.getBackend());

        Field field = TypesafeHostConfig.class.getDeclaredField("backendIndex");
        field.setAccessible(true);
        AtomicInteger atomicInteger = (AtomicInteger) field.get(hostConfig);
        atomicInteger.addAndGet(Integer.MAX_VALUE);

        Assert.assertNotNull(hostConfig.getBackend());
        Assert.assertNotNull(hostConfig.getBackend());
    }

    @Test
    public void testGetHostname() {
        Assert.assertNotNull(hostConfig.getHostname());
        Assert.assertEquals(hostConfig.getHostname(), "www.difi.no");
    }

    @Test
    public void testGetPathFor() {
        Assert.assertNotNull(hostConfig.getPathFor("/om-oss"));
        Assert.assertNotEquals(hostConfig.getPathFor("/om-oss"), "/om_oss");
    }

    @Test
    public void testGetCookieConfig() {
        Assert.assertNotNull(hostConfig.getCookieConfig());
        Assert.assertEquals(hostConfig.getCookieConfig().getName(), "dificookie");
        Assert.assertNotNull(hostConfig.getCookieConfig().getName(), "PROXYCOOKIE");
    }

    @Test
    public void testGetUnsecuredPaths() {
        Assert.assertNotNull(hostConfig.getUnsecuredPaths());
        Assert.assertTrue(hostConfig.getUnsecuredPaths().contains("/studier"));
    }

    @Test
    public void testGetSalt(){
        Assert.assertNotNull(hostConfig.getSalt());
        Assert.assertEquals(hostConfig.getSalt(), "2LMC539EF8nf04O9gndsfERGh3HI4ugjRTHnfAGmlwkSEhfnbi82finsdf");
    }

    @Test
    public void testIsTotallyUnsecured(){
        Assert.assertTrue(hostConfig.isTotallyUnsecured("/studier"));

    }

    @Test
    public void getLogoutRedirectUri(){
        Assert.assertNotNull(hostConfig.getLogoutRedirectUri());
        Assert.assertEquals(hostConfig.getLogoutRedirectUri(), "http://localhost:8080/logout-difi");
    }

    @Test
    public void getLogoutPostUri(){
        Assert.assertNotNull(hostConfig.getLogoutPostUri());
        Assert.assertEquals(hostConfig.getLogoutPostUri(), "/logout");
    }


}
