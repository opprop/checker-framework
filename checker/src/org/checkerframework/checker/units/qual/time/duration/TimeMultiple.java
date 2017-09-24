package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.*;

/**
 * Defines the numeric relationship between a base time unit and the current time unit.
 *
 * <p>This is used for non-metric multiples of seconds, such as minutes, hours, days, weeks, and
 * years.
 *
 * <p>E.g. minutes would be defined as 60 seconds.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeMultiple {
    /** @return The base time unit to use. */
    Class<? extends Annotation> timeUnit();

    /** @return The multiplier of the base time unit. */
    double multiplier() default 1.0D;
}
