package no.difi.idporten.oidc.proxy.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StorageModuleTest {

    @Test
    public void simple() {
        Injector injector = Guice.createInjector(new StorageModule());

        Assert.assertNotNull(injector.getBinding(CookieStorage.class));
    }

}
