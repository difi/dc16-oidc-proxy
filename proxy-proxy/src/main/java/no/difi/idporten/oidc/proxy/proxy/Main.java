package no.difi.idporten.oidc.proxy.proxy;

public class Main {
    public static void main(String[] args) {
        NettyHttpListener inboundHttpListener = new NettyHttpListener();
        inboundHttpListener.start();
    }
}
