package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.ConfigProvider;
import no.difi.idporten.oidc.proxy.model.AccessRequirement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;

public class TypesafeConfigProviderTest {

    @Test
    public void providesConfigProvider() {
        Injector injector = Guice.createInjector(new ConfigModule());
        Assert.assertNotNull(injector.getBinding(ConfigProvider.class));

        ConfigProvider configProvider = injector.getInstance(ConfigProvider.class);

        AccessRequirement accessRequirement = configProvider.forUri(URI.create("http://www.difi.no/"));
        Assert.assertNotNull(accessRequirement);
        Assert.assertNotNull(accessRequirement.getHost());
        Assert.assertEquals(accessRequirement.getMinLevel(), 0);

        Assert.assertNull(configProvider.forUri(URI.create("http://www.ntnu.no/")).getHost());
    }
}
