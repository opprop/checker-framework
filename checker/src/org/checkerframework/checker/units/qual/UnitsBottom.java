package org.checkerframework.checker.units.qual;

import static org.checkerframework.framework.qual.TypeUseLocation.EXPLICIT_LOWER_BOUND;
import static org.checkerframework.framework.qual.TypeUseLocation.EXPLICIT_UPPER_BOUND;
import static org.checkerframework.framework.qual.TypeUseLocation.LOWER_BOUND;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.lang.model.type.TypeKind;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TargetLocations;

/**
 * The bottom type in the Units type system. Programmers should rarely write this type.
 *
 * @checker_framework.manual #units-checker Units Checker
 * @checker_framework.manual #bottom-type the bottom type
 */
@SubtypeOf({}) // needs to be done programmatically
@ImplicitFor(
    types = {TypeKind.VOID},
    typeNames = Void.class,
    literals = LiteralKind.NULL
)
@DefaultFor({LOWER_BOUND})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@TargetLocations({EXPLICIT_LOWER_BOUND, EXPLICIT_UPPER_BOUND})
public @interface UnitsBottom {}
