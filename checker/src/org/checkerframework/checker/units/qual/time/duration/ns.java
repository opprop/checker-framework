package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.UnitsMultiple;
import org.checkerframework.checker.units.qual.time.TimeRelation;
import org.checkerframework.checker.units.qual.time.instant.CALns;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Nanosecond.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@TimeRelation(duration = ns.class, instant = CALns.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
@UnitsMultiple(quantity = s.class, prefix = Prefix.nano)
@TimeMultiple(timeUnit = ns.class, multiplier = 1L)
public @interface ns {}
