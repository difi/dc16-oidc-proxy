package no.difi.idporten.oidc.proxy.idp;

import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IdportenIdentityProviderTest {

    public String sample_token = "eyJqa3UiOiJodHRwczpcL1wvZWlkLWV4dHRlc3QuZGlmaS5ub1wvaWRwb3J0ZW4tb2lkYy1wcm92aWRlclwvandrIiwia2lkIjoiaWdiNUN5Rk1BbUZlZWk0TW5YQm82bWM5My03bUVwN29ncklxV2hNVGNLYyIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiIyTzMxSnA5RTRNdnNJNGRHaTU4YkZaTGY2dHB1IiwiYXVkIjoidGVzdF9ycF9laWRfZXh0dGVzdF9kaWZpIiwiYWNyIjoiTGV2ZWwzIiwiYW1yIjoiTWluaWQtUElOIiwiaXNzIjoiaHR0cHM6XC9cL2VpZC1leHR0ZXN0LmRpZmkubm9cL2lkcG9ydGVuLW9pZGMtcHJvdmlkZXJcLyIsInBpZCI6IjA4MDIzNTQ5OTMwIiwiZXhwIjoxNDY3MTg5NzcyLCJsb2NhbGUiOiJuYiIsImlhdCI6MTQ2NzE4NjE3Mn0.BCtZQsu9LjH5FJLUcVKB0zJQMjGdwp8AfSpIEhLHesAILmEP1zlyFLIGCPZu92JdB2172ilYZWx5gCRnUbIM8s52ePds814BfEatm2rOBXHNap15-COrliK9IC5YmGUEY2fzvZvHMzDgFP1DiTLNQ4VzLIr92XGlsCcvpnWnNJw";
    @Test
    public void testingGenerateURI() throws IdentityProviderException {
        IdentityProvider identityProvider = new IdportenIdentityProvider();

        Assert.assertTrue(identityProvider.generateURI().contains("difi.no"));
        Assert.assertTrue(identityProvider.generateURI().startsWith("https://"));


    }

    //@Test
    //public void testingUserData() throws IdentityProviderException {
    //    IdentityProvider identityProvider = new IdportenIdentityProvider();
    //    identityProvider.
    //}

}
