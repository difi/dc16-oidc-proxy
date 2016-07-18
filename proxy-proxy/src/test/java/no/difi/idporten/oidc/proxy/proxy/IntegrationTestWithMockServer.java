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
import no.difi.idporten.oidc.proxy.util.RegexMatcher;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
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

        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8081));
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

        // Configuring what the mock server should respond to requests.
        configureFor(8081);
        stubFor(get(urlMatching("/o/oauth2/auth.*")) // logging in with google
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.FOUND.code())
                        .withHeader(HttpHeaderNames.LOCATION.toString(), BASEURL + "/google?code=aValidCode")));
        stubFor(post(urlMatching("/oauth2/v3/token.*")) // getting token with google
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
                        .withBody(apiResponseContent)));
        stubFor(get(urlPathMatching("/google.*")) // using google idp
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                        .withBody("du bruker google idp")));
        stubFor(get(urlPathMatching(specificPathWithGoogle + ".*")) // a path using google idp
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                        .withBody(contentOfASpecificPath)));
        stubFor(get(urlPathMatching("/idporten.*")) // using idporten
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                        .withBody("du bruker idporten idp")));
        stubFor(get(urlPathMatching(unsecuredPath + ".*")) // unsecured path
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                        .withBody(contentOfAnUnsecuredPath)));
    }

    @AfterMethod
    public void tearDown() {
    }

    /**
     * Simply tests that the mock server is running and can give a response
     *
     * @throws Exception
     */
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
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
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

    /**
     * When following a redirect after trying to access a secured path, we should get a Set-Cookie in the response,
     * because that is the cookie used to remember the user the next time she makes a request.
     *
     * @throws Exception
     */
    @Test
    public void testFollowRedirectHasSetCookie() throws Exception {
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        verify((getRequestedFor(urlMatching("/google"))));

        logger.debug(response.toString());

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        Assert.assertTrue(response.containsHeader(HttpHeaderNames.SET_COOKIE.toString()));
        Header setCookieHeader = response.getFirstHeader(HttpHeaderNames.SET_COOKIE.toString());
        String expectedCookieName = cookieName;
        assertThat("Cookie value should match a hex string with dashes",
                setCookieHeader.getValue(), RegexMatcher.matchesRegex(expectedCookieName + "=[0-9a-f\\-]+"));
    }

    /**
     * Our server should insert a special header containing the sensitive information asked for after successfully
     * authorizing it, before we forward the request to the secured service which is mocked here.
     *
     * @throws Exception
     */
    @Test
    public void testFollowRedirectHasDifiHeader() throws Exception {
        String expectedEmailInResponse = "vikfand@gmail.com";
        String expectedSubInResponse = "108182803704140665355";
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        verify(getRequestedFor(urlEqualTo("/google"))
                .withHeader(RequestInterceptor.HEADERNAME + "email", equalTo(expectedEmailInResponse))
                .withHeader(RequestInterceptor.HEADERNAME + "email_verified", equalTo("true"))
                .withHeader(RequestInterceptor.HEADERNAME + "sub", equalTo(expectedSubInResponse))
        );

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

    }

    /**
     * After the client is redirected to an authorization service like Google when requesting a secured resource,
     * then redirected back through our server, we should be able to retrieve the original path so that the client
     * ends up there.
     *
     * @throws Exception
     */
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

        response = httpClient.execute(getRequest);

        verify(getRequestedFor(urlPathEqualTo(specificPathWithGoogle)));

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        assertThat("The content of the response should be what the mock server serves for the specific path",
                responseContent, is(contentOfASpecificPath));
    }

    /**
     * Tests that the request has the Difi headers when requesting a secured resource with a valid cookie that we
     * just generated from logging in.
     *
     * @throws Exception
     */
    @Test
    public void testSecuredConfiguredWithGeneratedValidCookie() throws Exception {
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        verify((getRequestedFor(urlMatching("/google"))));

        logger.debug(response.toString());

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        Map<String, String> headerMap = getHeadersAsMap(response.getAllHeaders());

        Assert.assertTrue(response.containsHeader(HttpHeaderNames.SET_COOKIE.toString()));


        httpClient = HttpClientBuilder.create().disableRedirectHandling().build();
        String securedPathToUse = "/google/some/secured/path";
        getRequest = new HttpGet(BASEURL + securedPathToUse);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);
        getRequest.setHeader(HttpHeaderNames.COOKIE.toString(), headerMap.get(HttpHeaderNames.SET_COOKIE.toString()));

        response = httpClient.execute(getRequest);

        String expectedEmailInResponse = "vikfand@gmail.com";
        String expectedSubInResponse = "108182803704140665355";

        verify(1, getRequestedFor(urlEqualTo(securedPathToUse))
                .withHeader(RequestInterceptor.HEADERNAME + "email", equalTo(expectedEmailInResponse))
                .withHeader(RequestInterceptor.HEADERNAME + "email_verified", equalTo("true"))
                .withHeader(RequestInterceptor.HEADERNAME + "sub", equalTo(expectedSubInResponse))
        );
        String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");

        assertThat("",
                responseContent, is("du bruker google idp"));
    }


    /**
     * Using reflection to change the urls of the GoogleIdentityProvider to use the mock server url instead.
     *
     * @throws Exception
     */
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
