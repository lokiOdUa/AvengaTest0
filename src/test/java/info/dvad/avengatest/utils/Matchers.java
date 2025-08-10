package info.dvad.avengatest.utils;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class Matchers {
    public static TypeSafeMatcher<String> isCloseToNow(long toleranceInDays) {
        long toleranceInSeconds = toleranceInDays * 24 * 60 * 60;
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String dateString) {
                try {
                    ZonedDateTime publishDate = ZonedDateTime.parse(dateString);
                    long difference = Math.abs(Duration.between(publishDate, ZonedDateTime.now()).getSeconds());
                    return difference <= toleranceInSeconds;
                } catch (DateTimeParseException e) {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a date string within " + toleranceInDays + " days of now");
            }
        };
    }
}
