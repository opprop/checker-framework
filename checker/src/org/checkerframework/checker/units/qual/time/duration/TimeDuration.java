package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.checker.units.qual.time.TimeRelation;
import org.checkerframework.checker.units.qual.time.instant.TimeInstant;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Units of time durations. These units denote the amount of time passed between two events.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@TimeRelation(duration = TimeDuration.class, instant = TimeInstant.class)
@SubtypeOf(UnknownUnits.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface TimeDuration {}
