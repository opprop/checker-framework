package org.checkerframework.dataflow.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code ThrowException} is a method annotation that indicates that a method always throws an
 * exception.
 *
 * <p>The annotation enables flow-sensitive type refinement to be more precise. For example, after
 *
 * <pre>
 * if (x == null) {
 *   buildAndThrowNullPointerException();
 * }
 * </pre>
 *
 * where method {@code buildAndThrowNullPointerException()} is defined as
 *
 * <pre>
 * public buildAndThrowNullPointerException() {
 *     throw new NullPointerException();
 * }
 * </pre>
 *
 * the Nullness Checker can determine that {@code x} is non-null.
 *
 * <p>
 *
 * <p>The annotation's value represents the type of exception that the method throws. By default,
 * the type is {@code Throwable}.
 *
 * <p>The annotation is a <em>trusted</em> annotation, meaning that it is not checked whether the
 * annotated method really unconditionally throws an exception.
 *
 * <p>This annotation is inherited by subtypes, just as if it were meta-annotated with
 * {@code @InheritedAnnotation}.
 */
// @InheritedAnnotation cannot be written here, because "dataflow" project cannot depend on
// "framework" project.
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface ThrowsException {
    Class<? extends Throwable> value() default Throwable.class;
}
