package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.lang.model.type.TypeKind;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultInUncheckedCodeFor;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TargetLocations;
import org.checkerframework.framework.qual.TypeUseLocation;

/**
 * The bottom type in the Units type system. Programmers should rarely write this type.
 *
 * @checker_framework.manual #units-checker Units Checker
 * @checker_framework.manual #bottom-type the bottom type
 */
@SubtypeOf({}) // programmatically assigned as the bottom type
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ImplicitFor(
    literals = {LiteralKind.NULL},
    types = {TypeKind.NULL, TypeKind.VOID},
    typeNames = {java.lang.Void.class}
)
@DefaultFor(TypeUseLocation.LOWER_BOUND)
@DefaultInUncheckedCodeFor(TypeUseLocation.LOWER_BOUND)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@TargetLocations({TypeUseLocation.EXPLICIT_LOWER_BOUND, TypeUseLocation.EXPLICIT_UPPER_BOUND})
public @interface UnitsBottom {}
