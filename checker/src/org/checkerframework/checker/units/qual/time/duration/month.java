package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.time.TimeRelation;
import org.checkerframework.checker.units.qual.time.instant.CALmonth;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Month.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@TimeRelation(duration = month.class, instant = CALmonth.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
// defined as a Gregorian year in seconds / 12 = 31556952 / 12 = 2629746
@TimeMultiple(timeUnit = s.class, multiplier = 2629746L)
public @interface month {}
