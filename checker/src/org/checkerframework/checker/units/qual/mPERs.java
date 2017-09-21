package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.time.duration.s;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Meter per second.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@UnitsRelations({@Relation(op = Op.DIV, lhs = m.class, rhs = s.class, res = mPERs.class)})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(Speed.class)
public @interface mPERs {}
