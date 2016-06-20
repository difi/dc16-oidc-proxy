package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import no.difi.idporten.oidc.proxy.api.ConfigProvider;
import no.difi.idporten.oidc.proxy.model.AccessRequirement;
import no.difi.idporten.oidc.proxy.model.Host;
import no.difi.idporten.oidc.proxy.model.Path;
import no.difi.idporten.oidc.proxy.model.Security;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypesafeConfigProvider implements ConfigProvider {

    private Config config;

    private List<Host> hosts = new ArrayList<>();
    private List<Security> securities = new ArrayList<>();
    private List<Path> paths = new ArrayList<>();

    private Map<String, Host> hostHostname = new HashMap<>();
    private Map<String,Integer> hostSecurity = new HashMap<>();
    private Map<String,Path> pathPathnames = new HashMap<>();


    @Inject
    public TypesafeConfigProvider(Config config) {
        this.config = config;

        for (String key : config.getObject("host").keySet()) {

            Host host = new Host();
            config.getStringList(String.format("host.%s.hostname", key)).stream()
                    .peek(hostname -> hostHostname.put(hostname, host))
                    .forEach(host::addHostname);
            hosts.add(host);

            Security security = new Security();

            config.getIntList(String.format("host.%s.security", key)).stream()
                    .peek(securitylevel -> hostSecurity.put("test", security.getSecurityLevel()))
                    .forEach(security::setSecurityLevel);
            securities.add(security);

            Path path = new Path();

            config.getStringList(String.format("host.%s.path",key)).stream()
                    .peek(pathname -> pathPathnames.put(pathname,path))
                    .forEach(path::addPath);

            paths.add(path);




        }
    }

    @Override
    public AccessRequirement forUri(URI uri) {
        return new AccessRequirement(hostHostname.get(uri.getHost()),3,pathPathnames.get(uri.getPath()));

    }
}
