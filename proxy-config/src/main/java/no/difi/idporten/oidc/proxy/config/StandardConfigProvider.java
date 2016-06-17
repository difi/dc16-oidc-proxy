package no.difi.idporten.oidc.proxy.config;

import no.difi.idporten.oidc.proxy.api.ConfigProvider;
import no.difi.idporten.oidc.proxy.model.AccessRequirement;
import no.difi.idporten.oidc.proxy.model.Host;

import java.net.URI;
import java.util.ArrayList;

public class StandardConfigProvider implements ConfigProvider {

    /**Based on uri, finds host and min security requirement (and other things), and returns the
     * correct accessRequirement
     */

    private Host host;
    private String path;

    //List of hosts requiring level four clearance
    private ArrayList<Host> levelFour;

    //List of paths requiring level four clearance
    private ArrayList<String> securePaths;
    private int minLevel;

    public void addToHostList(Host host){
        this.levelFour.add(host);
    }

    //Checking if the list of level four hosts contains this host
    public boolean needsLevelFour(Host host){
        if(this.levelFour.contains(host))
        {
            return true;
        } else {
            return false;
        }
    }

    //Checking if the list of level four paths contains this path
    public boolean pathRequirement(String path) {
        if(this.securePaths.contains(path)){
            return true;
        } else {
            return false;
        }
    }

    //Finding and returning what host is needed for the accessrequirement
    public Host findHost(Host host){
        //Host hostNeeded = new Host(host);
        return host;
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

        String hostString = uri.getHost();
        this.host = new Host(hostString);
        this.path = uri.getPath();

        //Returns accessrequirement based on host, and min security level
        //AccessRequirement accessRequirement = new AccessRequirement(host,3);

        if(needsLevelFour(host)){
            Host hostRequirement = findHost(host);
            AccessRequirement accessRequirement = new AccessRequirement(hostRequirement,4);
            return accessRequirement;
        } else if (pathRequirement(path)){
            Host hostRequirement = findHost(host);
            AccessRequirement accessRequirement = new AccessRequirement(hostRequirement,4);
            return accessRequirement;
        } else {
            Host hostRequirement = findHost(host);
            AccessRequirement accessRequirement = new AccessRequirement(hostRequirement,3);
            return accessRequirement;
        }


    }
}