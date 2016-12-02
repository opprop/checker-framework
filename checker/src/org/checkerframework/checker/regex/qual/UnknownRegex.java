package org.checkerframework.checker.regex.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TargetLocations;
import org.checkerframework.framework.qual.TypeUseLocation;

/**
 * Represents the top of the Regex qualifier hierarchy.
 *
 * @checker_framework.manual #regex-checker Regex Checker
 */
@InvisibleQualifier
@DefaultQualifierInHierarchy
@SubtypeOf({})
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@TargetLocations({
    TypeUseLocation.EXPLICIT_LOWER_BOUND,
    TypeUseLocation.EXPLICIT_UPPER_BOUND,
    TypeUseLocation.FIELD,
    TypeUseLocation.PARAMETER,
    TypeUseLocation.TYPE_ARGUMENT,
    TypeUseLocation.EXTENDS,
    TypeUseLocation.RECEIVER,
    TypeUseLocation.RETURN,
    TypeUseLocation.THROWS,
    TypeUseLocation.NEW,
    TypeUseLocation.EXCEPTION_PARAMETER,
    TypeUseLocation.RESOURCE_VARIABLE,
    TypeUseLocation.IMPLEMENTS,
    TypeUseLocation.INSTANCEOF,
    TypeUseLocation.ARRAY_COMPONENT,
    TypeUseLocation.LOCAL_VARIABLE,
    TypeUseLocation.EXPLICIT_UPPER_BOUND,
    TypeUseLocation.CAST,
    TypeUseLocation.TYPE_DECLARATION
})
public @interface UnknownRegex {}
