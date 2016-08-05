package no.difi.idporten.oidc.proxy.proxy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.proxy.util.RegexMatcher;
import no.difi.idporten.oidc.proxy.storage.StorageModule;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
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

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static no.difi.idporten.oidc.proxy.proxy.util.Utils.*;


public class IntegrationTestWithMockServer {

    private static final String BASEURL = "http://localhost:8080";

    private static Logger logger = LoggerFactory.getLogger(IntegrationTestWithMockServer.class);

    private static HttpClient httpClient;

    private static HttpClient notFollowHttpClient;

    private Thread thread;

    private WireMockServer wireMockServer;

    @BeforeClass
    public void beforeClass() throws Exception {
        Injector injector = Guice.createInjector(new ConfigModule(), new ProxyModule(), new StorageModule());

        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8081));
        wireMockServer.start();

        thread = new Thread(injector.getInstance(NettyHttpListener.class));
        thread.start();
        Thread.sleep(1_000);
    }


    @AfterClass
    public void afterClass() throws Exception {
        thread.interrupt();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        httpClient = HttpClientBuilder.create().build(); // Http client that automatically follows redirects
        notFollowHttpClient = HttpClientBuilder.create().disableRedirectHandling().build(); // Http client that automatically follows redirects

        // Configuring what the mock server should respond to requests.
        wireMockServer.resetRequests();
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
        stubFor(post(urlPathEqualTo(googleApiPath)).withRequestBody(matching(".*anInvalidCode.*")) // getting token but code is invalid
                .willReturn(aResponse()
                        .withStatus(HttpResponseStatus.BAD_REQUEST.code())
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
                        .withBody("{}")));
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
                "the response should have a 'Set-Cookie' header. This is tested by letting the httpClient " +
                "set the cookie if it's there and verify with WireMock.");
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        verify((getRequestedFor(urlPathMatching("/google"))));

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        httpClient.execute(getRequest);

        verify((getRequestedFor(urlPathEqualTo("/google")).withCookie(cookieName, matching("[0-9a-f\\-]+"))));
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * Our server should insert a special header containing the sensitive information asked for after successfully
     * authorizing it, before we forward the request to the secured service which is mocked here.
     *
     * @throws Exception
     */
    @Test
    public void testGoogleFollowRedirectHasDifiHeader() throws Exception {
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        verify(getRequestedFor(urlEqualTo("/google"))
                .withHeader(RequestInterceptor.HEADERNAME + "email", equalTo(emailInGoogleToken))
                .withHeader(RequestInterceptor.HEADERNAME + "email_verified", equalTo("true"))
                .withHeader(RequestInterceptor.HEADERNAME + "sub", equalTo(subInGoogleToken))
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
        String expectedEmailInResponse = "vikfand@gmail.com";
        String expectedSubInResponse = "108182803704140665355";

        String url = BASEURL + specificPathWithGoogle;
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = httpClient.execute(getRequest);

        verify(1, getRequestedFor(urlPathEqualTo(specificPathWithGoogle))
                .withHeader(RequestInterceptor.HEADERNAME + "email", equalTo(expectedEmailInResponse))
                .withHeader(RequestInterceptor.HEADERNAME + "email_verified", equalTo("true"))
                .withHeader(RequestInterceptor.HEADERNAME + "sub", equalTo(expectedSubInResponse))
        );

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        MatcherAssert.assertThat("The content of the response should be what the mock server serves for the specific path",
                responseContent, Matchers.is(contentOfASpecificPath));
    }

    /**
     * Tests that the 'Set-Cookie' header for a redirect cookie has '/' as its path, because that is what it needs
     * to be to actually be set by the browser after being redirected.
     *
     * @throws Exception
     */
    @Test
    public void testRedirectCookieHasRootAsPath() throws Exception {
        String expectedRedirectCookieName = "redirectCookie";
        String url = BASEURL + specificPathWithGoogle;
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = notFollowHttpClient.execute(getRequest);

        Map<String, String> headerMap = getHeadersAsMap(response.getAllHeaders());
        MatcherAssert.assertThat("Response should contain a 'Set-Cookie header'",
                headerMap.keySet(), Matchers.hasItem(HttpHeaderNames.SET_COOKIE.toString()));
        String setCookieHeader = headerMap.get(HttpHeaderNames.SET_COOKIE.toString());
        MatcherAssert.assertThat("The 'Set-Cookie' header should be for a redirect cookie",
                setCookieHeader, Matchers.containsString(expectedRedirectCookieName));
        MatcherAssert.assertThat("The path for the cookie should be equal to '/'",
                setCookieHeader, RegexMatcher.matchesRegex(".*[Pp]ath=/.*"));
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

        HttpResponse response = getGoogleLoggedInResponse(securedPathToUse, false);

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

        getGoogleLoggedInResponse(totallyUnsecuredPathToUse, false);

        verify(1, getRequestedFor(urlPathEqualTo(totallyUnsecuredPathToUse)));
        verify(0, getRequestedFor(urlPathEqualTo(totallyUnsecuredPathToUse))
                .withHeader(RequestInterceptor.HEADERNAME + "email", matching(".*"))
                .withHeader(RequestInterceptor.HEADERNAME + "email_verified", equalTo("true"))
                .withHeader(RequestInterceptor.HEADERNAME + "sub", matching(".*"))
        );
    }

    /**
     * Should be redirected to login again if requesting a resource with higher security than the current one.
     *
     * @throws Exception
     */
    @Test
    public void testRequestingResourceWithHigherSecurityThanCurrent() throws Exception {
        HttpResponse redirectResponse = getGoogleLoggedInResponse("/idporten", false);

        Assert.assertEquals(redirectResponse.getStatusLine().getStatusCode(), HttpResponseStatus.FOUND.code());

        HttpResponse finalResponse = getGoogleLoggedInResponse("/idporten", true);

        verify(1, getRequestedFor(urlPathEqualTo(idportenLoginPath)));
        verify(1, postRequestedFor(urlPathEqualTo(idportenApiPath)));
        Assert.assertEquals(finalResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
        String responseContent = IOUtils.toString(finalResponse.getEntity().getContent(), "UTF-8");
        Assert.assertEquals(responseContent, "du bruker idporten idp");
    }

    /**
     * Should insert Difi headers into request when requesting unsecured resource when logged in.
     *
     * @throws Exception
     */
    @Test
    public void testRequestingUnsecuredPathWithWhenLoggedInWithValidCookie() throws Exception {
        HttpResponse response = getGoogleLoggedInResponse("/unsecured", false);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpResponseStatus.OK.code());
        verify(1, getRequestedFor(urlPathEqualTo("/unsecured"))
                .withHeader(RequestInterceptor.HEADERNAME + "email", matching(".*"))
                .withHeader(RequestInterceptor.HEADERNAME + "email_verified", equalTo("true"))
                .withHeader(RequestInterceptor.HEADERNAME + "sub", matching(".*"))
        );
    }

    /**
     * Logging out should delete the cookie stored in our system and make the client log in again when requesting
     * something new later.
     *
     * @throws Exception
     */
    @Test
    public void testCanLogout() throws Exception {
        HttpGet getRequest = getRequestWithValidGoogleCookie(logoutPath);

        HttpResponse response = notFollowHttpClient.execute(getRequest);

        MatcherAssert.assertThat("Logging out should give a redirect response to a configured url",
                response.getStatusLine().getStatusCode(), Matchers.equalTo(HttpResponseStatus.FOUND.code()));

        getRequest.setURI(URI.create(BASEURL + "/google"));
        response = notFollowHttpClient.execute(getRequest);

        MatcherAssert.assertThat("After logging out, the cookie in the request should not be valid anymore, and the" +
                        "server should send a redirect to the login page for the IDP again.",
                response.getStatusLine().getStatusCode(), Matchers.equalTo(HttpResponseStatus.FOUND.code()));
        MatcherAssert.assertThat("Response should have a location header.",
                getHeadersAsMap(response.getAllHeaders()).keySet(), Matchers.hasItem(HttpHeaderNames.LOCATION.toString()));
        MatcherAssert.assertThat("Response should be redirected to the IDP login",
                response.getFirstHeader(HttpHeaderNames.LOCATION.toString()).getValue(), Matchers.startsWith(mockServerAddress + googleLoginPath));
    }

    /**
     * Logging out should delete (or make invalid) the cookie generated from logging in.
     * This is tested by requesting a secured resource after logged out, but still having the cookie generated from
     * successfully logging in in the request.
     *
     * @throws Exception
     */
    @Test
    public void testLogoutDeletesCookieFromDatabase() throws Exception {
        String path = "/google/hello/there";
        CookieStore cookieStore = new BasicCookieStore();
        logIn(BASEURL + path, cookieStore);
        verify(1, getRequestedFor(urlPathMatching(googleLoginPath)));

        Cookie apacheCookie = cookieStore.getCookies().stream()
                .filter(cookie -> cookie.getName().equals(cookieName)).findFirst().orElse(null);
        if (apacheCookie == null) {
            Assert.fail("Should have a cookie in browser when logged in.");
        }

        HttpGet getRequest = new HttpGet(BASEURL + logoutPath);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);
        HttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        httpClient.execute(getRequest);
        verify(1, getRequestedFor(urlPathMatching(googleLoginPath)));

        getRequest.setURI(new URI(BASEURL + path));
        httpClient.execute(getRequest);

        apacheCookie = cookieStore.getCookies().stream()
                .filter(cookie -> cookie.getName().equals(cookieName)).findFirst().orElse(null);
        if (apacheCookie == null) {
            Assert.fail("Request should have cookie, but it should be deleted on the server.");
        }

        verify(2, getRequestedFor(urlPathMatching(googleLoginPath)));
    }

    /**
     * Should be able to handle many requests without crashing.
     *
     * @throws Exception
     */
    @Test
    public void testLoginManyTimes() throws Exception {
        HttpGet getRequest = getRequestWithValidGoogleCookie("/path" + 0);
        for (int i = 1; i < 20; i++) {
            httpClient = HttpClientBuilder.create().build();
            getRequest = getRequestWithValidGoogleCookie("/path" + i);
        }
        httpClient.execute(getRequest);
    }

    /**
     * Having an invalid code in the url should not result in an error response, but simply redirect to a login for the
     * IDP needed on the requested path without the code parameter.
     * <p>
     * <p>
     * It has not been decided whether the user should be redirected to login
     * or if an error page should be presented. This test has therefore been disabled.
     *
     * @throws Exception
     */
    @Test(enabled = false)
    public void testRequestWithInvalidCodeShouldRedirectToLogin() throws Exception {
        HttpGet getRequest = new HttpGet(BASEURL + "/google" + invalidCodeUrlSuffix);

        HttpResponse response = notFollowHttpClient.execute(getRequest);

        MatcherAssert.assertThat("Should be a redirect to login",
                response.getStatusLine().getStatusCode(), Matchers.equalTo(HttpResponseStatus.FOUND.code()));
        MatcherAssert.assertThat("Should have a location header",
                getHeadersAsMap(response.getAllHeaders()).keySet(), Matchers.hasItem(HttpHeaderNames.LOCATION.toString()));
        MatcherAssert.assertThat("Should have a redirect url to IDP login, this time without code.",
                getHeadersAsMap(response.getAllHeaders()).get(HttpHeaderNames.LOCATION.toString()), Matchers.containsString(googleLoginPath));
    }

    @Test
    public void testAccessTokenInRequestWhenConfiguredTrue() throws Exception {
        HttpGet getRequest = new HttpGet(BASEURL + "/idporten");
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        httpClient.execute(getRequest);

        verify(1, getRequestedFor(urlPathEqualTo("/idporten"))
                .withHeader(RequestInterceptor.HEADERNAME + "access_token", matching(".*"))
        );
    }

    @Test
    public void testAccessTokenNotInRequestWhenConfiguredFalse() throws Exception {
        HttpGet getRequest = new HttpGet(BASEURL + "/google");
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        httpClient.execute(getRequest);

        verify(1, getRequestedFor(urlPathEqualTo("/google")));
        verify(0, getRequestedFor(urlPathMatching(".*"))
                .withHeader(RequestInterceptor.HEADERNAME + "access_token", matching(".*")));
    }

    /**
     * When logged in with two IDPs, Difi Proxy should insert the headers of the preferred IDP into the request.
     * The preferred IDP is configured for that path in the config file.
     *
     * @throws Exception
     */
    @Test
    public void testLoggedInWithTwoIdpsInsertsHeadersOfPreferredIdp() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).disableRedirectHandling().build();

        logInWithIdporten(cookieStore);
        logInWithGoogle(cookieStore);

        String pathWithIdportenPreferred = "/idporten/idporten/is/preferred";
        HttpGet getRequest = new HttpGet(BASEURL + pathWithIdportenPreferred);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = client.execute(getRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpResponseStatus.OK.code(),
                "Should be a success response without further need to login or redirect.");

        verify(1, getRequestedFor(urlPathEqualTo(pathWithIdportenPreferred))
                .withHeader(RequestInterceptor.HEADERNAME + "pid", matching("\\d{11}"))
        );
    }

    /**
     * When logged in with two IDPs, the preferred one should have its headers inserted to the request, but there
     * should be a reference to the other IDPs which the client is logged in for.
     *
     * @throws Exception
     */
    @Test
    public void testLoggedInWithTwoIdpsInsertsIdentifierOfSecondaryIdpOnSecuredPath() throws Exception {
        String expectedHeaderName = "X-additional-data/testgoogle-email";

        CookieStore cookieStore = new BasicCookieStore();
        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).disableRedirectHandling().build();

        logInWithIdporten(cookieStore);
        logInWithGoogle(cookieStore);

        String pathWithIdportenPreferred = "/idporten/idporten/is/preferred";
        HttpGet getRequest = new HttpGet(BASEURL + pathWithIdportenPreferred);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = client.execute(getRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpResponseStatus.OK.code(),
                "Should be a success response without further need to login or redirect.");

        verify(1, getRequestedFor(urlPathEqualTo(pathWithIdportenPreferred))
                .withHeader(RequestInterceptor.HEADERNAME + "pid", matching("\\d{11}"))
                .withHeader(expectedHeaderName, matching(emailInGoogleToken))
        );
    }

    /**
     * When logged in with two IDPs, the preferred one should have its headers inserted to the request, but there
     * should be a reference to the other IDPs which the client is logged in for.
     *
     * @throws Exception
     */
    @Test
    public void testLoggedInWithTwoIdpsInsertsIdentifierOfSecondaryIdpOnUnsecuredPath() throws Exception {
        String expectedHeaderName = "X-additional-data/testgoogle-email";

        CookieStore cookieStore = new BasicCookieStore();
        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).disableRedirectHandling().build();

        logInWithIdporten(cookieStore);
        logInWithGoogle(cookieStore);

        String unsecuredPathWithIdportenPreferred = "/unsecured/idporten/preferred";
        HttpGet getRequest = new HttpGet(BASEURL + unsecuredPathWithIdportenPreferred);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        HttpResponse response = client.execute(getRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpResponseStatus.OK.code(),
                "Should be a success response without further need to login or redirect.");

        verify(1, getRequestedFor(urlPathEqualTo(unsecuredPathWithIdportenPreferred))
                .withHeader(RequestInterceptor.HEADERNAME + "pid", matching("\\d{11}"))
                .withHeader(expectedHeaderName, matching(emailInGoogleToken))
        );
    }

    @Test
    public void testPostRequestOnUnsecuredPath() throws Exception {
        HttpPost postRequest = new HttpPost(BASEURL + "/unsecured");
        postRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        httpClient.execute(postRequest);

        verify(1, postRequestedFor(urlPathEqualTo("/unsecured")));
    }

    @Test(enabled = false) // What to expect here???
    public void testPostRequestOnSecuredPath() throws Exception {
        String actualPath = "/idporten/tralala";

        HttpPost postRequest = new HttpPost(BASEURL + actualPath);
        postRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);
        httpClient.execute(postRequest);

        verify(1, getRequestedFor(urlPathEqualTo(actualPath)));
    }

    /**
     * Makes a request so that the next execution on that client has a valid Google cookie.
     *
     * @param path
     * @return Request on the specified path with host header for the mock server.
     * @throws Exception
     */
    private static HttpGet getRequestWithValidGoogleCookie(String path) throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);
        httpClient.execute(getRequest);

        List<Cookie> cookies = cookieStore.getCookies();

        Cookie apacheCookie = cookies.stream()
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findAny()
                .orElseThrow(() -> new AssertionError("Should find a cookie"));

        getRequest = new HttpGet(BASEURL + path);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);
        getRequest.setHeader("Cookie", String.format("%s=%s", apacheCookie.getName(), apacheCookie.getValue()));

        return getRequest;
    }

    /**
     * Logs in to the IDP configured for a path, using a Cookie Store to manage the cookies generated from logging in.
     * The HttpClient that needs to use the valid cookies must be configured with the provided Cookie Store.
     *
     * @param url
     * @param cookieStore
     * @throws Exception
     */
    private static void logIn(String url, CookieStore cookieStore) throws Exception {
        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        client.execute(getRequest);
    }

    private static void logInWithGoogle(CookieStore cookieStore) throws Exception {
        logIn(BASEURL + "/google", cookieStore);
    }

    private static void logInWithIdporten(CookieStore cookieStore) throws Exception {
        logIn(BASEURL + "/idporten", cookieStore);
    }

    /**
     * Returns a response from a logged in request which either follow all redirects automatically or returns the
     * first response whether it's a redirect or not.
     *
     * @param path
     * @param followRedirect
     * @return
     * @throws Exception
     */
    private static HttpResponse getGoogleLoggedInResponse(String path, boolean followRedirect) throws Exception {
        HttpClient clientForCaller;
        CookieStore cookieStore = new BasicCookieStore();
        HttpClient clientToLogIn = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        if (followRedirect) {
            clientForCaller = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        } else {
            clientForCaller = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).disableRedirectHandling().build();
        }
        String url = BASEURL + "/google";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);
        clientToLogIn.execute(getRequest);

        List<Cookie> cookies = cookieStore.getCookies();

        cookies.stream()
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findAny()
                .orElseThrow(() -> new AssertionError("Should find a cookie"));

        getRequest = new HttpGet(BASEURL + path);
        getRequest.setHeader(HttpHeaderNames.HOST.toString(), mockServerHostName);

        return clientForCaller.execute(getRequest);
    }
}
