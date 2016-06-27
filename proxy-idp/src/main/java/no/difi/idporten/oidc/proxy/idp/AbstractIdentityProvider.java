package no.difi.idporten.oidc.proxy.idp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

abstract class AbstractIdentityProvider implements IdentityProvider {

    protected static Gson gson = new GsonBuilder().create();
    protected static HttpClient httpClient = HttpClients.createDefault();

}
