package it.unibo.spe.mdd.sheduler;

import it.unibo.spe.mdd.sheduler.sheduler.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeUtils {
    public static Duration toDuration(TimeSpan timeSpan) {
        return switch (timeSpan.getUnit()) {
            case NANOSECONDS -> Duration.ofNanos(timeSpan.getDuration());
            case MILLISECONDS -> Duration.ofMillis(timeSpan.getDuration());
            case SECONDS -> Duration.ofSeconds(timeSpan.getDuration());
            case MINUTES -> Duration.ofMinutes(timeSpan.getDuration());
            case HOURS -> Duration.ofHours(timeSpan.getDuration());
            case DAYS -> Duration.ofDays(timeSpan.getDuration());
            case WEEKS -> Duration.ofDays(timeSpan.getDuration() * 7L);
            case YEARS -> Duration.ofDays(timeSpan.getDuration() * 375L);
        };
    }

    public static Duration toDuration(RelativeTime relativeTime) {
        return relativeTime.getTimeSpans().stream()
                .map(TimeUtils::toDuration)
                .reduce(Duration::plus)
                .orElse(Duration.ZERO);
    }

    public static LocalTime toLocalTime(ClockTime clockTime) {
        return LocalTime.of(clockTime.getHour(),
                clockTime.getMinute(),
                clockTime.getSecond(),
                clockTime.getMillisecond() * 1_000 + clockTime.getNanosecond());
    }

    public static LocalDate toLocalDate(Date date) {
        return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }

    public static LocalDateTime toLocalDateTime(AbsoluteTime absoluteTime) {
        return LocalDateTime.of(toLocalDate(absoluteTime.getDate()), toLocalTime(absoluteTime.getTime()));
    }
}
