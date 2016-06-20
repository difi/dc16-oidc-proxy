package no.difi.idporten.oidc.proxy.model;

import java.util.ArrayList;
import java.util.List;

public class Path {

    private List<String> paths = new ArrayList<String>();
    private int minValue;


    public void addPath(String path){
        paths.add(path);

    }

    public List<String> getPaths(){
        return this.paths;
    }
    @Override
    public String toString() {
        return "Host{" +
                "pathname=" + paths +
                '}';
    }

}
