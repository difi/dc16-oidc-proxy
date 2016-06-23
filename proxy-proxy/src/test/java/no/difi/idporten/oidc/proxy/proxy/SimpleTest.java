package no.difi.idporten.oidc.proxy.proxy;

import com.google.common.io.ByteStreams;
import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.net.URI;

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
    public void simple() throws Exception {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteStreams.copy(URI.create("http://localhost:8080/").toURL().openStream(), baos);
        } catch (Exception e) {
            // Currently expected.
            logger.info("Received '{}'.", e.getMessage(), e);
        }
    }
}
