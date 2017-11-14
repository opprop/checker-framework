package org.checkerframework.checker.units.qual;

import static org.checkerframework.framework.qual.TypeUseLocation.*;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * UnknownUnits is the top type of the type hierarchy.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
// Note: unit relations for UnknownUnits are programatically added
@SubtypeOf({})
// @DefaultQualifierInHierarchyInUncheckedCode()
// @DefaultInUncheckedCodeFor({TypeUseLocation.PARAMETER, TypeUseLocation.UPPER_BOUND})
@ImplicitFor(typeNames = {Throwable.class, Exception.class})
@DefaultFor({
    // Allows flow based type refinement in the body of methods
    LOCAL_VARIABLE, // for flow based refinement
    EXCEPTION_PARAMETER, // exceptions are always top
    IMPLICIT_UPPER_BOUND, // <T>, so that T can take on any type in usage
    // RECEIVER, // so that methods defined in classes can be invoked on objects with units
})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER}) // ElementType.TYPE,
public @interface UnknownUnits {}
