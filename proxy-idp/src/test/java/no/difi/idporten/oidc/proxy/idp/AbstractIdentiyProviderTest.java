package no.difi.idporten.oidc.proxy.idp;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import no.difi.idporten.oidc.proxy.model.UserData;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class AbstractIdentiyProviderTest {

    SecurityConfig securityConfig;
    IdportenIdentityProvider idportenIdentityProvider;

    @BeforeTest
    public void injectSecurityConfigProvider() throws Exception{
        Injector injector = Guice.createInjector(new ConfigModule());
        SecurityConfigProvider provider = injector.getInstance(SecurityConfigProvider.class);
        this.securityConfig = provider.getConfig("www.ntnu.no", "idporten").get();
        this.idportenIdentityProvider = new IdportenIdentityProvider(securityConfig);
    }

    @Test
    public void testTokenHasValidSignature() throws Exception{
        Assert.assertTrue(idportenIdentityProvider.tokenHasValidSignature());
    }
}
