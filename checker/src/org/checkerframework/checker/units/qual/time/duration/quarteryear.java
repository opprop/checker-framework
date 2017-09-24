package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.time.TimeRelation;
import org.checkerframework.checker.units.qual.time.instant.CALquarteryear;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Quarter-Year.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@TimeRelation(duration = quarteryear.class, instant = CALquarteryear.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
// Defined as a Gregorian year / 4 = 31556952 / 4 = 7889238 seconds
@TimeMultiple(timeUnit = s.class, multiplier = 7889238L)
public @interface quarteryear {}
