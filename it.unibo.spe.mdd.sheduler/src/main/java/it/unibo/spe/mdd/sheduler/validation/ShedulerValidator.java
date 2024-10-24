/*
 * generated by Xtext 2.32.0
 */
package it.unibo.spe.mdd.sheduler.validation;


import it.unibo.spe.mdd.sheduler.TimeUtils;
import it.unibo.spe.mdd.sheduler.sheduler.*;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.CheckType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * This class contains custom validation rules. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
public class ShedulerValidator extends AbstractShedulerValidator {
	
    @Check
    public void checkRelativeTimeIsRepresentable(RelativeTime relativeTime) {
        try {
            TimeUtils.toDuration(relativeTime);
        } catch (ArithmeticException e) {
            warning("Relative time is not representable on the JVM", relativeTime, ShedulerPackage.Literals.RELATIVE_TIME__TIME_SPANS, 0);
        }
    }

    @Check
    public void checkAbsoluteTimeIsRepresentable(AbsoluteTime absoluteTime) {
        try {
            TimeUtils.toLocalDateTime(absoluteTime);
        } catch (ArithmeticException e) {
            warning("Absolute time is not representable on the JVM", absoluteTime, ShedulerPackage.Literals.ABSOLUTE_TIME__DATE, 0);
        }
    }

    @Check
    public void checkAbsoluteTimeIsInTheFuture(AbsoluteTime absoluteTime) {
        if (TimeUtils.toLocalDateTime(absoluteTime).isBefore(LocalDateTime.now())) {
            warning("Absolute time should be in the future", absoluteTime, ShedulerPackage.Literals.ABSOLUTE_TIME__TIME, 0);
        }
    }

    @Check(CheckType.FAST)
    public void ensureDateIsValid(Date date) {
        if (date.getYear() < 0) {
            error("Year must be positive", date, ShedulerPackage.Literals.DATE__YEAR, 0);
        }
        if (date.getMonth() < 1 || date.getMonth() > 12) {
            error("Month must be between 1 and 12", date, ShedulerPackage.Literals.DATE__MONTH, 0);
        }
        if (date.getDay() < 1 || date.getDay() > 31) {
            error("Day must be between 1 and 31", date, ShedulerPackage.Literals.DATE__DAY, 0);
        }
    }

    @Check(CheckType.FAST)
    public void ensureClockTimeIsValid(ClockTime clockTime) {
        if (clockTime.getHour() < 0 || clockTime.getHour() > 23) {
            error("Hour must be between 0 and 23", clockTime, ShedulerPackage.Literals.CLOCK_TIME__HOUR, 0);
        }
        if (clockTime.getMinute() < 0 || clockTime.getMinute() > 59) {
            error("Minute must be between 0 and 59", clockTime, ShedulerPackage.Literals.CLOCK_TIME__MINUTE, 0);
        }
        if (clockTime.getSecond() < 0 || clockTime.getSecond() > 59) {
            error("Second must be between 0 and 59", clockTime, ShedulerPackage.Literals.CLOCK_TIME__SECOND, 0);
        }
        if (clockTime.getMillisecond() < 0 || clockTime.getMillisecond() > 999) {
            error("Millisecond must be between 0 and 999", clockTime, ShedulerPackage.Literals.CLOCK_TIME__MILLISECOND, 0);
        }
        if (clockTime.getNanosecond() < 0 || clockTime.getNanosecond() > 999) {
            error("Nanosecond must be between 0 and 999", clockTime, ShedulerPackage.Literals.CLOCK_TIME__NANOSECOND, 0);
        }
    }

    @Check(CheckType.FAST)
    public void ensureTimeSpanIsValid(TimeSpan timeSpan) {
        if (timeSpan.getDuration() < 0) {
            error("Duration must be positive", timeSpan, ShedulerPackage.Literals.TIME_SPAN__DURATION, 0);
        }
        switch (timeSpan.getUnit()) {
            case MILLISECONDS, NANOSECONDS -> {
                if (timeSpan.getDuration() >= 1000) {
                    error("Duration must be less than 1000", timeSpan, ShedulerPackage.Literals.TIME_SPAN__DURATION, 0);
                }
            }
            case SECONDS, MINUTES -> {
                if (timeSpan.getDuration() >= 60) {
                    error("Duration must be less than 60", timeSpan, ShedulerPackage.Literals.TIME_SPAN__DURATION, 0);
                }
            }
            case HOURS -> {
                if (timeSpan.getDuration() >= 24) {
                    error("Duration must be less than 24", timeSpan, ShedulerPackage.Literals.TIME_SPAN__DURATION, 0);
                }
            }
        }
    }

    @Check(CheckType.FAST)
    public void ensureTaskNamesAreUniqueWithinPool(TaskPool pool) {
        Set<String> names = new HashSet<>();
        for (Task task : pool.getTasks()) {
            if (task.getName() == null) continue;
            if (names.contains(task.getName())) {
                error("Repeated task ID: " + task.getName(), task, ShedulerPackage.Literals.TASK__NAME, 0);
            } else {
                names.add(task.getName());
            }
        }
    }

    @Check(CheckType.FAST)
    public void ensurePoolNamesAreUniqueWithinPool(TaskPoolSet pools) {
        Set<String> names = new HashSet<>();
        for (TaskPool pool : pools.getPools()) {
            if (pool.getName() == null) continue;
            if (names.contains(pool.getName())) {
                error("Repeated pool ID: " + pool.getName(), pool, ShedulerPackage.Literals.TASK_POOL__NAME, 0);
            } else {
                names.add(pool.getName());
            }
        }
    }
}