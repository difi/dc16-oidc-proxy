package no.difi.idporten.oidc.proxy.dist;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import no.difi.idporten.oidc.proxy.config.ConfigModule;
import no.difi.idporten.oidc.proxy.proxy.NettyHttpListener;
import no.difi.idporten.oidc.proxy.proxy.ProxyModule;
import no.difi.idporten.oidc.proxy.storage.StorageModule;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;

public class Main {

    public static void main(String... args) throws CmdLineException{
        Main main = new Main();
        new CmdLineParser(main).parseArgument(args);
        main.run();
    }

    @Option(name = "--config", aliases = "-c", usage = "Configuration to load.")
    private String configuration = "application";

    public void run() {
        Injector injector = Guice.createInjector(new ArrayList<Module>() {{
            add(new ConfigModule(configuration));
            add(new StorageModule());
            add(new ProxyModule());
        }});

        injector.getInstance(NettyHttpListener.class).run();
    }
}
