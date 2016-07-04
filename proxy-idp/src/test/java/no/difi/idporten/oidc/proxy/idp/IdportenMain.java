package no.difi.idporten.oidc.proxy.idp;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.model.UserData;

import java.util.Scanner;

public class IdportenMain {

    public static void main(String... args) throws Exception {
        IdentityProvider identityProvider = new IdportenIdentityProvider(null);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Go here: " + identityProvider.generateRedirectURI());

        System.out.print("Write code: ");
        String code = scanner.next();

        UserData userData = identityProvider.getToken("http://localhost:8080/?code=" + code);
        System.out.println(userData);
    }
}
