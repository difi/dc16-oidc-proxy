package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import org.apache.http.Header;
import io.netty.handler.codec.http.*;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class IntegrationTest {
    private static Logger logger = LoggerFactory.getLogger(SimpleTest.class);
    private static HttpClient httpClient;
    private Thread thread;
    private final String BASEURL = "http://localhost:8080";

    private static Map<String, String> getHeadersAsMap(Header[] headers) {
        return Arrays.stream(headers)
                .collect(Collectors.toMap(Header::getName, Header::getValue));
    }

    @BeforeClass
    public void beforeClass() throws Exception {
        Injector injector = Guice.createInjector(new ConfigModule(), new ProxyModule());

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
        HttpGet getRequest = new HttpGet(BASEURL);

        HttpResponse response = httpClient.execute(getRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Test
    public void testUnsecuredConfiguredWithPath() throws Exception {
        String url = BASEURL + "/robots.txt";
        String configuredHostName = "www.ntnu.no";

        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), configuredHostName);


        HttpResponse response = httpClient.execute(getRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Test
    public void testSecuredConfiguredGoogle() throws Exception {
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
        HttpGet getRequest = new HttpGet(BASEURL);
        String host = "not.configured.url";
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), host);

        HttpResponse response = httpClient.execute(getRequest);

        Map<String, String> headerMap = getHeadersAsMap(response.getAllHeaders());

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST);
        Assert.assertTrue(headerMap.get(HttpHeaderNames.CONTENT_TYPE.toString()).contains(ResponseGenerator.TEXT_HTML));
    }

    // Need to mock some of the IdentityProvider / External server / InboundHandler to make this work
    @Test
    public void testValidCookieWithIdporten() throws Exception {
        String url = BASEURL + "/idporten";
        String cookieName = "TESTCOOKIE";
        String cookieUuid = "uuidForValidCookie";
        HttpGet getRequest = new HttpGet(url);

        getRequest.setHeader(HttpHeaderNames.COOKIE.toString(), String.format("%s:%s", cookieName, cookieUuid));

        HttpResponse response = httpClient.execute(getRequest);
        Assert.assertNotEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST);
        Assert.assertNotEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        //Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    // Need to mock some of the IdentityProvider / External server / InboundHandler to make this work
    @Test
    public void testValidCookieWithGoogle() throws Exception {
        String url = BASEURL + "/google";
        String cookieName = "TESTCOOKIE";
        String cookieUuid = "uuidForValidCookie";
        HttpGet getRequest = new HttpGet(url);

        getRequest.setHeader(HttpHeaderNames.COOKIE.toString(), String.format("%s:%s", cookieName, cookieUuid));

        HttpResponse response = httpClient.execute(getRequest);
        Assert.assertNotEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST);
        Assert.assertNotEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        //Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }
}
