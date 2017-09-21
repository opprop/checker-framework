package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.time.TimeRelation;
import org.checkerframework.checker.units.qual.time.instant.CALmillennia;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Millennia (1000 Gregorian years).
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@TimeRelation(duration = millennia.class, instant = CALmillennia.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
@TimeMultiple(timeUnit = s.class, multiplier = 31556952000L)
public @interface millennia {}
