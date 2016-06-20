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


        AccessRequirement accessRequirement = configProvider.forUri(URI.create("http://www.difi.no/app2/"));
        Assert.assertNotNull(accessRequirement);
        Assert.assertEquals(accessRequirement.getMinLevel(),4);
        Assert.assertNotNull(accessRequirement.getHost());

        AccessRequirement pathAccessRequirement = configProvider.forUri(URI.create("http://www.difi.no/app1/"));
        Assert.assertNotNull(accessRequirement);
        Assert.assertEquals(pathAccessRequirement.getMinLevel(),3);

        pathAccessRequirement = configProvider.forUri(URI.create("http://www.difi.no/app5/"));
        Assert.assertEquals(pathAccessRequirement.getMinLevel(),2);
        //Assert.assertNotNull(accessRequirement.getPath());




    }
}
