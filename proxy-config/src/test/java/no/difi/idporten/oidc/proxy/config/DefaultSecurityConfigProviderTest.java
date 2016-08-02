package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Optional;

public class DefaultSecurityConfigProviderTest {

    private SecurityConfigProvider provider;

    private SecurityConfig securityConfigWithIdpPathChecker;

    private SecurityConfig securityConfigWithPathPathChecker;


    @BeforeTest
    public void injectSecurityConfigProvider() {
        Injector injector = Guice.createInjector(new ConfigModule());
        provider = injector.getInstance
                (SecurityConfigProvider.class);
        String HOST = "www.difi.no";
        String pathChecksIdpForScopeRedirectAndSecurity = "/";
        securityConfigWithIdpPathChecker = provider.getConfig(HOST, pathChecksIdpForScopeRedirectAndSecurity).get();
        String pathChecksPathForScopeRedirectAndSecurity = "/app5/";
        securityConfigWithPathPathChecker = provider.getConfig(HOST, pathChecksPathForScopeRedirectAndSecurity).get();

    }

    @Test
    public void testCreateIdentityProvider() throws Exception {
        Optional<IdentityProvider> identityProvider = securityConfigWithIdpPathChecker.createIdentityProvider();
        Assert.assertTrue(identityProvider.isPresent());
        Assert.assertEquals(identityProvider.get().generateRedirectURI(), "https://eid-exttest.difi.no/idporten-oidc-provider/authorize?scope=openid&client_id=dificamp&response_type=code&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2F");
    }

    @Test
    public void testEmptyHostnameInjection() {
        Assert.assertEquals(provider.getConfig("", "/"), Optional.empty());
    }

    @Test
    public void testGetCookieConfig() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getCookieConfig());
        Assert.assertEquals(securityConfigWithIdpPathChecker.getCookieConfig().getName(), "dificookie");
    }

    @Test
    public void testGetBackend() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getBackend());
    }


    @Test
    public void testGetHostname() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getHostname());
        Assert.assertEquals(securityConfigWithIdpPathChecker.getHostname(), "www.difi.no");
    }

    @Test
    public void testGetPath() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getPath());
        Assert.assertEquals(securityConfigWithIdpPathChecker.getPath(), "/");
    }

    @Test
    public void testGetIdp() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getIdp());
        Assert.assertEquals(securityConfigWithIdpPathChecker.getIdp(), "idporten");
    }

    @Test
    public void testGetIdpClass() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getIdpClass());
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getIdpClass(), "no.difi.idporten.oidc.proxy.idp.IdportenIdentityProvider");
    }

    @Test
    public void testgetClientId() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getClientId());
        Assert.assertEquals(securityConfigWithIdpPathChecker.getClientId(), "dificamp");
    }

    @Test
    public void testGetPassword() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getPassword());
        Assert.assertEquals(securityConfigWithIdpPathChecker.getPassword(), "password");
    }

    @Test
    public void testGetExistingParameter() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getParameter("security"));
        Assert.assertEquals(securityConfigWithIdpPathChecker.getParameter("security"), "3"); // Gets a string when retrieving value directly
    }

    @Test
    public void testGetNonexistingParameter() {
        Assert.assertEquals(securityConfigWithIdpPathChecker.getParameter("scope"), "");
    }

    @Test
    public void testGetSecurity() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getSecurity());
        Assert.assertEquals(securityConfigWithIdpPathChecker.getSecurity(), 3);
    }

    @Test
    public void testGetRedirectUriFromPath() {
        Assert.assertNotNull(securityConfigWithPathPathChecker.getRedirectUri());
        Assert.assertEquals(securityConfigWithPathPathChecker.getRedirectUri(), "http://localhost:8080/redirect");
    }

    @Test
    public void testGetRedirectUriFromIdp() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getRedirectUri());
        Assert.assertEquals(securityConfigWithIdpPathChecker.getRedirectUri(), "http://localhost:8080/");
    }

    @Test
    public void testGetScopeFromPath() {
        Assert.assertNotNull(securityConfigWithPathPathChecker.getScope());
        Assert.assertEquals(securityConfigWithPathPathChecker.getScope(), "email");
    }

    @Test
    public void testGetScopeFromIdp() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getScope());
        Assert.assertEquals(securityConfigWithIdpPathChecker.getScope(), "openid");
    }

    @Test
    public void testGetSecurityFromPath() {
        Assert.assertNotNull(securityConfigWithPathPathChecker.getSecurity());
        Assert.assertEquals(securityConfigWithPathPathChecker.getSecurity(), 2);
    }

    @Test
    public void testGetSecurityFromIdp() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getSecurity());
        Assert.assertEquals(securityConfigWithIdpPathChecker.getSecurity(), 3);
    }

    @Test
    public void testGetUserData() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getUserDataNames());
        Assert.assertTrue(securityConfigWithIdpPathChecker.getUserDataNames().contains("pid"));
    }

    @Test
    public void testGetUnsecuredPaths() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getUnsecuredPaths());
        Assert.assertTrue(securityConfigWithIdpPathChecker.getUnsecuredPaths().contains("/studier"));
    }

    @Test
    public void testIsSecured() {
        Assert.assertTrue(securityConfigWithIdpPathChecker.isSecured());
    }

    @Test
    public void testToString() {
        Assert.assertTrue(securityConfigWithIdpPathChecker.toString().contains("DefaultSecurityConfig"));
    }

    @Test
    public void testGetSalt() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getSalt());
        Assert.assertEquals(securityConfigWithIdpPathChecker.getSalt(), "2LMC539EF8nf04O9gndsfERGh3HI4ugjRTHnfAGmlwkSEhfnbi82finsdf");
    }

    @Test
    public void testIsTotallyUnsecured() {
        Assert.assertTrue(securityConfigWithIdpPathChecker.isTotallyUnsecured("/studier"));

    }

    @Test
    public void getLogoutRedirectUri() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getLogoutRedirectUri());
        Assert.assertEquals(securityConfigWithIdpPathChecker.getLogoutRedirectUri(), "http://localhost:8080/logout-difi");
    }

    @Test
    public void getLogoutPostUri() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getLogoutPostUri());
        Assert.assertEquals(securityConfigWithIdpPathChecker.getLogoutPostUri(), "/logout");
    }
}
