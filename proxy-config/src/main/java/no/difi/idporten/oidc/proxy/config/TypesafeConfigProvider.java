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
    private List<Object> strings = new ArrayList<>();
    private Map<String, Host> hostHostname = new HashMap<>();
    private Map<String,Integer> hostSecurity = new HashMap<>();
    private Map<String,String> pathPathnames = new HashMap<>();


    public void objectHandler(Object a){
        Host nextHost = new Host();

    }
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


            for(ConfigObject a : config.getObjectList(String.format("host.%s.paths",key))){
                Config newConfig = a.toConfig();
                host.setIdp(newConfig.getString("idp"));
                //host.addPathname(newConfig.getString("path"));
                Path path = new Path();
                path.addPath(newConfig.getString("path"));
                host.addPathname(path);
                hostSecurity.put(newConfig.getString("path"),newConfig.getInt("security"));
                //pathPathnames.put(host.getHostname(),newConfig.getString("path"));
            }
            hosts.add(host);







        }
    }

    @Override
    public AccessRequirement forUri(URI uri) {
        int minSecurityLevel = 0;

        if(hostSecurity.get(uri.getPath()) != null){
            minSecurityLevel = hostSecurity.get(uri.getPath());
        }

        Host hostObject = hostHostname.get(uri.getHost());

        return new AccessRequirement(hostObject,minSecurityLevel,hostObject.getPathnames().get(0),hostObject.getIdp());

    }
}
