package no.difi.idporten.oidc.proxy.idp;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GoogleIdentityProviderTest {

    @Test
    public void testingGenerateURI()throws IdentityProviderException {
        IdentityProvider identityProvider = new GoogleIdentityProvider();

        Assert.assertTrue(identityProvider.generateURI().contains("accounts.google.com"));
        Assert.assertTrue(identityProvider.generateURI().startsWith("https"));



    }
}
