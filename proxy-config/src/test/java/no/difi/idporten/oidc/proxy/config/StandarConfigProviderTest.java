package no.difi.idporten.oidc.proxy.config;

import junit.framework.Assert;
import no.difi.idporten.oidc.proxy.model.AccessRequirement;
import no.difi.idporten.oidc.proxy.model.Host;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by camp-nto on 17.06.2016.
 */
public class StandarConfigProviderTest {

        //URI: http://www.pierobon.org/iis/review1.htm

    public static void main(String[] args) throws URISyntaxException {
        ArrayList<Host> hosts = new ArrayList<Host>();
        Host test1 = new Host("default");
        Host test2 = new Host("Secure host");

        hosts.add(test2);
        hosts.add(test1);

        ArrayList<String> securePaths = new ArrayList<>();

        securePaths.add("/bin/");
        securePaths.add("/test/");

        URI level4path = new URI(null,"level3host","/test/",null);
        URI level4host = new URI(null,"Secure host","...",null);
        URI level3host = new URI(null,"level3host","...",null);

        Host testHostOne = new Host("level3host");
        Host testHostTwo = new Host("Secure host");
        Host testHostThree = new Host("level3host");

        StandardConfigProvider standardConfigProvider = new StandardConfigProvider(hosts, securePaths);

        AccessRequirement testOne = new AccessRequirement(testHostOne,4);
        AccessRequirement t1check = standardConfigProvider.forUri(level4path);

        AccessRequirement testTwo = new AccessRequirement(testHostTwo,4);
        AccessRequirement t2check = standardConfigProvider.forUri(level4host);

        AccessRequirement testThree = new AccessRequirement(testHostThree,3);
        AccessRequirement t3check = standardConfigProvider.forUri(level3host);

        Assert.assertEquals(testOne,t1check);
        Assert.assertEquals(testTwo,t2check);
        Assert.assertEquals(testThree,t3check);

        }

}
