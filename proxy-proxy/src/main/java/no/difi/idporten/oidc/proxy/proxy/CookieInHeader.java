package no.difi.idporten.oidc.proxy.proxy;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;


public class CookieInHeader {

    //Inserts a cookie with a seesionID fetched from the configuration, and a uuid from generating
    //the cookie, into the response's header. The response is sent to the client and the cookie is
    //stored int he clients browser.
    public void insertCookieIntoHeader(HttpResponse httpResponse, String sessionID, String uuid ){
        httpResponse.headers().add("Set-Cookie", sessionID+"="+uuid);
    }

    //Checks either e response or a request for a specific cookie.
    public boolean checkHeaderForCookie(HttpHeaders httpHeaders, String cookieValue, String headerField){
        if (httpHeaders.contains(headerField)){
            if (httpHeaders.get(headerField).contains(cookieValue)){
                return true;
            }
        }
        return false;
    }

}
