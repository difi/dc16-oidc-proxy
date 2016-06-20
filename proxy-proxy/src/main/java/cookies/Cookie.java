package cookies;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class Cookie {

    private final String value;
    private final String uuid; // Universally unique identifier per user.

    public Cookie(String uuid, String value){
        this.uuid = uuid;
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }

    public String getUUID(){
        return this.uuid;
    }

    public String toString(){
        return this.value + "=" + this.uuid;
    }


    public boolean checkHeaderForCookie(HttpHeaders httpHeaders, String cookieValue, String headerField){
        if (httpHeaders.contains(headerField)){
            if (httpHeaders.get(headerField).contains(cookieValue)){
                return true;
            }
        }
        return false;
    }


    /* this function adds this cookie to the response header*/
    public void insertCookieIntoHeader(HttpResponse httpResponse){
        httpResponse.headers().add("Set-Cookie", this);
    }

}
