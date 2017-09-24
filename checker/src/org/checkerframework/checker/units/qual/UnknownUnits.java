package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultInUncheckedCodeFor;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchyInUncheckedCode;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeUseLocation;

/**
 * UnknownUnits is the top type of the type hierarchy.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
// Note: unit relations for UnknownUnits are programatically added
@SubtypeOf({})
@DefaultQualifierInHierarchyInUncheckedCode()
@DefaultInUncheckedCodeFor({TypeUseLocation.UPPER_BOUND})
// Exceptions are always TOP type, so Throwable must be as well
@ImplicitFor(typeNames = {java.lang.Throwable.class})
@DefaultFor({
    // Allows flow based type refinement in the body of methods
    TypeUseLocation.LOCAL_VARIABLE, // for flow based refinement
    TypeUseLocation.EXCEPTION_PARAMETER, // exceptions are always top
    TypeUseLocation.IMPLICIT_UPPER_BOUND, // <T>, so that T can take on any type in usage
})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface UnknownUnits {}
