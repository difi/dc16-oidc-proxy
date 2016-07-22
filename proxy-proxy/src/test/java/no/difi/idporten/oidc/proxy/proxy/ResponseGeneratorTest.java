package no.difi.idporten.oidc.proxy.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.mockito.*;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.nio.charset.Charset;


public class ResponseGeneratorTest {

    private String notConfiguredHostName;

    private String configuredHostName;

    private String securedPath;

    private String redirectCookieName;

    private String salt = "salt";

    private ResponseGenerator responseGenerator;


    /**
     * This one can capture and show you which arguments were passed to methods you spy on.
     */
    @Captor
    private ArgumentCaptor<FullHttpResponse> httpResponseCaptor;

    /**
     * Acts as an empty ChannelHandlerContext object, but can be configured to return any value you want from any of
     * its methods if needed. We use the httpResponseCaptor to see which arguments writeAndFlush were called with.
     */
    @Mock
    private ChannelHandlerContext ctxMock;

    @Mock
    private SecurityConfig securityConfigMock;

    @Mock
    private CookieConfig cookieConfigMock;

    @BeforeTest
    public void setUpBeforeAllTests() {
        this.notConfiguredHostName = "not.configured.host";
        this.configuredHostName = "configured.host.com";
        this.securedPath = "/secured-path";
        this.redirectCookieName = "redirectCookie";
        this.responseGenerator = new ResponseGenerator();

        cookieConfigMock = Mockito.mock(CookieConfig.class);
        Mockito.doReturn(redirectCookieName).when(cookieConfigMock).getName();
    }


    @BeforeTest
    public void setUpChannelHandlerContextMock() {
        // Instantiating mock
        this.ctxMock = Mockito.mock(ChannelHandlerContext.class);
        MockitoAnnotations.initMocks(this); // Needed for httpResponseCaptor to work
    }

    @BeforeTest
    public void setUpSecurityConfigMock() {
        securityConfigMock = Mockito.mock(SecurityConfig.class);
        Mockito.doReturn(configuredHostName).when(securityConfigMock).getHostname();
        Mockito.doReturn(securedPath).when(securityConfigMock).getPath();
        Mockito.doReturn(cookieConfigMock).when(securityConfigMock).getCookieConfig();
        Mockito.doReturn(salt).when(securityConfigMock).getSalt();
    }

    @BeforeMethod
    public void setUpBeforeEachTest() {
        //Empty body
    }

    @AfterMethod
    public void tearDown() {
        Mockito.reset(ctxMock); // Don't think this works
    }

    @Test
    public void responseGenerator() {
        Assert.assertNotNull(responseGenerator);
    }

    @Test
    public void generateRedirectResponseWhenIdentityProviderNotConfigured() throws Exception {
        IdentityProvider identityProviderMock = Mockito.mock(IdentityProvider.class);

        // The spy is basically the real responseGenerator objects, but it watches which methods have been called
        // and how many times each method has been called.
        ResponseGenerator responseGeneratorSpy = Mockito.spy(responseGenerator);
        // Because of exceptions we need to wrap tests in these ugly try/catch things
        try {
            responseGeneratorSpy.generateRedirectResponse(ctxMock, identityProviderMock, securityConfigMock, securedPath, new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "MUSTFIX"));
        } catch (NullPointerException exc) {

        } finally {
            // All we need to test here is that the correct methods have been called and maybe which arguments
            // they were called with.
            Mockito.verify(responseGeneratorSpy).generateRedirectResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.any());
            Mockito.verify(identityProviderMock).generateRedirectURI();

