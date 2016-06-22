package no.difi.idporten.oidc.proxy.openid;

import com.google.api.client.auth.oauth2.BrowserClientRequestUrl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

public class OpenIdConnecter{

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = new BrowserClientRequestUrl(
                "https://server.example.com/authorize", "s6BhdRkqt3").setState("xyz")
                .setRedirectUri("https://client.example.com/cb").build();
        response.sendRedirect(url);
    }












}
