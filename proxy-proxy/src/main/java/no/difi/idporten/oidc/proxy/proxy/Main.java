package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Guice;
import com.google.inject.Module;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.storage.StorageModule;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Guice.createInjector(new ArrayList<Module>() {{
            add(new ConfigModule());
            add(new StorageModule());
            add(new ProxyModule());
        }}).getInstance(NettyHttpListener.class).run();
    }
}
