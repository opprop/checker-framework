package org.checkerframework.checker.i18nformatter.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TargetLocations;
import org.checkerframework.framework.qual.TypeUseLocation;

/**
 * The top qualifier.
 *
 * A type annotation indicating that the run-time value might or might not
 * be a valid i18n format string.
 *
 * @checker_framework.manual #i18n-formatter-checker Internationalization
 *                           Format String Checker
 * @author Siwakorn Srisakaokul
 */
@InvisibleQualifier
@SubtypeOf({})
@DefaultQualifierInHierarchy
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@TargetLocations({
    TypeUseLocation.EXPLICIT_LOWER_BOUND,
    TypeUseLocation.EXPLICIT_UPPER_BOUND,
    TypeUseLocation.TYPE_DECLARATION,
    TypeUseLocation.PARAMETER,
    TypeUseLocation.ARRAY_COMPONENT,
    TypeUseLocation.LOCAL_VARIABLE,
    TypeUseLocation.NEW,
    TypeUseLocation.THROWS,
    TypeUseLocation.EXCEPTION_PARAMETER,
    TypeUseLocation.RETURN,
    TypeUseLocation.TYPE_ARGUMENT,
    TypeUseLocation.CAST,
    TypeUseLocation.EXTENDS,
    TypeUseLocation.FIELD,
    TypeUseLocation.INSTANCEOF,
    TypeUseLocation.IMPLEMENTS,
    TypeUseLocation.RECEIVER,
    TypeUseLocation.RESOURCE_VARIABLE
})
public @interface I18nUnknownFormat {}
