package org.checkerframework.checker.units.qual;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the arithmetic relationship between (up to) 3 units.
 *
 * <p>Each UnitsRelation defines one relationship.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface UnitsRelation {
    /**
     * operation
     *
     * @return the arithmetic operation for this relation
     */
    Op op();

    /**
     * left-hand argument
     *
     * @return the annotation class of the left-hand argument
     */
    Class<? extends Annotation> lhs();

    /**
     * right-hand argument
     *
     * @return the annotation class of the right-hand argument
     */
    Class<? extends Annotation> rhs();

    /**
     * result
     *
     * @return the annotation class of the result
     */
    Class<? extends Annotation> res();
}
