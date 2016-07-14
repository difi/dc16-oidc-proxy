package no.difi.idporten.oidc.proxy.proxy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.idp.GoogleIdentityProvider;
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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


public class IntegrationTestWithMockServer {
    private static Logger logger = LoggerFactory.getLogger(IntegrationTestWithMockServer.class);
    private static HttpClient httpClient;
    private Thread thread;
    private final String BASEURL = "http://localhost:8080";
    private String specificPathWithGoogle = "/google/a/specific/path";
    private String unsecuredPath = "/unsecured";
    private String contentOfASpecificPath = "content of a specific path";
    private String contentOfAnUnsecuredPath = "content of an unsecured path";
    private String mockServerHostName = "www.mockhost.com";
    private String cookieName = "PROXYCOOKIE";

    private WireMockServer wireMockServer;

    private static Map<String, String> getHeadersAsMap(Header[] headers) {
        return Arrays.stream(headers)
                .collect(Collectors.toMap(Header::getName, Header::getValue));
    }

    private static String originalGoogleApiUrl;
    private static String originalGoogleLoginUrl;


    @BeforeClass
    public void beforeClass() throws Exception {
        Injector injector = Guice.createInjector(new ConfigModule(), new ProxyModule(), new StorageModule());

        wireMockServer =  new WireMockServer(WireMockConfiguration.options().port(8081));
        wireMockServer.start();

        thread = new Thread(injector.getInstance(NettyHttpListener.class));

        thread.start();

        originalGoogleLoginUrl = getPrivateField(GoogleIdentityProvider.class.getDeclaredField("LOGINURL"));
        originalGoogleApiUrl = getPrivateField(GoogleIdentityProvider.class.getDeclaredField("APIURL"));

        Thread.sleep(1_000);
    }


