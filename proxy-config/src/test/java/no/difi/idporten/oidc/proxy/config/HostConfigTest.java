package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class HostConfigTest {

    private static final String DIFIHOSTNAME = "www.difi.no"; // this is a hostname that should be configured and valid
    private HostConfig config;



    @BeforeClass
    public void injectHostConfigProvider() {
        Injector injector = Guice.createInjector(new ConfigModule());
        config = injector.getInstance(HostConfigProvider.class).getByHostname(DIFIHOSTNAME);
    }

    @Test
    public void testCanInitiate() {
        Assert.assertNotNull(config);
    }

    @Test
    public void testCanReturnBackendIPAddress() {
        Assert.assertEquals(config.getBackend().getClass(), String.class);
    }

}
