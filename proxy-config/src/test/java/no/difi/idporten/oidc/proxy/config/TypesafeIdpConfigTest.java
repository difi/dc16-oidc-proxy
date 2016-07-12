package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.ConfigFactory;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.model.IdpConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TypesafeIdpConfigTest {

    private IdpConfig idpConfig;
    private IdpConfigProvider idpConfigProvider;

    @BeforeTest
    public void injectIdpConfigProvider() {
        this.idpConfigProvider = new TypesafeIdpConfigProvider(ConfigFactory.load());
        this.idpConfig = idpConfigProvider.getByIdentifier("idporten");
    }

    @Test
    public void testGetIdpClass() {
        Assert.assertNotNull(idpConfig.getIdpClass());
        Assert.assertEquals(idpConfig.getIdpClass(), "no.difi.idporten.oidc.proxy.idp.IdportenIdentityProvider");
    }

    @Test
    public void testGetPassword() {
        Assert.assertNotNull(idpConfig.getPassword());
        Assert.assertEquals(idpConfig.getPassword(), "password");
    }

    @Test
    public void testGetScope() {
        Assert.assertNotNull(idpConfig.getScope());
        Assert.assertEquals(idpConfig.getScope(), "openid");
    }

    @Test
    public void testGetRedirect_uri() {
        Assert.assertNotNull(idpConfig.getRedirectUri());
        Assert.assertEquals(idpConfig.getRedirectUri(), "http://localhost:8080/");
    }

    @Test
    public void testGetClient_id() {
        Assert.assertNotNull(idpConfig.getClientId());
        Assert.assertEquals(idpConfig.getClientId(), "dificamp");
    }

    @Test
    public void testGetUserData() {
        Assert.assertNotNull(idpConfig.getUserDataNames());
        Assert.assertTrue(idpConfig.getUserDataNames().contains("pid"));

    }
}
