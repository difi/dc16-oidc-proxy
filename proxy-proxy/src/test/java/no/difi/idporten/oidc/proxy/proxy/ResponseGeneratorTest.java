package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class ResponseGeneratorTest {

    private static Logger logger = LoggerFactory.getLogger(ResponseGeneratorTest.class);

    private ResponseGenerator responseGenerator;


    @BeforeTest
    public void injectIdpConfigProvider() {
        Injector injector = Guice.createInjector(new ConfigModule(), new ProxyModule());

        this.responseGenerator = new ResponseGenerator();
    }

    @Test
    public void responseGenerator() {
        Assert.assertNotNull(responseGenerator);
    }

    // We need more sophisticated mocking to do these tests as they actually write to a Netty channel
    @Test(enabled = false)
    public void generateRedirectResponse() {
    }

    @Test(enabled = false)
    public void generateDefaultResponse() {
    }

    @Test(enabled = false)
    public void generateJWTResponse() {

    }
}
