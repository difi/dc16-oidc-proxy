package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

public class RedirectCookieHandlerTest {

    private String path;

    private String salt;

    private String useragent;

    @Mock
    private CookieConfig cookieConfigMock;

    @BeforeMethod
    public void setUp() {
        this.cookieConfigMock = Mockito.mock(CookieConfig.class);
        this.path = "/secured-path";
        this.salt = "salt";
        this.useragent = "useragent";
    }

    @Test
    public void testCanInsertCookieToResponseAndItCanBeRetrievedAfter() {
        RedirectCookieHandler redirectCookieHandler = new RedirectCookieHandler(path);
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);

        Assert.assertFalse(RedirectCookieHandler.findRedirectCookiePath(httpRequest, salt, useragent).isPresent());

        Cookie insertedCookie = redirectCookieHandler.insertCookieToResponse(httpResponse, salt, useragent);
        CookieHandler.insertCookieToRequest(httpRequest, insertedCookie.name(), insertedCookie.value());

        Optional<String> retrievedPathFromCookieOptional = RedirectCookieHandler.findRedirectCookiePath(httpRequest, salt, useragent);

        Assert.assertTrue(retrievedPathFromCookieOptional.isPresent());

        String retrievedPathFromCookie = retrievedPathFromCookieOptional.get();

        Assert.assertEquals(retrievedPathFromCookie, path);
    }
}
