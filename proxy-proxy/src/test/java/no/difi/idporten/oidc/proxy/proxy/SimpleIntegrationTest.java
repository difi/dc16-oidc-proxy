package no.difi.idporten.oidc.proxy.proxy;


import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.handler.codec.http.HttpHeaderNames;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.storage.StorageModule;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.*;
import java.util.stream.Collectors;


public class SimpleIntegrationTest {

    private static Logger logger = LoggerFactory.getLogger(SimpleIntegrationTest.class);

    private static HttpClient httpClient;

    private Thread thread;

    private CookieStorage cookieStorage;

    private final String BASEURL = "http://localhost:8080";

    private String host = "localhost:8080";

    private String remoteHostName = "www.ntnu.no";

    private String cookieName = "PROXYCOOKIE";

    private String redirectCookieName = "redirectCookie";

    private String pid = "08023549930";

    private String tokenType = "JWTToken";

    private String aud = "dificamp";

    private int security;

    private Map<String, String> idPortenUserData;

    private ProxyCookie storedIdportenCookie;

    private static Map<String, String> getHeadersAsMap(Header[] headers) {
        return Arrays.stream(headers)
                .collect(Collectors.toMap(Header::getName, Header::getValue));
    }

    @BeforeClass
    public void beforeClass() throws Exception {
        Injector injector = Guice.createInjector(new ConfigModule(), new ProxyModule(), new StorageModule());

        cookieStorage = injector.getInstance(CookieStorage.class);

        thread = new Thread(injector.getInstance(NettyHttpListener.class));

        thread.start();

        Thread.sleep(1_000);
    }


    @AfterClass
    public void afterClass() {
        thread.interrupt();
    }

    @BeforeMethod
    public void setUp() {
        // A HttpClient that does not automatically follow redirects as the default one does
        httpClient = HttpClientBuilder.create().disableRedirectHandling().build();
    }

