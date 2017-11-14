package org.checkerframework.checker.units.qual;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define the relation between a base unit and the current unit.
 *
 * <p>TODO: add support for factors and more general formulas? E.g. it would be cool if the relation
 * hour &rarr; minute and Fahrenheit &rarr; Celsius could be expressed.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface UnitsMultiple {
    /** @return the base unit to use */
    Class<? extends Annotation> quantity();

    /** @return the scaling prefix */
    Prefix prefix() default Prefix.one;
}
