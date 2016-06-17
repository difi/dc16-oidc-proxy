package no.difi.idporten.oidc.proxy.config;

import no.difi.idporten.oidc.proxy.model.Host;

import java.util.ArrayList;

public class ConfigProviderTest {

    public static void main(String [] args){
        ArrayList<Host> levelFourHosts = new ArrayList<>(); //Hosts needing level 4 clearance
        ArrayList<String> securePaths = new ArrayList<>(); //Paths needing level 4 clearance

        Host host1 = null; // new Host("default/test/");
        Host host2 = null; // new Host("default2");

        securePaths.add("/test/");
        levelFourHosts.add(host1);

        StandardConfigProvider standardConfigProvider1 = new StandardConfigProvider(levelFourHosts, securePaths);
        StandardConfigProvider standardConfigProvider2 = new StandardConfigProvider(levelFourHosts, securePaths);
        StandardConfigProvider standardConfigProvider3 = new StandardConfigProvider(levelFourHosts, securePaths);

        //Test 1
        if (standardConfigProvider1.needsLevelFour(host1) == true){
            System.out.println("Test 1 success");
        }
        else{
            System.out.println("Test 1 failed");
        }

        //Test 2
        if(standardConfigProvider2.needsLevelFour(host2) == false){
            System.out.println("Test 2 success");
        }
        else{
            System.out.println("Test 2 failed");
        }

        //Test 3
        if(standardConfigProvider3.pathRequirement("/test/") == true){
            System.out.println("Test 3 success");
        }
        else{
            System.out.println("Test 3 failed");
        }

        //Test 4
        if(standardConfigProvider3.pathRequirement("/test2") == false){
            System.out.println("Test 4 success");
        }
        else{
            System.out.println("Test 4 failed");
        }




    }

}
