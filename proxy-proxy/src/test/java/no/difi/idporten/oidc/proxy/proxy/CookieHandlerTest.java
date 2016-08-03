package no.difi.idporten.oidc.proxy.proxy;

import com.typesafe.config.ConfigFactory;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import no.difi.idporten.oidc.proxy.config.TypesafeCookieConfig;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.*;

public class CookieHandlerTest {

    private CookieConfig cookieConfig;

    private String host;

    private String idp;

    private String path;

    private String cookieName;

    private String uuid;

    private String salt;

    private String useragent;

    private int security;

    private CookieHandler cookieHandler;

    List<Map.Entry<String, String>> prefIdpsGoogleTwitterIdporten= new ArrayList<>(Arrays.asList(
            new AbstractMap.SimpleEntry<>("google", "email"),
            new AbstractMap.SimpleEntry<>("twitter", "username"),
            new AbstractMap.SimpleEntry<>("idporten", "pid")));

    @BeforeTest
    public void injectIdpConfigProvider() {
        this.cookieConfig = new TypesafeCookieConfig(ConfigFactory.load());

        this.host = "www.nav.no";
        this.path = "/trydgesøknad";
        this.idp = prefIdpsGoogleTwitterIdporten.get(0).getKey();
        this.security = 3;
        this.cookieName = cookieConfig.getName();
        this.uuid = "aValidUuidMustBe64BytesLongaValidUuidMustBe64BytesLongaValidUuidMustBe64BytesLongaValidUuidMustBe64BytesLong";
        this.cookieHandler = new CookieHandler(cookieConfig, host, prefIdpsGoogleTwitterIdporten);
        this.salt = "salt";
    }


    @Test
    public void insertCookieToResponse() {
        String cookieName = "testCookie";
        String cookieValue = "testValue";
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        HttpHeaders headers = httpResponse.headers();
        Assert.assertFalse(headers.contains(HttpHeaderNames.SET_COOKIE));
        CookieHandler.insertCookieToResponse(httpResponse, cookieName, cookieValue, salt, useragent);
        Cookie nettyCookie = ClientCookieDecoder.STRICT.decode(headers.getAsString(HttpHeaderNames.SET_COOKIE));
        Assert.assertEquals(nettyCookie.name(), cookieName);
        Assert.assertEquals(nettyCookie.path(), "/");

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
        ProxyCookie actualProxyCookie = cookieHandler.generateCookie(userData, security, 20, 120);

        Assert.assertNotNull(actualProxyCookie);
        Assert.assertEquals(actualProxyCookie.getHost(), host);
        Assert.assertEquals(actualProxyCookie.getIdp(), idp);
        Assert.assertEquals(actualProxyCookie.getUserData().get("pid"), pid);
    }

    @Test (enabled = false)
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
        Assert.assertFalse(cookieHandler.getValidProxyCookie(httpRequest, salt, useragent).isPresent());
    }

}
