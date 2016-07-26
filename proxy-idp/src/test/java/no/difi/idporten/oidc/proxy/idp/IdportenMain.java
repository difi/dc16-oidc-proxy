package no.difi.idporten.oidc.proxy.idp;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.model.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class IdportenMain {

    private static Logger logger = LoggerFactory.getLogger(IdportenMain.class);

    public static void main(String... args) throws Exception {
        IdentityProvider identityProvider = new IdportenIdentityProvider(null);
        Scanner scanner = new Scanner(System.in);

        logger.debug("Go here: {}", identityProvider.generateRedirectURI());

        logger.debug("Write code: ");
        String code = scanner.next();

        UserData userData = identityProvider.getToken("http://localhost:8080/?code=" + code);
        logger.debug(userData.toString());
    }
}
