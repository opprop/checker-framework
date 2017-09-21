package org.checkerframework.checker.units.qual;

import java.lang.annotation.Annotation;

/**
 * Specifies the arithmetic relationship between (up to) 3 units.
 *
 * <p>Each Relation defines one relationship.
 */
public @interface Relation {
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
