package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specify the set of relationships between units. Each Relation defines one relationship, and
 * UnitsRelations defines an array of Relations.
 *
 * <p>Duplicate Relation definitions are allowed and may help with improved qualifier documentation,
 * the copies are automatically discarded by Units Checker. However, contradictory relationships are
 * not allowed and will produce exceptions during the execution of Units Checker.
 *
 * @see org.checkerframework.checker.units.qual.Relation
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface UnitsRelations {
    Relation[] value();
}
