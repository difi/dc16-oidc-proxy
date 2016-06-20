package no.difi.idporten.oidc.proxy.proxy;

import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private static int inboundPort = 8080;

    private static HashSet<String> approvedHostNames = new HashSet<String>();


    public static void main(String args[]) throws Exception {

        approvedHostNames.add("www.example.com");
        approvedHostNames.add("www.vg.no");
        approvedHostNames.add("www.ntnu.no");
        approvedHostNames.add("www.ap.no");
        approvedHostNames.add("www.xkcd.no");

        /**
         * Using LittleProxy which is a library for Netty (https://github.com/adamfisk/LittleProxy)
         * This starts a proxy server configured to intercept HTTP requests and responses.
         * Any website can be accessed through this proxy with
         * curl www.example.com --proxy localhost:8080
         * Does not work with HTTPS yet
         */
        HttpProxyServer server =
                DefaultHttpProxyServer.bootstrap()
                        .withPort(inboundPort)
                        .withFiltersSource(new OIDCHttpFiltersSourceAdapter())
                        .start();
    }
}