package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class TypesafeHostConfigProviderTest {

    private HostConfigProvider provider;
    private static final String DIFIHOSTNAME = "www.difi.no"; // this is a hostname that should be configured and valid
    private static final String UNKNOWNHOSTNAME = "www.facebook.com"; // this is a hostname that should not be configured

    @BeforeClass
    public void injectHostConfigProvider() {
        Injector injector = Guice.createInjector(new ConfigModule());
        provider = injector.getInstance(HostConfigProvider.class);
    }

    @Test
    public void testCanReturnKnownHostConfig() {
        Assert.assertNotNull(provider.getByHostname(DIFIHOSTNAME));
    }

    @Test
    public void testReturnsNullWhenUnknownHostConfig() {
        Assert.assertNull(provider.getByHostname(UNKNOWNHOSTNAME));
    }
}
