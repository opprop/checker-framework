package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.Op;
import org.checkerframework.checker.units.qual.Relation;
import org.checkerframework.checker.units.qual.UnitsRelations;
import org.checkerframework.checker.units.qual.km;
import org.checkerframework.checker.units.qual.kmPERh;
import org.checkerframework.checker.units.qual.time.TimeRelation;
import org.checkerframework.checker.units.qual.time.instant.CALh;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Hour.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@UnitsRelations({@Relation(op = Op.DIV, lhs = km.class, rhs = h.class, res = kmPERh.class)})
@TimeRelation(duration = h.class, instant = CALh.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
public @interface h {}
