package no.difi.idporten.oidc.proxy.proxy;

import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private static String hostName;
    private static int outboundPort = 80;
    private static int inboundPort = 8080;


    public static void main(String args[]) throws Exception {

        Logger logger = LoggerFactory.getLogger(Main.class);

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