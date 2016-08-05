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

    @Test
    public void testGetJWKSet() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getJSONWebKeys());
        Assert.assertNotEquals(securityConfigWithIdpPathChecker.getJSONWebKeys(), "{\"keys\":[{\"kty\":\"RSA\",\"e\":\"AQAB\",\"use\":\"sig\",\"kid\":\"36e2e419341b693ec482d6676bafc3133061caaf\",\"alg\":\"RS256\",\"n\":\"vYaqrmTIqGbqK6oQO2nIC1H2FdhIf5J-Odv9--IzYJiEqOdchr2zHfF5CLpzKVgURxS3KzMcdcK_hJh_c0X14z9dCv1CINuvIz228h19iQdDlXIyvN_KDpFOro64LZ-b2CmPgE19v_u6WpgI8OByRUGPswwZ7s4DljmY-5WUVDK2MBr7vfr6oYT_Uh2UD6BUW0iGtqTMgqUfl_2FGBZYsoCEtcaRw44i1fOSHgPdNL7ppm0guRk_Pl4q3F39Ga_uqyAa2h6esJhSg4PpmTpayxUlEpcDB6X-1mH-1CYiHLEKQJ2tmQHBbrbpnBKtYqMKmUxj8Mg5eQxpdlQcjlxF7w\"},{\"kty\":\"RSA\",\"e\":\"AQAB\",\"use\":\"sig\",\"kid\":\"e27d33093814b052594840219c8f4b0070ee5a3a\",\"alg\":\"RS256\",\"n\":\"vNSQ2tMH7T20JgWCUMhQb2ofkE5oG0TFqXb-eOa3ap-BdujTeKUgS-ZZj7Apw_X3Bvf-yTkY_cFuH3paqUkKHy0BNQCo_Y4qPVa8u_57n2bFntHAz0Qi4YeXGxVTwgFa7X0gLFbhWjZBPmlj44vWUsFujqfARiWJRN-dUhKPaxcc7hUBnzRIs2Ll3tYZ2nYw9DT_l1qC9-b2zikWyZ_5bqv7l5Njq2Naf5GZug2m2OgH5lrnaxNU5eQhvMyajeld36GGAzn5a76Rr1fB3F-NaurzUDuw7mgmRjZU6aCjx-OqUwHsgnS3IY5a0EEuI6Hzc6T-GCmUBqUy85kko8595Q\"},{\"kty\":\"RSA\",\"e\":\"AQAB\",\"use\":\"sig\",\"kid\":\"d0ec514a32b6f88c0abd12a2840699bdd3deba9d\",\"alg\":\"RS256\",\"n\":\"yecH_BNaZW3vuU2jepfqUVeXrGzRKQo6CvAI4lqOFdfYjXtj7VAg64Q7-VtCO-VDovnXsQ2f_ytts3B3UI9j8v8nNDlrNSL7vwekgu-FNfsCDV8ktmNivES9ounsL1xbg5u6Amvyp4p8fQ_QJmp0GHaUy4m2BsU9dp-kpoO7ByKqbpbjHHiSvxyST5JZk1_PV9lzsmpm5pyXw28w-l6lVrdG9in82Kao4LciOspOMserCBguag0abrSE19vE5n_36ZStqUqR-IdOsGTq3BehJP7OmX21BcqSpRep4uo5Y61qZvFBcOXLyk0YGZ4x7ksvzFHzjpl6pi_Awv3-VWfC-w\"},{\"kty\":\"RSA\",\"e\":\"AQAB\",\"use\":\"sig\",\"kid\":\"0f2f5e31614bb1a78f91561eb12a43b9f5054643\",\"alg\":\"RS256\",\"n\":\"0l5rSPt7xnHUZxYTwjYtAAF3zLZOA2bNsEISDi-TCN4umFu_3BNzjujNTRNEOT0_Az2DhZvgbLtkIYwvMMI1fr5amNCRb5BupIgksu7gB08kwVDeu_OSeWKyEC3Rl5r_d_Uc4DfyLrw_xKbTstQaoO9C6JhmtQvuEAmukGm7t0w3NkQBGeWPiaUyCTJ5tTOfru6ZyRCHVedoj-9gkhIBitVqHRTC9jBCaQT0C2pMkpS_7xquLhd45JOD12Xuf20Vny_mJ02i8o0SWrZ5qyNu2DLaTBVMhcwN5OthUAtENXzbHppT8D7aHIaLPejy1UXqOWLA9byKiW9eUk1Gm7mKiQ\"}]}");
        Assert.assertEquals(securityConfigWithIdpPathChecker.getJSONWebKeys().toString(), "{\"keys\":[{\"kty\":\"RSA\",\"e\":\"AQAB\",\"use\":\"sig\",\"kid\":\"igb5CyFMAmFeei4MnXBo6mc93-7mEp7ogrIqWhMTcKc\",\"x5c\":[\"MIICQDCCAakCBEeNB0swDQYJKoZIhvcNAQEEBQAwZzELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFDASBgNVBAcTC1NhbnRhIENsYXJhMQwwCgYDVQQKEwNTdW4xEDAOBgNVBAsTB09wZW5TU08xDTALBgNVBAMTBHRlc3QwHhcNMDgwMTE1MTkxOTM5WhcNMTgwMTEyMTkxOTM5WjBnMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEUMBIGA1UEBxMLU2FudGEgQ2xhcmExDDAKBgNVBAoTA1N1bjEQMA4GA1UECxMHT3BlblNTTzENMAsGA1UEAxMEdGVzdDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEArSQc\\/U75GB2AtKhbGS5piiLkmJzqEsp64rDxbMJ+xDrye0EN\\/q1U5Of+RkDsaN\\/igkAvV1cuXEgTL6RlafFPcUX7QxDhZBhsYF9pbwtMzi4A4su9hnxIhURebGEmxKW9qJNYJs0Vo5+IgjxuEWnjnnVgHTs1+mq5QYTA7E6ZyL8CAwEAATANBgkqhkiG9w0BAQQFAAOBgQB3Pw\\/UQzPKTPTYi9upbFXlrAKMwtFf2OW4yvGWWvlcwcNSZJmTJ8ARvVYOMEVNbsT4OFcfu2\\/PeYoAdiDAcGy\\/F2Zuj8XJJpuQRSE6PtQqBuDEHjjmOQJ0rV\\/r8mO1ZCtHRhpZ5zYRjhRC9eCbjx9VrFax0JDC\\/FfwWigmrW0Y0Q==\"],\"n\":\"rSQc_U75GB2AtKhbGS5piiLkmJzqEsp64rDxbMJ-xDrye0EN_q1U5Of-RkDsaN_igkAvV1cuXEgTL6RlafFPcUX7QxDhZBhsYF9pbwtMzi4A4su9hnxIhURebGEmxKW9qJNYJs0Vo5-IgjxuEWnjnnVgHTs1-mq5QYTA7E6ZyL8\"}]}");
    }

    @Test
    public void testGetLoginUri() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getLoginUri());
        Assert.assertNotEquals(securityConfigWithIdpPathChecker.getLoginUri(), "https://accounts.google.com/o/oauth2/auth");
        Assert.assertEquals(securityConfigWithIdpPathChecker.getLoginUri(), "https://eid-exttest.difi.no/idporten-oidc-provider/authorize");
    }

    @Test
    public void testGetApiUri() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getApiUri());
        Assert.assertNotEquals(securityConfigWithIdpPathChecker.getApiUri(), "https://www.googleapis.com/oauth2/v3/token");
        Assert.assertEquals(securityConfigWithIdpPathChecker.getApiUri(), "https://eid-exttest.difi.no/idporten-oidc-provider/token");
    }

    @Test
    public void testGetIssuer() {
        Assert.assertNotNull(securityConfigWithIdpPathChecker.getIssuer());
        Assert.assertNotEquals(securityConfigWithIdpPathChecker.getIssuer(), "accounts.google.com");
        Assert.assertEquals(securityConfigWithIdpPathChecker.getIssuer(), "https://eid-exttest.difi.no/idporten-oidc-provider/");
    }
}
