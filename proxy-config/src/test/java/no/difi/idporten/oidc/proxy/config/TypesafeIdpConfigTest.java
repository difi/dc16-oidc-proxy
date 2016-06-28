package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.ConfigFactory;
import no.difi.idporten.oidc.proxy.idp.IdportenIdentityProvider;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.InputStreamReader;

public class TypesafeIdpConfigTest {

    private TypesafeIdpConfig config;

    @BeforeTest
    public void injectIdpConfigProvider(){
        this.config = new TypesafeIdpConfig(ConfigFactory
                .parseReader(new InputStreamReader(getClass()
                        .getResourceAsStream("/idpConfig/simple.conf"))));
    }

    @Test
    public void configCanReturnIdp() {
        Assert.assertNotNull(config.getIdp());
        Assert.assertEquals(config.getIdp().getClass(), IdportenIdentityProvider.class);
    }




}
