package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.storage.StorageModule;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class Main {

    @SuppressWarnings("all")
    @Test(groups = "manual")
    public void main() throws Exception {
        List<Module> modules = new ArrayList<>();
        modules.add(new ConfigModule());
        modules.add(new StorageModule());
        modules.add(new ProxyModule());

        Injector injector = Guice.createInjector(modules);

        Thread thread = new Thread(injector.getInstance(NettyHttpListener.class));
        thread.start();
        thread.join();
    }
}
