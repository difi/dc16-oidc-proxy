package no.difi.idporten.oidc.proxy.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HTMLGenerator {

    /**
     * Generates a HTML error page based on the template in resources/error-page.html with the message incluced in it
     * @param message
     * @return HTML page as a String
     */
    public static String getErrorPage(String message) {
        try {
            File file = new File(HTMLGenerator.class.getClassLoader().getResource("error-page.html").getFile());
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            return bufferedReader.lines().map(line -> replaceVariable(line, message)).collect(Collectors.joining("\n"));
        } catch (FileNotFoundException exc) {
            exc.printStackTrace();
        } catch (NullPointerException exc) {
            exc.printStackTrace();
        }
        return String.format("no cannot %s", message);
    }

    /**
     * Replaces patterns matching {{variableName}} in a line and returns with the curly braces and variable name replaced.
     * @param line
     * @param message
     * @return
     */
    private static String replaceVariable(String line, String message) {
        Pattern regex = Pattern.compile("\\{\\{(\\w+)\\}\\}");
        Matcher matcher = regex.matcher(line);
        if (matcher.find()) {
            // Can be used to look up multiple variables from a map
            // return matcher.replaceAll(String.format("%s", getVariable(matcher.group(1))));
            // But for now we only need one variable
            return matcher.replaceAll(String.format("%s", message));
        }
        return line;
    }
}
