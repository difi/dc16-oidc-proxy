package no.difi.idporten.oidc.proxy.proxy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.idp.GoogleIdentityProvider;
import no.difi.idporten.oidc.proxy.proxy.util.RegexMatcher;
import no.difi.idporten.oidc.proxy.idp.IdportenIdentityProvider;
import no.difi.idporten.oidc.proxy.storage.StorageModule;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


public class IntegrationTestWithMockServer {

    private static final String BASEURL = "http://localhost:8080";

    private static Logger logger = LoggerFactory.getLogger(IntegrationTestWithMockServer.class);

    private static HttpClient httpClient;

    private static HttpClient notFollowHttpClient;

    private static String specificPathWithGoogle = "/google/a/specific/path";

    private static String unsecuredPath = "/unsecured";

    private static String contentOfASpecificPath = "content of a specific path";

    private static String contentOfAnUnsecuredPath = "content of an unsecured path";

    private static String mockServerHostName = "www.mockhost.com";

    private static String mockServerAddress = "http://localhost:8081";

    private static String cookieName = "PROXYCOOKIE";

    private static String googleApiPath = "/oauth2/v3/token";

    private static String googleLoginPath = "/o/oauth2/auth";

    private static String idportenApiPath = "/idporten-oidc-provider/token";

    private static String idportenLoginPath = "/idporten-oidc-provider/authorize";

    private static String originalGoogleApiUrl;

    private static String originalGoogleLoginUrl;

    private static String originalIdportenLoginUrl;

    private static String originalIdportenApiUrl;

    private Thread thread;

    private WireMockServer wireMockServer;

    private static Map<String, String> getHeadersAsMap(Header[] headers) {
        return Arrays.stream(headers)
                .collect(Collectors.toMap(Header::getName, Header::getValue));
    }