    @Test
    public void testUnsecuredConfigured() throws Exception {
        logger.info("With an unsecured path for a configured host, the server should work as a normal proxy and " +
                "hopefully return a 200 OK success response if the remote server is up.");
        HttpGet getRequest = new HttpGet(BASEURL);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), remoteHostName);

        HttpResponse response = httpClient.execute(getRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Test
    public void testUnsecuredConfiguredWithPath() throws Exception {
        logger.info("With an unsecured path for a configured host, the server should work as a normal proxy and " +
                "hopefully return a 200 OK success response if the remote server is up. Must also work if the path " +
                "is not the default path");
        String url = BASEURL + "/robots.txt";

        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), remoteHostName);


        HttpResponse response = httpClient.execute(getRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Test
    public void testSecuredConfiguredGoogle() throws Exception {
        logger.info("With a secured path on a configured host, the server should respond with a response " +
                "that redirects the client to a remote login server like Idporten or Google login.");
        String url = BASEURL + "/google";
        String expectedRedirectUrlFragment = "accounts.google.com";
        HttpGet getRequest = new HttpGet(url);

        HttpResponse response = httpClient.execute(getRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY);
        Assert.assertTrue(response.containsHeader(HttpHeaderNames.LOCATION.toString()));

        Map<String, String> headerMap = getHeadersAsMap(response.getAllHeaders());

        Assert.assertTrue(headerMap.get(HttpHeaderNames.LOCATION.toString()).contains(expectedRedirectUrlFragment));
    }

    @Test
    public void testSecuredConfiguredIdporten() throws Exception {
        logger.info("With a secured path on a configured host, the server should respond with a response " +
                "that redirects the client to a remote login server like Idporten or Google login.");
        String url = BASEURL + "/idporten";
        String expectedRedirectUrlFragment = "difi";
        HttpGet getRequest = new HttpGet(url);

        HttpResponse response = httpClient.execute(getRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY);
        Assert.assertTrue(response.containsHeader(HttpHeaderNames.LOCATION.toString()));

        Map<String, String> headerMap = getHeadersAsMap(response.getAllHeaders());

        Assert.assertTrue(headerMap.get(HttpHeaderNames.LOCATION.toString()).contains(expectedRedirectUrlFragment));
    }

    @Test
    public void testUnconfiguredHost() throws Exception {
        logger.info("With an unconfigured host, the server should respond with an error response telling the " +
                "client that the host is not configured.");
        HttpGet getRequest = new HttpGet(BASEURL);
        String host = "not.configured.url";
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), host);

        HttpResponse response = httpClient.execute(getRequest);

        Map<String, String> headerMap = getHeadersAsMap(response.getAllHeaders());

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST);
        Assert.assertTrue(headerMap.get(HttpHeaderNames.CONTENT_TYPE.toString()).contains(ResponseGenerator.TEXT_HTML));
    }

    @Test(enabled = false) // must have better configuration for this test
    public void testIdportenWithValidCookie() throws Exception {
        logger.info("With a valid cookie on a secured path on a configured host, the server should work as a " +
                "normal proxy while inserting the correct headers into the request before it reaches " +
                "the intended resource like www.nav.no");
        String url = BASEURL + "/idporten";
        String cookieUuid = storedIdportenCookie.getUuid();
        HttpGet getRequest = new HttpGet(url);

        getRequest.setHeader(HttpHeaderNames.HOST.toString(), remoteHostName);
        getRequest.setHeader(HttpHeaderNames.COOKIE.toString(), String.format("%s=%s", cookieName, cookieUuid));

        HttpResponse response = httpClient.execute(getRequest);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);

        String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");

        Assert.assertTrue(responseContent.contains(pid));

        Assert.assertTrue(response.containsHeader(RequestInterceptor.HEADERNAME));
        Header setCookieHeader = response.getFirstHeader(RequestInterceptor.HEADERNAME);
        Assert.assertTrue(setCookieHeader.getValue().contains(pid));
    }

    // Need to mock some of the IdentityProvider / External server / InboundHandler to make this work
    @Test(enabled = false)
    public void testValidCookieWithGoogle() throws Exception {
        logger.info("With a valid cookie on a secured path on a configured host, the server should work as a " +
                "normal proxy while inserting the correct headers into the request before it reaches " +
                "the intended resource like www.nav.no");
        String url = BASEURL + "/google";
        String cookieUuid = "uuidForValidCookie";
        HttpGet getRequest = new HttpGet(url);

        getRequest.setHeader(HttpHeaderNames.COOKIE.toString(), String.format("%s:%s", cookieName, cookieUuid));

        HttpResponse response = httpClient.execute(getRequest);
        Assert.assertNotEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST);
        Assert.assertNotEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        //Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Test
    public void testFirstRedirectResponseHasCookieForSavingPath() throws Exception {
        logger.info("With a secured path on a configured host, the server should respond with a redirect " +
                "response that has a Set-Cookie header in order to allow the server to remember the " +
                "original path the client requested after she has been redirected.");
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);


        HttpResponse response = httpClient.execute(getRequest);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY);

        Map<String, String> headerMap = getHeadersAsMap(response.getAllHeaders());

        Assert.assertTrue(headerMap.containsKey(HttpHeaderNames.SET_COOKIE.toString()));
        logger.debug(headerMap.get(HttpHeaderNames.SET_COOKIE.toString()));
        Assert.assertTrue(headerMap.get(HttpHeaderNames.SET_COOKIE.toString()).contains(redirectCookieName));
    }


    private ProxyCookie createValidGoogleCookie() {
        idPortenUserData = new HashMap<>();
        idPortenUserData.put("pid", pid);
        idPortenUserData.put("tokenType", tokenType);
        idPortenUserData.put("aud", aud);
        // Using hardcoded touchPeriod and maxExpiry value, as this isn't tested here
        storedIdportenCookie = cookieStorage.generateCookieInDb(cookieName, remoteHostName, "/google", security, 20, 120, idPortenUserData);
        return storedIdportenCookie;
    }

}
