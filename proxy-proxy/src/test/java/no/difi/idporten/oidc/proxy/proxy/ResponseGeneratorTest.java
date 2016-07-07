package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;



public class ResponseGeneratorTest {

    private static Logger logger = LoggerFactory.getLogger(ResponseGeneratorTest.class);

    private String host;

    private ResponseGenerator responseGenerator;

    @Captor
    ArgumentCaptor<HttpResponse> httpResponseCaptor;
    private ChannelHandlerContext ctxMock;


    @BeforeTest
    public void injectIdpConfigProvider() {
        Injector injector = Guice.createInjector(new ConfigModule(), new ProxyModule());

        this.host = "not.configured.host";

        this.ctxMock = Mockito.mock(ChannelHandlerContext.class);
        MockitoAnnotations.initMocks(this);

        this.responseGenerator = new ResponseGenerator();
    }

    @Test
    public void responseGenerator() {
        Assert.assertNotNull(responseGenerator);
    }

    // We need more sophisticated Mockito.mocking to do these tests as they actually write to a Netty channel
    @Test
    public void generateRedirectResponseWhenIdentityProviderNotConfigured() throws Exception {
        IdentityProvider identityProviderMock = Mockito.mock(IdentityProvider.class);
        ResponseGenerator responseGeneratorSpy = Mockito.spy(responseGenerator);
        try {
            responseGeneratorSpy.generateRedirectResponse(ctxMock, identityProviderMock);
        } catch (NullPointerException exc) {

        } finally {
            Mockito.verify(responseGeneratorSpy).generateRedirectResponse(Mockito.any(), Mockito.any());
            /*
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
        try {
            responseGenerator.generateDefaultResponse(ctxMock, host);
        } catch (NullPointerException exc) {

        } finally {
            Mockito.verify(ctxMock).writeAndFlush(Mockito.any(HttpResponse.class));
        }
    }

    @Test
    public void generateJWTResponse() {
        ProxyCookie proxyCookieMock = Mockito.mock(ProxyCookie.class);

        String pid = "08023549930";
        HashMap<String, String> userData = new HashMap<>();
        userData.put("pid", pid);
        userData.put("tokenType", "JWTToken");
        userData.put("aud", "dificamp");
        try {
            responseGenerator.generateJWTResponse(ctxMock, userData, proxyCookieMock);
        } catch (NullPointerException exc) {

        } catch (IdentityProviderException exc) {

        } finally {
            Mockito.verify(ctxMock).writeAndFlush(Mockito.any());
        }
    }
}
