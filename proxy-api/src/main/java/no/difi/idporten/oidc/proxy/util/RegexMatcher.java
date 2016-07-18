package no.difi.idporten.oidc.proxy.util;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Thanks to http://www.vogella.com/tutorials/Hamcrest/article.html#using-hamcrest-string-matchers
 */
public class RegexMatcher extends TypeSafeMatcher<String> {

    private final String regex;

    public RegexMatcher(final String regex) {
        this.regex = regex;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("matches regular expression=`" + regex + "`");
    }

    @Override
    public boolean matchesSafely(final String string) {
        return string.matches(regex);
    }

    public static RegexMatcher matchesRegex(final String regex) {
        return new RegexMatcher(regex);
    }
}