package no.difi.idporten.oidc.proxy.proxy;

import com.google.common.io.ByteStreams;
import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.net.URI;

public class SimpleTest {

    private Thread thread;

    @BeforeClass
    public void beforeClass() {
        Injector injector = Guice.createInjector(new ConfigModule(), new ProxyModule());
        thread = new Thread(injector.getInstance(NettyHttpListener.class));
        thread.start();
    }

    @AfterClass
    public void afterClass() {
        thread.interrupt();
    }

    @Test(enabled = false)
    public void simple() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteStreams.copy(URI.create("http://localhost:8080/").toURL().openStream(), baos);

        Assert.assertTrue(baos.toString().contains("xkcd"));
    }
}
