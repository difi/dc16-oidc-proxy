package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import no.difi.idporten.oidc.proxy.api.ConfigProvider;
import no.difi.idporten.oidc.proxy.model.AccessRequirement;
import no.difi.idporten.oidc.proxy.model.Host;
import no.difi.idporten.oidc.proxy.model.Path;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypesafeConfigProvider implements ConfigProvider {

    private Config config;

    private List<Host> hosts = new ArrayList<>();
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


            //hosts.add(host);

            System.out.println(config.getObjectList(String.format("host.%s.paths",key)).get(0).toConfig().getString("path"));
            System.out.println(config.getObjectList(String.format("host.%s.paths",key)).get(0).toConfig().getString("security"));
            System.out.println(config.getObjectList(String.format("host.%s.paths",key)).get(0).toConfig().getString("idp"));


            System.out.println(config.getObjectList(String.format("host.%s.paths",key)).get(1).toConfig().getString("path"));


            config.getObjectList(String.format("host.%s.paths",key)).stream();

            //config.getStringList(String.format("host.%s.paths.path",key)).stream()
             //       .forEach(host::addPathname);
                    //.forEach(host::setIdp);




            //config.getStringList(String.format("host.%s.paths",key));



            //String newString = config.getString(String.format("host.%s.paths.path",key));


            //System.out.println(newString);
            //List<Integer> securityList = config.getIntList(String.format("host.%s.security", key));





        }
    }

    @Override
    public AccessRequirement forUri(URI uri) {
        int minSecurityLevel = 4;
        if(hostSecurity.get(uri.getHost()) != null){
            minSecurityLevel = hostSecurity.get(uri.getHost());
        }
        return new AccessRequirement(hostHostname.get(uri.getHost()),minSecurityLevel,pathPathnames.get(uri.getPath()));

    }
}
