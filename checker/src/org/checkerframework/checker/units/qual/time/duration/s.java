package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.time.TimeRelation;
import org.checkerframework.checker.units.qual.time.instant.CALs;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * A second (1/60 of a minute).
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@TimeRelation(duration = s.class, instant = CALs.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
public @interface s {
    Prefix value() default Prefix.one;
}
