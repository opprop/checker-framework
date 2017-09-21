package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.UnitsMultiple;
import org.checkerframework.checker.units.qual.time.TimeRelation;
import org.checkerframework.checker.units.qual.time.instant.CALus;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Microsecond.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@TimeRelation(duration = us.class, instant = CALus.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
@UnitsMultiple(quantity = s.class, prefix = Prefix.micro)
@TimeMultiple(timeUnit = ns.class, multiplier = 1000L)
public @interface us {}
