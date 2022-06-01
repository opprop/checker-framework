package org.checkerframework.checker.guieffect.qual;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeUseLocation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to override the UI effect on a class, and make a field or method safe for non-UI code
 * to use.
 *
 * @checker_framework.manual #guieffect-checker GUI Effect Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({UI.class})
@DefaultQualifierInHierarchy
@DefaultFor({
    TypeUseLocation.LOCAL_VARIABLE,
    TypeUseLocation.RESOURCE_VARIABLE,
    TypeUseLocation.EXCEPTION_PARAMETER,
    TypeUseLocation.IMPLICIT_UPPER_BOUND
})
public @interface AlwaysSafe {}
