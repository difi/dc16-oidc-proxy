package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import no.difi.idporten.oidc.proxy.api.ConfigProvider;
import no.difi.idporten.oidc.proxy.model.AccessRequirement;
import no.difi.idporten.oidc.proxy.model.Host;
import no.difi.idporten.oidc.proxy.model.Path;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class TypesafeConfigProvider implements ConfigProvider {

    private Map<String, Host> hostHostname = new HashMap<>();
    private Map<String, Integer> hostSecurity = new HashMap<>();

    @Inject
    public TypesafeConfigProvider(Config config) {

        for (String key : config.getObject("host").keySet()) {

            //New host object.
            Host host = new Host();


            //Adds all the different hostnames belonging to the host object.
            config.getStringList(String.format("host.%s.hostname", key)).stream()
                    .peek(hostname -> hostHostname.put(hostname, host))
                    .forEach(host::addHostname);

            //Iterates over the different path objects found from the configuration file
            for (ConfigObject a : config.getObjectList(String.format("host.%s.paths", key))) {
                //Converting the ConfigObject to a config file
                Config newConfig = a.toConfig();
                host.setIdp(newConfig.getString("idp"));

                //New path object that will belong to a host
                Path path = new Path();
                //path.addPath(newConfig.getString("path"));
                host.addPathname(path);

                //Making the map between paths and security level.
                hostSecurity.put(newConfig.getString("path"), newConfig.getInt("security"));
                //pathPathnames.put(host.getHostname(),newConfig.getString("path"));
            }
        }
    }

    /**
     * Returns an AccessRequirement object by finding the appropriate matches using mappings.
     */
    @Override
    public AccessRequirement forUri(URI uri) {
        int minSecurityLevel = 0;

        if (hostSecurity.get(uri.getPath()) != null) {
            minSecurityLevel = hostSecurity.get(uri.getPath());
        }

        Host hostObject = hostHostname.get(uri.getHost());

        return new AccessRequirement(hostObject, minSecurityLevel, hostObject.getPathnames().get(0), hostObject.getIdp());

    }
}
