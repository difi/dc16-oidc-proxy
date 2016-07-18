package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.config.TypesafeCookieConfig;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

public class CookieHandlerTest {

    private static Logger logger = LoggerFactory.getLogger(CookieHandlerTest.class);

    private CookieConfig cookieConfig;
    private String host;
    private String path;
    private String cookieName;
    private String uuid;
    private String salt;
    private CookieHandler cookieHandler;


    @BeforeTest
    public void injectIdpConfigProvider() {
        Injector injector = Guice.createInjector(new ConfigModule(), new ProxyModule());
        this.cookieConfig = new TypesafeCookieConfig(ConfigFactory.load());

        this.host = "www.nav.no";
        this.path = "/trydgesøknad";
        this.cookieName = cookieConfig.getName();
        this.uuid = "aValidUuidMustBe64BytesLongaValidUuidMustBe64BytesLongaValidUuidMustBe64BytesLongaValidUuidMustBe64BytesLong";
        this.cookieHandler = new CookieHandler(cookieConfig, host, path);
        this.salt = "salt";
    }


    @Test
    public void insertCookieToResponse() {
        String cookieName = "testCookie";
        String cookieValue = "testValue";
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        HttpHeaders headers = httpResponse.headers();
        Assert.assertFalse(headers.contains(HttpHeaderNames.SET_COOKIE));
        CookieHandler.insertCookieToResponse(httpResponse, cookieName, cookieValue, salt, "MUSTBEFIXED");
        Set<Cookie> nettyCookies = ServerCookieDecoder.STRICT.decode(headers.getAsString(HttpHeaderNames.SET_COOKIE));
        Assert.assertEquals(nettyCookies.size(), 1);
/*        Assert.assertTrue(nettyCookies.stream()
                .filter(cookie -> cookie.name().equals(cookieName))
                .findAny().get().value().equals(cookieValue));*/
    }

    @Test
    public void generateCookie() {

        String pid = "08023549930";
        HashMap<String, String> userData = new HashMap<>();
        userData.put("pid", pid);
        userData.put("tokenType", "JWTToken");
        userData.put("aud", "dificamp");

        Assert.assertNotNull(cookieConfig);
        Assert.assertNotNull(cookieHandler);

        // Causing error at parameters here
        ProxyCookie actualProxyCookie = cookieHandler.generateCookie(userData, 20, 120);

        Assert.assertNotNull(actualProxyCookie);
        Assert.assertEquals(actualProxyCookie.getHost(), host);
        Assert.assertEquals(actualProxyCookie.getPath(), path);
        Assert.assertEquals(actualProxyCookie.getUserData().get("pid"), pid);
    }

    @Test
    public void getCookieFromRequest() throws Exception {
        HttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, host + path);
        httpRequest.headers().set(HttpHeaderNames.COOKIE, ServerCookieEncoder.STRICT.encode(cookieName, uuid));

        // Using reflection to get private method
        String privateMethodName = "getCookieFromRequest";
        Method reflectedGetCookieFromRequest = CookieHandler.class.getDeclaredMethod(privateMethodName, HttpRequest.class);
        reflectedGetCookieFromRequest.setAccessible(true);

        Cookie actualNettyCookie = ((Optional<Cookie>) reflectedGetCookieFromRequest.invoke(cookieHandler, httpRequest)).get();

        Assert.assertNotNull(actualNettyCookie);
        Assert.assertEquals(actualNettyCookie.value(), uuid);
        Assert.assertEquals(actualNettyCookie.name(), cookieName);
        Assert.assertTrue(httpRequest.headers().getAsString(HttpHeaderNames.COOKIE).contains(uuid));
    }

    @Test
    public void getValidProxyCookie() {
        HttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, host + path);
        httpRequest.headers().set(HttpHeaderNames.COOKIE, ServerCookieEncoder.STRICT.encode(cookieName, uuid));

        // Cookie storage is empty, so it should not get a valid cookie here
        Assert.assertFalse(cookieHandler.getValidProxyCookie(httpRequest, "salt", "MUSTBEFIXED").isPresent());
    }


}
