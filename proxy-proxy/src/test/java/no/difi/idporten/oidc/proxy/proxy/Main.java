package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.storage.StorageModule;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class Main {

    @Test
    public void main() {
        Injector injector = Guice.createInjector(new ArrayList<Module>() {{
            add(new ConfigModule());
            add(new StorageModule());
            add(new ProxyModule());
        }});
        System.out.println(injector.getBindings());
        injector.getInstance(NettyHttpListener.class).run();

    }
}
