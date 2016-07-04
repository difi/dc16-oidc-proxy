package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class DefaultSecurityConfigProviderTest {

    private SecurityConfigProvider provider;
    private SecurityConfig securityConfigWithPathOne;
    private SecurityConfig securityConfigWithPathTwo;
    private final String HOST = "www.difi.no";
    private final String PATHONE = "/";
    private final String PATHTWO = "/app5/";

    @BeforeTest
    public void injectSecurityConfigProvider(){
        Injector injector = Guice.createInjector(new ConfigModule());
        provider = injector.getInstance(SecurityConfigProvider.class);
        securityConfigWithPathOne = provider.getConfig(HOST, PATHONE).get();
        securityConfigWithPathTwo = provider.getConfig(HOST, PATHTWO).get();
    }


    @Test
    public void testGetHostname(){
        Assert.assertNotNull(securityConfigWithPathOne.getHostname());
        Assert.assertEquals(securityConfigWithPathOne.getHostname(), "www.difi.no");
    }

    @Test
    public void testGetPath(){
        Assert.assertNotNull(securityConfigWithPathOne.getPath());
        Assert.assertEquals(securityConfigWithPathOne.getPath(), "/");
    }

    @Test
    public void testGetIdp(){
        Assert.assertNotNull(securityConfigWithPathOne.getIdp());
        Assert.assertEquals(securityConfigWithPathOne.getIdp(), "idporten");
    }

    @Test
    public void testGetIdpClass(){
        Assert.assertNotNull(securityConfigWithPathOne.getIdpClass());
        Assert.assertNotNull(securityConfigWithPathOne.getIdpClass(), "no.difi.idporten.oidc.proxy.idp.IdportenIdentityProvider");
    }

    @Test
    public void testgetClient_id(){
        Assert.assertNotNull(securityConfigWithPathOne.getClient_id());
        Assert.assertEquals(securityConfigWithPathOne.getClient_id(), "dificamp");
    }

    @Test
    public void testGetPassword(){
        Assert.assertNotNull(securityConfigWithPathOne.getPassword());
        Assert.assertEquals(securityConfigWithPathOne.getPassword(), "password");
    }

    @Test
    public void testGetExistingParameter(){
        Assert.assertNotNull(securityConfigWithPathOne.getParameter("quality"));
        Assert.assertEquals(securityConfigWithPathOne.getParameter("quality"), "3");
    }

    @Test
    public void testGetNonexistingParameter(){
        Assert.assertEquals(securityConfigWithPathOne.getParameter("scope"), "");
    }

    @Test
    public void testGetSecurity(){
        Assert.assertNotNull(securityConfigWithPathOne.getSecurity());
        Assert.assertEquals(securityConfigWithPathOne.getSecurity(), "3");
    }

    @Test
    public void testGetRedirect_uriFromPath(){
        Assert.assertNotNull(securityConfigWithPathTwo.getRedirect_uri());
        Assert.assertEquals(securityConfigWithPathTwo.getRedirect_uri(), "http://localhost:8080/redirect");
    }

    @Test
    public void testGetRedirect_uriFromIdp(){
        Assert.assertNotNull(securityConfigWithPathOne.getRedirect_uri());
        Assert.assertEquals(securityConfigWithPathOne.getRedirect_uri(), "http://localhost:8080/");
    }

    @Test
    public void testGetScopeFromPath(){
        Assert.assertNotNull(securityConfigWithPathTwo.getScope());
        Assert.assertEquals(securityConfigWithPathTwo.getScope(), "email");
    }

    @Test
    public void testGetScopeFromIdp(){
        Assert.assertNotNull(securityConfigWithPathOne.getScope());
        Assert.assertEquals(securityConfigWithPathOne.getScope(), "openid");
    }
}
