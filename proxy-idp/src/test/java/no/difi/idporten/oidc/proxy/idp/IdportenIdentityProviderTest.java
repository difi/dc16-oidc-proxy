package no.difi.idporten.oidc.proxy.idp;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IdportenIdentityProviderTest {

    @Test
    public void testingGenerateURI() throws IdentityProviderException {
        IdentityProvider identityProvider = new IdportenIdentityProvider();

        Assert.assertTrue(identityProvider.generateURI().contains("difi.no"));
        Assert.assertTrue(identityProvider.generateURI().startsWith("https://"));
    }
}
