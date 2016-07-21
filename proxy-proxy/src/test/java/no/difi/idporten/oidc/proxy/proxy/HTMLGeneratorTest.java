package no.difi.idporten.oidc.proxy.proxy;

import no.difi.idporten.oidc.proxy.proxy.util.RegexMatcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

public class HTMLGeneratorTest {

    @Test
    public void getErrorPage() {
        String expectedMessage = "www.example.com kan ikke nås";
        String notExpectedPattern = "\\{\\{\\w+\\}\\}";
        String actualHTML = HTMLGenerator.getErrorPage(expectedMessage);

        MatcherAssert.assertThat("",
                actualHTML, Matchers.containsString(expectedMessage));
        MatcherAssert.assertThat("Generated HTML should not contain the curly braces used around variables",
                actualHTML, Matchers.not(RegexMatcher.matchesRegex(notExpectedPattern)));
    }
}
