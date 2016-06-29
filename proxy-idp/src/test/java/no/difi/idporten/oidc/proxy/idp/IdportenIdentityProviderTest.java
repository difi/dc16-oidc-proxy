package no.difi.idporten.oidc.proxy.idp;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IdportenIdentityProviderTest {


    @Test(enabled = false)
    public void testingGenerateURI() throws IdentityProviderException {
        IdentityProvider identityProvider = new IdportenIdentityProvider(null);

        Assert.assertTrue(identityProvider.generateURI().contains("difi.no"));
        Assert.assertTrue(identityProvider.generateURI().startsWith("https://"));


    }

    //@Test
    //public void testingUserData() throws IdentityProviderException {
    //    IdentityProvider identityProvider = new IdportenIdentityProvider();
    //    identityProvider.
    //}

}
