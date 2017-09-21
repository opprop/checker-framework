package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.time.TimeRelation;
import org.checkerframework.checker.units.qual.time.instant.CALweek;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Week.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@TimeRelation(duration = week.class, instant = CALweek.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
// 86400 * 7 = 604800
@TimeMultiple(timeUnit = s.class, multiplier = 604800L)
public @interface week {}