    @AfterClass
    public void afterClass() throws Exception {
        resetGoogleIdpUrls();
        thread.interrupt();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        httpClient = HttpClientBuilder.create().build(); // Http client that automatically follows redirects
        modifyGoogleIdpUrls();
        String apiResponseContent = "{'access_token': 'ya29.Ci8dAzAibGLSpSo9h69_eve7JOskC49kmzhqg7E1fwZoSr6XA2B0y9V7ZGbt_FydMA','token_type': 'Bearer','expires_in': 3600,'refresh_token': '1/ZO_GEfsXflVCyiQUIIExKRSfCEnFsrTzwNsvcVQ56iI','id_token': 'eyJhbGciOiJSUzI1NiIsImtpZCI6IjE1MDgzMjdmODUzZGU4OTNlZWFhODZjMjAxNTI2ODk1ZDFlOTUxNDMifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXRfaGFzaCI6Ii16dzZmZlNXclo0LUVidnl0c0tkU1EiLCJhdWQiOiIxMDYzOTEwMjI0ODc3LWRocWQzNmMwOXNpdGY5YWxxM2piMHJmc2ZtZWJlMzVvLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTA4MTgyODAzNzA0MTQwNjY1MzU1IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF6cCI6IjEwNjM5MTAyMjQ4NzctZGhxZDM2YzA5c2l0ZjlhbHEzamIwcmZzZm1lYmUzNW8uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJlbWFpbCI6InZpa2ZhbmRAZ21haWwuY29tIiwiaWF0IjoxNDY4MjI3ODAzLCJleHAiOjE0NjgyMzE0MDN9.BVxm1dJWX41H2FX8P6fnP2WjJyLP8T9LKVppTqRF2cclKPEj9jDafip55rdYEuxI0p_Hx0UKNLnLp8QWOrhLWLuuXhWzmcxYW-bAGk3AarokTbLcSNJagGXFILPjMDDJ-qaMBLJhSITPy3-VlXkBn98fznYljXIEBZ4hd0OD9c93pkJ_DKF2FZ2WeFZlolrv1LK9xZkw43QtCS1mVqdyLG8KJHqQh2VGdjCyF0y1te2E23A7yPq9zmiS_67YX9T_WhiZ24CqjWtlul9dpUuTRlhMoP9FovUdjJpg7ry9zQRQsIpqt5ijTVAUJ9xuepsH6ZrSOzHqYzBLBzEoTYw0sg'}";

        configureFor(8081);
        stubFor(get(urlMatching("/o/oauth2/auth.*"))
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.FOUND.code())
                        .withHeader(HttpHeaderNames.LOCATION.toString(), BASEURL + "/google?code=aValidCode")));
        stubFor(post(urlMatching("/oauth2/v3/token.*"))
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
                        .withBody(apiResponseContent)));
        stubFor(get(urlPathMatching(specificPathWithGoogle + ".*"))
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                        .withBody(contentOfASpecificPath)));
        stubFor(get(urlPathMatching("/google"))
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                        .withBody("du bruker google idp")));
        stubFor(get(urlPathMatching("/idporten.*"))
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                        .withBody("du bruker idporten idp")));
        stubFor(get(urlPathMatching(unsecuredPath + ".*"))
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                        .withBody(contentOfAnUnsecuredPath)));
    }

    @AfterMethod
    public void tearDown() {
    }

    @Test
    public void testWireMockWorks() throws Exception {
        configureFor(8081);
        stubFor(get(urlEqualTo("/hei"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Hello world!")));
        String url = "http://localhost:8081" + "/hei";
        HttpGet getRequest = new HttpGet(url);

        HttpResponse response = httpClient.execute(getRequest);
        logger.debug(response.toString());
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }


    @Test
    public void testConfiguredUnsecured() throws Exception {
        String url = BASEURL + unsecuredPath;
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
        String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        Assert.assertTrue(responseContent.contains(contentOfAnUnsecuredPath));
    }

    @Test(enabled = false)
    public void testFollowRedirectGivesToken() throws Exception {
        String expectedEmailInResponse = "vikfand@gmail.com";
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
        String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        Assert.assertTrue(responseContent.contains(expectedEmailInResponse));
    }

    @Test
    public void testFollowRedirectHasSetCookie() throws Exception {
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        verify((getRequestedFor(urlMatching("/google\\?code=.*"))));

        logger.debug(response.toString());

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        Assert.assertTrue(response.containsHeader(HttpHeaderNames.SET_COOKIE.toString()));
        Header setCookieHeader = response.getFirstHeader(HttpHeaderNames.SET_COOKIE.toString());
        String expectedCookieName = cookieName;
        Assert.assertTrue(setCookieHeader.getValue().matches(expectedCookieName + "=[0-9a-f]+"));
    }

    @Test
    public void testFollowRedirectHasDifiHeader() throws Exception {
        String expectedEmailInResponse = "vikfand@gmail.com";
        String expectedSubInResponse = "108182803704140665355";
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        verify(getRequestedFor(urlEqualTo("/google?code=aValidCode"))
                .withHeader(RequestInterceptor.HEADERNAME + "email", equalTo(expectedEmailInResponse))
                .withHeader(RequestInterceptor.HEADERNAME + "email_verified", equalTo("true"))
                .withHeader(RequestInterceptor.HEADERNAME + "sub", equalTo(expectedSubInResponse))
        );

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

    }

    @Test
    public void testFollowRedirectHasOriginalPath() throws Exception {
        httpClient = HttpClientBuilder.create().disableRedirectHandling().build();
        String url = BASEURL + specificPathWithGoogle;
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY);
        Map<String, String> headerMap = getHeadersAsMap(response.getAllHeaders());
        String cookiesString = headerMap.get(HttpHeaderNames.SET_COOKIE.toString());

        getRequest = new HttpGet(BASEURL + "/google?code=aValidCode");
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);
        getRequest.setHeader(HttpHeaderNames.COOKIE.toString(), cookiesString);
        getRequest.setHeader("TULL", "LSDFLKSD");

        response = httpClient.execute(getRequest);

        verify(getRequestedFor(urlPathMatching(specificPathWithGoogle + ".*")));

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        Assert.assertTrue(responseContent.contains(contentOfASpecificPath));
    }


    private static void modifyGoogleIdpUrls() throws Exception {
        String mockServerApiUrl = "http://localhost:8081/oauth2/v3/token";
        String mockServerLoginUrl = "http://localhost:8081/o/oauth2/auth";
        setPrivateField(GoogleIdentityProvider.class.getDeclaredField("APIURL"), mockServerApiUrl);
        setPrivateField(GoogleIdentityProvider.class.getDeclaredField("LOGINURL"), mockServerLoginUrl);
    }

    private static void resetGoogleIdpUrls() throws Exception {
        setPrivateField(GoogleIdentityProvider.class.getDeclaredField("APIURL"), originalGoogleApiUrl);
        setPrivateField(GoogleIdentityProvider.class.getDeclaredField("LOGINURL"), originalGoogleLoginUrl);
    }

    private static void setPrivateField(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        field.set(null, newValue);
    }

    private static String getPrivateField(Field field) throws Exception {
        field.setAccessible(true);
        return (String) field.get(null);
    }
}
