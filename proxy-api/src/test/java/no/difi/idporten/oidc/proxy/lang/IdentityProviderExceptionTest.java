package no.difi.idporten.oidc.proxy.lang;

import org.testng.annotations.Test;

public class IdentityProviderExceptionTest {

    @Test
    @SuppressWarnings("all")
    public void simple() {
        new IdentityProviderException("Test");
        new IdentityProviderException("Test", null);
    }

}
