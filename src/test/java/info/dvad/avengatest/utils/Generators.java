package info.dvad.avengatest.utils;

import net.datafaker.Faker;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

public class Generators {
    public static OffsetDateTime getDateDaysBefore(int days) {
        var faker = new Faker();
        Instant instant = faker.date().past(days, TimeUnit.DAYS).toInstant();
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
