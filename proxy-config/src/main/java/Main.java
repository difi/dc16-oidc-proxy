import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

import java.net.URL;

/**
 * Created by camp-eul on 16.06.2016.
 */
public class Main {
    String hostname = "default";
    String level = "#3";

    public static void main(String args[]) throws Exception {

    }

    public static Config makeConfig(String hostname){


        Config config = ConfigFactory.empty();
        config.entrySet("Hostname",hostname);


    }

    }
