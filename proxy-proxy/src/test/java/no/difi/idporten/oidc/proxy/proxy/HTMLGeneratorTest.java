package no.difi.idporten.oidc.proxy.proxy;

import org.testng.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

public class HTMLGeneratorTest {

    private static Logger logger = LoggerFactory.getLogger(HTMLGeneratorTest.class);

    @Test
    public void getErrorPage() {
        String expectedMessage = "www.example.com kan ikke nås";
        String notExpectedPattern = "\\{\\{\\w+\\}\\}";
        String actualHTML = HTMLGenerator.getErrorPage(expectedMessage);

        Assert.assertTrue(actualHTML.contains(expectedMessage));
        Assert.assertFalse(Pattern.matches(notExpectedPattern, actualHTML));
    }
}
