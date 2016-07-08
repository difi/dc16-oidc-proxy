package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.config.TypesafeCookieConfig;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.*;

public class RequestInterceptorTest {

    private static Logger logger = LoggerFactory.getLogger(RequestInterceptorTest.class);

    private RequestInterceptor requestInterceptor;
    private String host;
    private String path;
    private String pid;
    private String tokenType;
    private String aud;
    private CookieHandler cookieHandler;
    private Map<String, String> userData;


    @BeforeTest
    public void beforeTest() {
        this.host = "www.nav.no";
        this.path = "/trydgesøknad";
        this.pid = "08023549930";
        this.tokenType = "JWTToken";
        this.aud = "dificamp";
        this.userData = new HashMap<>();
        userData.put("pid", pid);
        userData.put("tokenType", tokenType);
        userData.put("aud", aud);
    }


    @Test
    public void insertHeaderToRequest() {
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, host + path);

        RequestInterceptor.insertUserDataToHeader(httpRequest, userData);

        HttpHeaders headers = httpRequest.headers();
        List<String> userDataHeaderFields = headers.getAllAsString(RequestInterceptor.HEADERNAME);

        Assert.assertTrue(headers.contains(RequestInterceptor.HEADERNAME));
        Assert.assertTrue(headers.getAsString(RequestInterceptor.HEADERNAME).contains(pid));
        userData.forEach((key, value) -> Assert.assertTrue(headers.getAsString(RequestInterceptor.HEADERNAME).contains(String.format("%s=%s", key, value))));
    }
}
