package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Area of square kilometer.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@UnitsRelations({@Relation(op = Op.MUL, lhs = km.class, rhs = km.class, res = km2.class)})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(Area.class)
public @interface km2 {}
