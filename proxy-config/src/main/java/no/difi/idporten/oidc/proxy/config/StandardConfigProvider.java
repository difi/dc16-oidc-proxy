package no.difi.idporten.oidc.proxy.config;

import no.difi.idporten.oidc.proxy.api.ConfigProvider;
import no.difi.idporten.oidc.proxy.model.AccessRequirement;
import no.difi.idporten.oidc.proxy.model.Host;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;

public class StandardConfigProvider implements ConfigProvider {

    /**Based on uri, finds host and min security requirement (and other things), and returns the
     * correct accessRequirement
     */

    private Host host;
    private String path;

    //List of hosts requiring level four clearance and list of hosts just needing level two.
    private ArrayList<Host> levelFour;
    private ArrayList<Host> levelTwo;

    //Add same for lower levels

    //List of paths requiring level four clearance
    private ArrayList<String> securePaths;

    public void addToHostList(Host host){
        this.levelFour.add(host);
    }

    /**
     * Constructor, takes the list of hosts requiring level four and the list of paths requiring level four
     * @param levelFour
     * @param securePaths
     */
    public StandardConfigProvider(ArrayList<Host> levelFour, ArrayList<String> securePaths){
        this.levelFour = levelFour;
        this.securePaths = securePaths;

    }

    /**
     * Alternative constructor if we need to check against a leveltwo list as well.
     * @param levelFour
     * @param levelTwo
     * @param securePaths
     */
    public StandardConfigProvider(ArrayList<Host> levelFour, ArrayList<Host> levelTwo, ArrayList<String> securePaths) {
        this.levelFour = levelFour;
        this.levelTwo = levelTwo;
        this.securePaths = securePaths;
    }

    //Checking if the list of level two hosts contains this host
    public boolean needsLevelTwo(Host host){
        for (Host h: this.levelTwo) {
            if(host.getHostname() == h.getHostname()){
                return true;
            }
        }
        return false;
    }

    //Checking if the list of level four hosts contains this host
    public boolean needsLevelFour(Host host){
        for (Host h: this.levelFour) {
            if(host.getHostname() == h.getHostname()){
                return true;
            }
        }
        return false;
    }

    //Checking if the list of level four paths contains this path
    public boolean pathRequirement(String path) {
        if(this.securePaths.contains(path)){
            return true;
        } else {
            return false;
        }
    }

    //Finding and returning the host access requirement
    public Host findHost(Host host){

        //Only test values here.
        Host customHost = host;
        if(host.getHostname() == "4"){
            customHost = new Host("Default");
        }
        if(host.getHostname() == "5"){
            customHost = new Host("Custom");
        }
        return customHost;
    }

    public Host getHost(){
        return this.host;
    }

    public String getPath(){
        return this.path;
    }


    /**
     * Function returning an accessrequirement
     * @param uri
     * @return
     */
    public AccessRequirement forUri(URI uri){

        String hostString = uri.getHost();
        this.host = new Host(hostString);
        this.path = uri.getPath();

        //Loop that returns AccessRequirement based on host, and minimum security level
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