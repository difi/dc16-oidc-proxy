package no.difi.idporten.oidc.proxy.config;

import no.difi.idporten.oidc.proxy.model.Host;

import java.util.ArrayList;

/**
 * Created by camp-nto on 17.06.2016.
 */
public class ConfigProviderTest {

    public static void main(String [] args){
        ArrayList<Host> levelFourhosts = new ArrayList<>(); //Hosts needing level 4 clearance
        ArrayList<String> securePaths = new ArrayList<>(); //Paths needing level 4 clearance

        Host host1 = new Host("default/test/");
        Host host2 = new Host("default2");

        securePaths.add("/test/");
        levelFourhosts.add(host1);

        StandardConfigProvider standardConfigProvider1 = new StandardConfigProvider(levelFourhosts, securePaths);
        StandardConfigProvider standardConfigProvider2 = new StandardConfigProvider(levelFourhosts, securePaths);
        StandardConfigProvider standardConfigProvider3 = new StandardConfigProvider(levelFourhosts, securePaths);

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
        if(standardConfigProvider3.pathRequirement("/test/")){
            System.out.println("Test 3 success");
        }
        else{
            System.out.println("Test 3 failed");
        }




    }

}
