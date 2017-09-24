package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.time.TimeRelation;
import org.checkerframework.checker.units.qual.time.instant.CALhalfday;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Half-Day (12 hours).
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@TimeRelation(duration = halfday.class, instant = CALhalfday.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
@TimeMultiple(timeUnit = s.class, multiplier = 43200L)
public @interface halfday {}
