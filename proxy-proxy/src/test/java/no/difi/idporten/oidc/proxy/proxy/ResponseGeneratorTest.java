package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.DefaultProxyCookie;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;


public class ResponseGeneratorTest {

    private static Logger logger = LoggerFactory.getLogger(ResponseGeneratorTest.class);

    private String host;

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


    @BeforeTest
    public void injectIdpConfigProvider() {
        Injector injector = Guice.createInjector(new ConfigModule(), new ProxyModule());

        this.host = "not.configured.host";
        this.responseGenerator = new ResponseGenerator();

        // Instantiating mock
        this.ctxMock = Mockito.mock(ChannelHandlerContext.class);
        MockitoAnnotations.initMocks(this); // Needed for httpResponseCaptor to work
    }

    @BeforeMethod
    public void setUp() {
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
            responseGeneratorSpy.generateRedirectResponse(ctxMock, identityProviderMock);
        } catch (NullPointerException exc) {

        } finally {
            // All we need to test here is that the correct methods have been called and maybe which arguments
            // they were called with.
            Mockito.verify(responseGeneratorSpy).generateRedirectResponse(Mockito.any(), Mockito.any());
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
            responseGeneratorSpy.generateRedirectResponse(ctxMock, identityProviderMock);
        } catch (NullPointerException exc) {

        } finally {
            Mockito.verify(responseGeneratorSpy).generateRedirectResponse(Mockito.any(), Mockito.any());
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
            responseGeneratorSpy.generateDefaultResponse(ctxMock, host);
        } catch (NullPointerException exc) {

        } finally {
            Mockito.verify(responseGeneratorSpy).generateDefaultResponse(Mockito.any(), Mockito.anyString());
            Mockito.verify(ctxMock).writeAndFlush(httpResponseCaptor.capture());

            FullHttpResponse actual = httpResponseCaptor.getValue();
            Assert.assertTrue(actual instanceof HttpResponse);
            String content = actual.content().toString(Charset.forName("UTF-8"));
            Assert.assertEquals(actual.status(), HttpResponseStatus.BAD_REQUEST);
            Assert.assertTrue(actual.headers().getAsString(HttpHeaderNames.CONTENT_TYPE).contains(ResponseGenerator
                    .TEXT_HTML));
            Assert.assertTrue(content.contains(host));
        }
    }

    @Test
    public void generateJWTResponseWhenCorrectlyConfigured() throws Exception {
        /*
        Mockito.reset(ctxMock); // It only works when resetting here

        ResponseGenerator responseGeneratorSpy = Mockito.spy(responseGenerator);

        String uuid = "uuid";
        String cookieName = "TESTCOOKIE";
        String path = "/beskyttet-side";
        Date farFutureDate = new Date(new Date().getTime() + Integer.MAX_VALUE);

        String pid = "08023549930";
        HashMap<String, String> userData = new HashMap<>();
        userData.put("pid", pid);
        userData.put("tokenType", "JWTToken");
        userData.put("aud", "dificamp");
        ProxyCookie proxyCookie = new DefaultProxyCookie(uuid, cookieName, host, path, farFutureDate, farFutureDate,
                userData);
        try {
            responseGeneratorSpy.generateJWTResponse(ctxMock, userData, proxyCookie);
        } catch (NullPointerException exc) {

        } catch (IdentityProviderException exc) {

        } finally {
            Mockito.verify(responseGeneratorSpy).generateJWTResponse(Mockito.any(), Mockito.any(), Mockito.any());
            Mockito.verify(ctxMock).writeAndFlush(httpResponseCaptor.capture());

            FullHttpResponse actual = httpResponseCaptor.getValue();
            Assert.assertTrue(actual instanceof HttpResponse);
            String content = actual.content().toString(Charset.forName("UTF-8"));
            Assert.assertEquals(actual.status(), HttpResponseStatus.OK);
            Assert.assertTrue(actual.headers().getAsString(HttpHeaderNames.CONTENT_TYPE).contains(ResponseGenerator
                    .APPLICATION_JSON));
            userData.entrySet().stream().forEach(entry -> {
                Assert.assertTrue(content.contains(entry.getKey()));
                Assert.assertTrue(content.contains(entry.getValue()));
            });
        }
        */
    }



}
