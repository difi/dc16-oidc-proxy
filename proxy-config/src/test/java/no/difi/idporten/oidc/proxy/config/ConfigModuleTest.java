package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.ConfigProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConfigModuleTest {

    @Test
    public void providesConfigProvider() {
        Injector injector = Guice.createInjector(new ConfigModule());
        Assert.assertNotNull(injector.getBinding(ConfigProvider.class));
    }

}
