package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.handler.codec.http.*;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.config.DefaultSecurityConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class RequestInterceptorTest {


    private String host;

    private String path;

    private String pid;

    private String sub;

    private Map<String, String> userData;

    private SecurityConfigProvider provider;


    @BeforeTest
    public void beforeTest() {
        Injector injector = Guice.createInjector(new ConfigModule());
        provider = injector.getInstance(SecurityConfigProvider.class);
        this.host = "localhost:8080";
        this.path = "/idporten";
        this.pid = "08023549930";
        this.sub = "2O31Jp9E4MvsI4dGi58bFZLf6tpu";
        this.userData = new HashMap<>();
        userData.put("pid", pid);
        userData.put("sub", sub);
    }


    @Test
    public void insertHeaderToRequest() {
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, host + path);

        RequestInterceptor.insertUserDataToHeader(httpRequest, userData, provider.getConfig(host, path).get());

        HttpHeaders headers = httpRequest.headers();

        Assert.assertTrue(headers.contains(RequestInterceptor.HEADERNAME + "pid"));
        Assert.assertTrue(headers.getAsString(RequestInterceptor.HEADERNAME + "pid").contains(pid));
        userData.forEach((key, value) -> Assert.assertTrue(headers.getAsString(
                RequestInterceptor.HEADERNAME + key).contains(String.format("%s", value))));
    }
}
