package no.difi.idporten.oidc.proxy.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds all URIs. Can be used to add, and get URIs from uriLIst
 */


public class URICollection {

    private List<URI> uriList;

    //Constructor
    public URICollection(){
        this.uriList = new ArrayList<URI>();
    }

    //Adds parameter URI
    public void addURI(URI uri){
        this.uriList.add(uri);
    }

    //Gets URI from list by using parameter i
    public URI getURI(int i){
        if((i > 0) && (i < uriList.size())){
            return uriList.get(i);

        }
        else throw new IllegalArgumentException(i + "out of bound");
    }
}
