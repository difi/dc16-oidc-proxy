package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class ProxyModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NettyHttpListener.class).in(Singleton.class);
    }
}
