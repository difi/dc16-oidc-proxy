package no.difi.idporten.oidc.proxy.config;

import no.difi.idporten.oidc.proxy.api.ConfigProvider;
import no.difi.idporten.oidc.proxy.model.AccessRequirement;
import no.difi.idporten.oidc.proxy.model.Host;

import java.net.URI;

public class StandardConfigProvider implements ConfigProvider {

    /**Based on uri, finds host and min security requirement (and other things), and returns the
     * correct accessRequirement
     */

    private Host host;
    private String path;
    private int minLevel;

    public StandardConfigProvider(URI uri){
        this.path = uri.getPath();
        this.host = new Host(uri.getHost());
    }

    public Host getHost(){
        return this.host;
    }

    public String getPath(){
        return this.path;
    }

    private void findMinLeve(){
        //What decides minLeve, host and path
    }


    public AccessRequirement forUri(URI uri){

        /** What we can add:
         * - MinLevel has to be decided from uri
         * - AccessRequirement must use path, not only host and 3
         * - Perhaps: Add field port
         * - Perhaps: Add scheme
         */

        AccessRequirement accessRequirement = new AccessRequirement(,3); //Returns accessrequirement based on host, and min security level
        //Return accessRequirement based on path
        return accessRequirement;
    }
}