    @BeforeClass
    public void beforeClass() throws Exception {
        Injector injector = Guice.createInjector(new ConfigModule(), new ProxyModule(), new StorageModule());

        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8081));
        wireMockServer.start();

        thread = new Thread(injector.getInstance(NettyHttpListener.class));

        thread.start();

        originalGoogleLoginUrl = getPrivateField(GoogleIdentityProvider.class.getDeclaredField("LOGINURL"));
        originalGoogleApiUrl = getPrivateField(GoogleIdentityProvider.class.getDeclaredField("APIURL"));
        originalIdportenLoginUrl = getPrivateField(IdportenIdentityProvider.class.getDeclaredField("LOGINURL"));
        originalIdportenApiUrl = getPrivateField(IdportenIdentityProvider.class.getDeclaredField("APIURL"));

        Thread.sleep(1_000);
    }


    @AfterClass
    public void afterClass() throws Exception {
        resetGoogleIdpUrls();
        resetIdportenIdpUrls();
        thread.interrupt();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        httpClient = HttpClientBuilder.create().build(); // Http client that automatically follows redirects
        notFollowHttpClient = HttpClientBuilder.create().disableRedirectHandling().build(); // Http client that automatically follows redirects
        modifyGoogleIdpUrls();
        modifyIdportenIdpUrls();
        String googleApiResponseContent = "{'access_token': 'ya29.Ci8dAzAibGLSpSo9h69_eve7JOskC49kmzhqg7E1fwZoSr6XA2B0y9V7ZGbt_FydMA','token_type': 'Bearer','expires_in': 3600,'refresh_token': '1/ZO_GEfsXflVCyiQUIIExKRSfCEnFsrTzwNsvcVQ56iI','id_token': 'eyJhbGciOiJSUzI1NiIsImtpZCI6IjE1MDgzMjdmODUzZGU4OTNlZWFhODZjMjAxNTI2ODk1ZDFlOTUxNDMifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXRfaGFzaCI6Ii16dzZmZlNXclo0LUVidnl0c0tkU1EiLCJhdWQiOiIxMDYzOTEwMjI0ODc3LWRocWQzNmMwOXNpdGY5YWxxM2piMHJmc2ZtZWJlMzVvLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTA4MTgyODAzNzA0MTQwNjY1MzU1IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF6cCI6IjEwNjM5MTAyMjQ4NzctZGhxZDM2YzA5c2l0ZjlhbHEzamIwcmZzZm1lYmUzNW8uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJlbWFpbCI6InZpa2ZhbmRAZ21haWwuY29tIiwiaWF0IjoxNDY4MjI3ODAzLCJleHAiOjE0NjgyMzE0MDN9.BVxm1dJWX41H2FX8P6fnP2WjJyLP8T9LKVppTqRF2cclKPEj9jDafip55rdYEuxI0p_Hx0UKNLnLp8QWOrhLWLuuXhWzmcxYW-bAGk3AarokTbLcSNJagGXFILPjMDDJ-qaMBLJhSITPy3-VlXkBn98fznYljXIEBZ4hd0OD9c93pkJ_DKF2FZ2WeFZlolrv1LK9xZkw43QtCS1mVqdyLG8KJHqQh2VGdjCyF0y1te2E23A7yPq9zmiS_67YX9T_WhiZ24CqjWtlul9dpUuTRlhMoP9FovUdjJpg7ry9zQRQsIpqt5ijTVAUJ9xuepsH6ZrSOzHqYzBLBzEoTYw0sg'}";
        String idportenApiResponseContent = "{\n" +
                "  \"access_token\" : \"f6ce8020-7c2c-4cf0-992d-9d1ba7aa8ff2\",\n" +
                "  \"id_token\" : \"eyJqa3UiOiJodHRwczpcL1wvZWlkLWV4dHRlc3QuZGlmaS5ub1wvaWRwb3J0ZW4tb2lkYy1wcm92aWRlclwvandrIiwia2lkIjoiaWdiNUN5Rk1BbUZlZWk0TW5YQm82bWM5My03bUVwN29ncklxV2hNVGNLYyIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiIyTzMxSnA5RTRNdnNJNGRHaTU4YkZaTGY2dHB1IiwiYXVkIjoidGVzdF9ycF9laWRfZXh0dGVzdF9kaWZpIiwiYWNyIjoiTGV2ZWwzIiwiYW1yIjoiTWluaWQtUElOIiwiaXNzIjoiaHR0cHM6XC9cL2VpZC1leHR0ZXN0LmRpZmkubm9cL2lkcG9ydGVuLW9pZGMtcHJvdmlkZXJcLyIsInBpZCI6IjA4MDIzNTQ5OTMwIiwiZXhwIjoxNDY4OTE0MzAzLCJsb2NhbGUiOiJuYiIsImlhdCI6MTQ2ODkxMDcwM30.Mk2NBA-xCM1qtAHKSEbSFEpSFAhwdK5tWtQguxTjM4aHxi4qEhxJ6z1056vQIbnH3FWnXNZZlzhyDd4Tg5Fb5XQJNstwrTlkGQAEgul0YbTWgKLWoTXiId-kKP2YoXhn-DexAuleAmxVS6XtXoDqjd0HXGvx-JD_ufOzJ_YIr2U\",\n" +
                "  \"token_type\" : \"Bearer\",\n" +
                "  \"expires_in\" : 3599,\n" +
                "  \"refresh_token\" : \"90fc8152-3d1b-40dc-8eae-0114db775bcb\",\n" +
                "  \"scope\" : \"openid profile\"\n" +
                "}";

        // Configuring what the mock server should respond to requests.
        configureFor(8081);
        stubFor(get(urlPathMatching("/idporten.*")) // using idporten idp
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                        .withBody("du bruker idporten idp")));
        stubFor(get(urlPathMatching("/google.*")) // using google idp
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                        .withBody("du bruker google idp")));
        stubFor(get(urlPathEqualTo(googleLoginPath)) // logging in with google
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.FOUND.code())
                        .withHeader(HttpHeaderNames.LOCATION.toString(), BASEURL + "/google?code=aValidCode")));
        stubFor(post(urlPathEqualTo(googleApiPath)) // getting token with google
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
                        .withBody(googleApiResponseContent)));
        stubFor(get(urlPathMatching(idportenLoginPath)) // logging in with idporten
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.FOUND.code())
                        .withHeader(HttpHeaderNames.LOCATION.toString(), BASEURL + "/idporten?code=aValidCode")));
        stubFor(post(urlPathEqualTo(idportenApiPath)) // getting token with idporten
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
                        .withBody(idportenApiResponseContent)));
        stubFor(get(urlPathMatching(specificPathWithGoogle + ".*")) // a path using google idp
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                        .withBody(contentOfASpecificPath)));
        stubFor(get(urlPathMatching(unsecuredPath + ".*")) // unsecured path
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.OK.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                        .withBody(contentOfAnUnsecuredPath)));
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
        MatcherAssert.assertThat(response.getStatusLine().getStatusCode(), Matchers.is(HttpStatus.SC_OK));
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
        logger.info("After logging in when requesting a secured resource, " +
                "the response should have a 'Set-Cookie header'");
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        verify((getRequestedFor(urlMatching("/google"))));

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        Assert.assertTrue(response.containsHeader(HttpHeaderNames.SET_COOKIE.toString()));
        Header setCookieHeader = response.getFirstHeader(HttpHeaderNames.SET_COOKIE.toString());
        String expectedCookieName = cookieName;
        MatcherAssert.assertThat("Cookie value should match a hex string with dashes",
                setCookieHeader.getValue(), RegexMatcher.matchesRegex(expectedCookieName + "=[0-9a-f\\-]+"));
    }

    /**
     * Our server should insert a special header containing the sensitive information asked for after successfully
     * authorizing it, before we forward the request to the secured service which is mocked here.
     *
     * @throws Exception
     */
    @Test
    public void testGoogleFollowRedirectHasDifiHeader() throws Exception {
        String expectedEmailInRequest = "vikfand@gmail.com";
        String expectedSubInRequest = "108182803704140665355";
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        verify(getRequestedFor(urlEqualTo("/google"))
                .withHeader(RequestInterceptor.HEADERNAME + "email", equalTo(expectedEmailInRequest))
                .withHeader(RequestInterceptor.HEADERNAME + "email_verified", equalTo("true"))
                .withHeader(RequestInterceptor.HEADERNAME + "sub", equalTo(expectedSubInRequest))
        );

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

    }

    @Test
    public void testIdportenFollowRedirectHasDifiHeader() throws Exception {
        String expectedPidInRequest = "08023549930";
        String url = BASEURL + "/idporten";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        verify(getRequestedFor(urlEqualTo("/idporten"))
                .withHeader(RequestInterceptor.HEADERNAME + "pid", equalTo(expectedPidInRequest))
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
        String url = BASEURL + specificPathWithGoogle;
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = notFollowHttpClient.execute(getRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY);
        Map<String, String> headerMap = getHeadersAsMap(response.getAllHeaders());
        String cookiesString = headerMap.get(HttpHeaderNames.SET_COOKIE.toString());

        getRequest = new HttpGet(BASEURL + "/google?code=aValidCode");
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);
        getRequest.setHeader(HttpHeaderNames.COOKIE.toString(), cookiesString);

        response = notFollowHttpClient.execute(getRequest);

        headerMap = getHeadersAsMap(response.getAllHeaders());
        cookiesString = headerMap.get(HttpHeaderNames.SET_COOKIE.toString());
        getRequest = new HttpGet(BASEURL + headerMap.get(HttpHeaderNames.LOCATION.toString()));
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);
        getRequest.setHeader(HttpHeaderNames.COOKIE.toString(), cookiesString);

        httpClient = HttpClientBuilder.create().build();
        response = httpClient.execute(getRequest);

        verify(getRequestedFor(urlPathEqualTo(specificPathWithGoogle)));

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        MatcherAssert.assertThat("The content of the response should be what the mock server serves for the specific path",
                responseContent, Matchers.is(contentOfASpecificPath));
    }

    /**
     * Tests that the request has the Difi headers when requesting a secured resource with a valid cookie that we
     * just generated from logging in.
     *
     * @throws Exception
     */
    @Test
    public void testSecuredConfiguredWithGeneratedValidCookie() throws Exception {
        String securedPathToUse = "/google/some/secured/path";
        HttpGet getRequest = getRequestWithValidGoogleCookie(securedPathToUse);

        HttpResponse response = notFollowHttpClient.execute(getRequest);

        String expectedEmailInResponse = "vikfand@gmail.com";
        String expectedSubInResponse = "108182803704140665355";

        verify(1, getRequestedFor(urlEqualTo(securedPathToUse))
                .withHeader(RequestInterceptor.HEADERNAME + "email", equalTo(expectedEmailInResponse))
                .withHeader(RequestInterceptor.HEADERNAME + "email_verified", equalTo("true"))
                .withHeader(RequestInterceptor.HEADERNAME + "sub", equalTo(expectedSubInResponse))
        );
        String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");

        MatcherAssert.assertThat("",
                responseContent, Matchers.is("du bruker google idp"));
    }

    @Test
    public void testDifiHeadersNotInRequestToTotallyUnsecuredPathWithValidCookie() throws Exception {
        String totallyUnsecuredPathToUse = "/something/totally/unsecured/like/a/logo/or/something.svg";
        HttpGet getRequest = getRequestWithValidGoogleCookie(totallyUnsecuredPathToUse);

        notFollowHttpClient.execute(getRequest);

        verify(1, getRequestedFor(urlPathEqualTo(totallyUnsecuredPathToUse)));
        verify(0, getRequestedFor(urlPathEqualTo(totallyUnsecuredPathToUse))
                .withHeader(RequestInterceptor.HEADERNAME + "email", matching(".*"))
                .withHeader(RequestInterceptor.HEADERNAME + "email_verified", equalTo("true"))
                .withHeader(RequestInterceptor.HEADERNAME + "sub", matching(".*"))
        );
    }

    @Test
    public void testRequestingResourceWithHigherSecurityThanCurrent() throws Exception {
        HttpGet getRequest = getRequestWithValidGoogleCookie("/idporten");

        HttpResponse redirectResponse = notFollowHttpClient.execute(getRequest);

        Assert.assertEquals(redirectResponse.getStatusLine().getStatusCode(), HttpResponseStatus.FOUND.code());
        /*
        HttpResponse finalResponse = httpClient.execute(getRequest);

        verify(1, getRequestedFor(urlPathEqualTo(idportenLoginPath)));
        verify(1, postRequestedFor(urlPathEqualTo(idportenApiPath)));
        Assert.assertEquals(finalResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
        String responseContent = IOUtils.toString(finalResponse.getEntity().getContent(), "UTF-8");
        Assert.assertEquals(responseContent, "du bruker idporten idp");
        */
    }

    private static HttpGet getRequestWithValidGoogleCookie(String path) throws Exception {
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);
        HttpResponse response = httpClient.execute(getRequest);

        Map<String, String> headerMap = getHeadersAsMap(response.getAllHeaders());

        MatcherAssert.assertThat("Should have a valid cookie at this point",
                headerMap.keySet().contains(HttpHeaderNames.SET_COOKIE.toString()));

        String acquiredCookie = headerMap.get(HttpHeaderNames.SET_COOKIE.toString());

        getRequest = new HttpGet(BASEURL + path);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);
        getRequest.setHeader("Cookie", acquiredCookie);

        return getRequest;
    }


    /**
     * Using reflection to change the urls of the GoogleIdentityProvider to use the mock server url instead.
     *
     * @throws Exception
     */
    private static void modifyGoogleIdpUrls() throws Exception {
        setPrivateField(GoogleIdentityProvider.class.getDeclaredField("APIURL"), mockServerAddress + googleApiPath);
        setPrivateField(GoogleIdentityProvider.class.getDeclaredField("LOGINURL"), mockServerAddress + googleLoginPath);
    }

    private static void modifyIdportenIdpUrls() throws Exception {
        setPrivateField(IdportenIdentityProvider.class.getDeclaredField("APIURL"), mockServerAddress + idportenApiPath);
        setPrivateField(IdportenIdentityProvider.class.getDeclaredField("LOGINURL"), mockServerAddress + idportenLoginPath);
    }

    private static void resetGoogleIdpUrls() throws Exception {
        setPrivateField(GoogleIdentityProvider.class.getDeclaredField("APIURL"), originalGoogleApiUrl);
        setPrivateField(GoogleIdentityProvider.class.getDeclaredField("LOGINURL"), originalGoogleLoginUrl);
    }

    private static void resetIdportenIdpUrls() throws Exception {
        setPrivateField(IdportenIdentityProvider.class.getDeclaredField("APIURL"), originalIdportenApiUrl);
        setPrivateField(IdportenIdentityProvider.class.getDeclaredField("LOGINURL"), originalIdportenLoginUrl);
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
