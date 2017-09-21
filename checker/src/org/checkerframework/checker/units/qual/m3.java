package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Volume of meter cubed.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@UnitsRelations({@Relation(op = Op.MUL, lhs = m.class, rhs = m2.class, res = m3.class)})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(Volume.class)
public @interface m3 {}
