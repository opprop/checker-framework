package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.time.TimeRelation;
import org.checkerframework.checker.units.qual.time.instant.CALforever;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * A conceptual duration of forever, artificially defined in Java 8 as {@linkplain Long#MAX_VALUE}
 * seconds + 999999999 nanoseconds.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@TimeRelation(duration = forever.class, instant = CALforever.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
@TimeMultiple(timeUnit = s.class, multiplier = Long.MAX_VALUE + 0.999999999D)
public @interface forever {}
