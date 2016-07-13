package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.*;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

public class RedirectCookieHandlerTest {

    private String host;
    private String path;

    @Mock
    private CookieConfig cookieConfigMock;

    @BeforeMethod
    public void setUp() {
        this.cookieConfigMock = Mockito.mock(CookieConfig.class);
        this.host = "configured.host.com";
        this.path = "/secured-path";
    }

    @Test
    public void testCanInsertCookieToResponseAndItCanBeRetrievedAfter() {
        RedirectCookieHandler redirectCookieHandler = new RedirectCookieHandler(cookieConfigMock, host, path);
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);

        Assert.assertFalse(RedirectCookieHandler.findRedirectCookiePath(httpRequest).isPresent());

        redirectCookieHandler.insertCookieToResponse(httpResponse);

        Optional<String> retrievedPathFromCookieOptional = RedirectCookieHandler.findRedirectCookiePath(httpRequest);

        Assert.assertTrue(retrievedPathFromCookieOptional.isPresent());

        String retrievedPathFromCookie = retrievedPathFromCookieOptional.get();

        Assert.assertEquals(retrievedPathFromCookie, path);
    }
}
