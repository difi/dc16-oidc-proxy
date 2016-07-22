package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URL;

public class SimpleTest {

    private static Logger logger = LoggerFactory.getLogger(SimpleTest.class);

    private Thread thread;

    @BeforeClass
    public void beforeClass() throws Exception {
        Injector injector = Guice.createInjector(new ConfigModule(), new ProxyModule());
        thread = new Thread(injector.getInstance(NettyHttpListener.class));
        thread.start();

        Thread.sleep(1_000);
    }

    @AfterClass
    public void afterClass() {
        thread.interrupt();
    }

    @Test
    public void testSecuredConfigured() throws Exception {
        try {
            URL url = URI.create("http://localhost:8080/google").toURL();
            System.out.println("SECUREDCONFIGURED: " + url.openConnection().getHeaderFields().values().toString());
            Assert.assertTrue(url.openConnection().getHeaderFields().values().toString().contains("[HTTP/1.1 302 Found]"));
        } catch (Exception e) {
            logger.info("Received '{}'.", e.getMessage(), e);
        }
    }

    @Test
    public void testUnsecuredConfigured() throws Exception {
        try {
            URL url = URI.create("http://localhost:8080/").toURL();
            System.out.println("UNSECUREDCONFIGURED: " + url.openConnection().getHeaderFields().values().toString());
            Assert.assertTrue(url.openConnection().getHeaderFields().values().toString().contains("[HTTP/1.1 200 OK]"));
        } catch (Exception e) {
            logger.info("Received '{}'.", e.getMessage(), e);
        }
    }

    @Test
    public void testUnconfigured() throws Exception {
        try {
            URL url = URI.create("http://127.0.0.1:8080").toURL();
            //Assert.assertEquals(url.openConnection().getHeaderFields().values().toString(), "[HTTP/1.1 400 Bad Request]");
            System.out.println("UNCONFIGURED: " + url.openConnection().getHeaderFields().values().toString());
            Assert.assertTrue(url.openConnection().getHeaderFields().values().toString().contains("[HTTP/1.1 400 Bad Request]"));
        } catch (Exception e) {
            logger.info("Received '{}'.", e.getMessage(), e);
        }
    }


}
