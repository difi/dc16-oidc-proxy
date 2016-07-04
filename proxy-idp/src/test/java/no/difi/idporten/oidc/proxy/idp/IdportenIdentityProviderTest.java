package no.difi.idporten.oidc.proxy.idp;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IdportenIdentityProviderTest {

    @Test(enabled = false)
    public void testingGenerateURI() throws IdentityProviderException {
        IdentityProvider identityProvider = new IdportenIdentityProvider(null);

        Assert.assertTrue(identityProvider.generateRedirectURI().contains("difi.no"));
        Assert.assertTrue(identityProvider.generateRedirectURI().startsWith("https://"));
    }
}