            /* Not sure whether it's this method's responsibility to create an error when this goes south.
            Mockito.verify(ctxMock).writeAndFlush(httpResponseCaptor.capture());
            Mockito.verify(responseGeneratorSpy).generateDefaultResponse(Mockito.any(), Mockito.anyString());

            HttpResponse actual = httpResponseCaptor.getValue();
            Assert.assertTrue(actual instanceof HttpResponse);
            Assert.assertEquals(actual.status(), HttpResponseStatus.BAD_REQUEST);
            */
        }
    }

    @Test
    public void generateRedirectResponseWhenIdentityProviderIsConfigured() throws Exception {
        String validRedirectUrl = "valid.redirect.url";
        IdentityProvider identityProviderMock = Mockito.mock(IdentityProvider.class);
        ResponseGenerator responseGeneratorSpy = Mockito.spy(responseGenerator);
        Mockito.doReturn(validRedirectUrl).when(identityProviderMock).generateRedirectURI();
        try {
            responseGeneratorSpy.generateRedirectResponse(ctxMock, identityProviderMock, securityConfigMock, securedPath, new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "MUSTFIX"));
        } catch (NullPointerException exc) {

        } finally {
            Mockito.verify(responseGeneratorSpy).generateRedirectResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.any());
            Mockito.verify(identityProviderMock).generateRedirectURI();
            Mockito.atLeastOnce();
            Mockito.verify(ctxMock).writeAndFlush(httpResponseCaptor.capture());

            HttpResponse actual = httpResponseCaptor.getValue();
            Assert.assertTrue(actual instanceof HttpResponse);
            Assert.assertEquals(actual.status(), HttpResponseStatus.FOUND);
            Assert.assertTrue(actual.headers().contains(HttpHeaderNames.LOCATION));
            Assert.assertEquals(actual.headers().getAsString(HttpHeaderNames.LOCATION), validRedirectUrl);
        }
    }


    @Test
    public void generateDefaultResponse() {
        ResponseGenerator responseGeneratorSpy = Mockito.spy(responseGenerator);
        try {
            responseGeneratorSpy.generateDefaultResponse(ctxMock, notConfiguredHostName, HttpResponseStatus.BAD_REQUEST);
        } catch (NullPointerException exc) {

        } finally {
            Mockito.verify(responseGeneratorSpy).generateDefaultResponse(Mockito.any(), Mockito.anyString(), Mockito.any(HttpResponseStatus.class));
            Mockito.verify(ctxMock).writeAndFlush(httpResponseCaptor.capture());

            FullHttpResponse actual = httpResponseCaptor.getValue();
            Assert.assertTrue(actual instanceof HttpResponse);
            String content = actual.content().toString(Charset.forName("UTF-8"));
            Assert.assertEquals(actual.status(), HttpResponseStatus.BAD_REQUEST);
            Assert.assertTrue(actual.headers().getAsString(HttpHeaderNames.CONTENT_TYPE).contains(ResponseGenerator
                    .TEXT_HTML));
            Assert.assertTrue(content.contains(notConfiguredHostName));
        }
    }

    @Test
    public void generateServerErrorResponse() {
        String expectedMessage = "This is an error message.";
        ResponseGenerator responseGeneratorSpy = Mockito.spy(responseGenerator);
        try {
            responseGeneratorSpy.generateServerErrorResponse(ctxMock, expectedMessage);
        } catch (NullPointerException exc) {

        } finally {
            Mockito.verify(responseGeneratorSpy).generateServerErrorResponse(Mockito.any(), Mockito.anyString());
            Mockito.verify(ctxMock).writeAndFlush(httpResponseCaptor.capture());

            FullHttpResponse actual = httpResponseCaptor.getValue();
            Assert.assertTrue(actual instanceof HttpResponse);
            String content = actual.content().toString(Charset.forName("UTF-8"));
            Assert.assertEquals(actual.status(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
            Assert.assertTrue(actual.headers().getAsString(HttpHeaderNames.CONTENT_TYPE).contains(ResponseGenerator
                    .TEXT_HTML));
            Assert.assertTrue(content.contains(expectedMessage));
        }
    }

    @Test
    public void generateUnknownHostResponse() {
        String expectedMessage = "Host is not configured: an.unknown.host";
        ResponseGenerator responseGeneratorSpy = Mockito.spy(responseGenerator);
        try {
            responseGeneratorSpy.generateUnknownHostResponse(ctxMock, expectedMessage);
        } catch (NullPointerException exc) {

        } finally {
            Mockito.verify(responseGeneratorSpy).generateUnknownHostResponse(Mockito.any(), Mockito.anyString());
            Mockito.verify(ctxMock).writeAndFlush(httpResponseCaptor.capture());

            FullHttpResponse actual = httpResponseCaptor.getValue();
            Assert.assertTrue(actual instanceof HttpResponse);
            String content = actual.content().toString(Charset.forName("UTF-8"));
            Assert.assertEquals(actual.status(), HttpResponseStatus.BAD_REQUEST);
            Assert.assertTrue(actual.headers().getAsString(HttpHeaderNames.CONTENT_TYPE).contains(ResponseGenerator
                    .TEXT_HTML));
            Assert.assertTrue(content.contains(expectedMessage));
        }
    }
}
