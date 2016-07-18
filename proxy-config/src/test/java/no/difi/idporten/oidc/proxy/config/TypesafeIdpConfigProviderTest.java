package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TypesafeIdpConfigProviderTest {

    private IdpConfigProvider provider;

    private static final String IDPORTENIDENTIFIER = "idporten";

    private static final String UNKNOWNIDENTIFIER = "facebook";

    @BeforeClass
    public void injectIdpConfigProvider() {
        Injector injector = Guice.createInjector(new ConfigModule());
        provider = injector.getInstance(IdpConfigProvider.class);
    }

    @Test
    public void testCanReturnKnownIdpConfig() {
        Assert.assertNotNull(provider.getByIdentifier(IDPORTENIDENTIFIER));
    }

    @Test
    public void testReturnsNullWhenUnknownHostConfig() {
        Assert.assertNull(provider.getByIdentifier(UNKNOWNIDENTIFIER));
    }
}
