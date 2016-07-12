package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.*;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestInterceptorTest {


    private String host;

    private String path;

    private String pid;

    private String tokenType;

    private String aud;

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

        RequestInterceptor.insertUserDataToHeader(httpRequest, userData, null);

        HttpHeaders headers = httpRequest.headers();

        Assert.assertTrue(headers.contains(RequestInterceptor.HEADERNAME));
        Assert.assertTrue(headers.getAsString(RequestInterceptor.HEADERNAME).contains(pid));
        userData.forEach((key, value) -> Assert.assertTrue(headers.getAsString(
                RequestInterceptor.HEADERNAME).contains(String.format("%s=%s", key, value))));
    }
}
