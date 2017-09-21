package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.time.duration.TimeDuration;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Units of speed.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@UnitsRelations({
    @Relation(op = Op.DIV, lhs = Length.class, rhs = TimeDuration.class, res = Speed.class)
})
@SubtypeOf(UnknownUnits.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface Speed {}
