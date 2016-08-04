package no.difi.idporten.oidc.proxy.proxy;

import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import no.difi.idporten.oidc.proxy.proxy.util.RegexMatcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Optional;

public class HTMLGeneratorTest {

    @Mock
    SecurityConfig securityConfigMock = Mockito.mock(SecurityConfig.class);

    private String templateUrlFromSecurityConfig = "custom-error-page.html";

    @Test
    public void getErrorPage() {
        String expectedMessage = "www.example.com kan ikke nås";
        String notExpectedPattern = "\\{\\{\\w+\\}\\}";
        String actualHTML = HTMLGenerator.getErrorPage(expectedMessage);

        MatcherAssert.assertThat("Expected message should be inserted to the HTML template.",
                actualHTML, Matchers.containsString(expectedMessage));
        MatcherAssert.assertThat("Generated HTML should not contain the curly braces used around variables.",
                actualHTML, Matchers.not(RegexMatcher.matchesRegex(notExpectedPattern)));
    }

    @Test
    public void testErrorPageWithUrlFromSecurityConfig() {
        Mockito.doReturn(Optional.of(templateUrlFromSecurityConfig)).when(securityConfigMock).getErrorPageUrl();

        String expectedMessage = "www.example.com kan ikke nås";
        String expectedContentFromCustomTemplate = "This is a custom error page";
        String notExpectedPattern = "\\{\\{\\w+\\}\\}";
        String actualHTML = HTMLGenerator.getErrorPage(expectedMessage, securityConfigMock);

        MatcherAssert.assertThat("Expected message should be inserted to the HTML template.",
                actualHTML, Matchers.containsString(expectedMessage));
        MatcherAssert.assertThat("HTML string should be the custom one for the current SecurityConfig.",
                actualHTML, Matchers.containsString(expectedContentFromCustomTemplate));
        MatcherAssert.assertThat("Generated HTML should not contain the curly braces used around variables.",
                actualHTML, Matchers.not(RegexMatcher.matchesRegex(notExpectedPattern)));
    }

    @Test
    public void testUsesDefaultErrorPageWhenUrlIsNotConfigured() {
        Mockito.doReturn(Optional.empty()).when(securityConfigMock).getErrorPageUrl();

        String expectedMessage = "www.example.com kan ikke nås";
        String notExpectedPattern = "\\{\\{\\w+\\}\\}";
        String actualHTML = HTMLGenerator.getErrorPage(expectedMessage);

        MatcherAssert.assertThat("Expected message should be inserted to the HTML template.",
                actualHTML, Matchers.containsString(expectedMessage));
        MatcherAssert.assertThat("Generated HTML should not contain the curly braces used around variables.",
                actualHTML, Matchers.not(RegexMatcher.matchesRegex(notExpectedPattern)));
    }

    @Test
    public void testShouldHandleInvalidTemplateUrlWithFallbackToDefaultUrl() {
        Mockito.doReturn(Optional.of("你的中文好")).when(securityConfigMock).getErrorPageUrl();
        String notExpectedPattern = "\\{\\{\\w+\\}\\}";

        String expectedMessage = "差不多";
        String actualHTML = HTMLGenerator.getErrorPage(expectedMessage, securityConfigMock);

        MatcherAssert.assertThat("Expected message should be inserted to the HTML template.",
                actualHTML, Matchers.containsString(expectedMessage));
        MatcherAssert.assertThat("Generated HTML should not contain the curly braces used around variables.",
                actualHTML, Matchers.not(RegexMatcher.matchesRegex(notExpectedPattern)));
    }
}
