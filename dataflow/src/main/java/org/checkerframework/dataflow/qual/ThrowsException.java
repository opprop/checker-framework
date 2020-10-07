package org.checkerframework.dataflow.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code ThrowsException} is a method declaration annotation indicating that a method always throws
 * an exception.
 *
 * <p>The annotation enables flow-sensitive type refinement to be more precise.
 *
 * <p>For example, given a method {@code throwsNullPointerException()} defined as
 *
 * <pre>
 * public throwsNullPointerException() {
 *     throw new NullPointerException();
 * }
 * </pre>
 *
 * then after the following code
 *
 * <pre>
 * if (x == null) {
 *   throwsNullPointerException();
 * }
 * </pre>
 *
 * the Nullness Checker can determine that {@code x} is non-null.
 *
 * <p>The annotation's value represents the type of exception that the method <b>unconditionally</b>
 * throws. The type of the exception can be checked exception, unchecked exception and error.
 * Whichever the case is, Checker Framework always assumes the type specified in {@code
 * ThrowsException} annotation overrides the one specified in the method signature.
 *
 * <p>Note that when the method itself already declares to throw a checked exception, then the type
 * of exception specified in {@code ThrowsException} annotation is required to be
 * <em>compatible</em>, means the type specified in {@code ThrowsException} annotation is a subtype
 * of the one specified in the method declaration. Otherwise Checker Framework issues an error.
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
    Class<? extends Throwable> value();
}
