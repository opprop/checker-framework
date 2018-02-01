package org.checkerframework.checker.units;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;

/**
 * Units Qualifier Hierarchy programmatically defines {@link UnitsBottom} as the bottom of the
 * hierarchy, and defines the subtype and LUB relationships for prefixed units.
 */
public class UnitsQualifierHierarchy extends GraphQualifierHierarchy {
    private final Elements elements;

    public UnitsQualifierHierarchy(MultiGraphFactory mgf, UnitsAnnotatedTypeFactory atf) {
        super(mgf, atf.BOTTOM);
        this.elements = atf.getElementUtils();
    }

    /** Checks to see if a1 is subtype of a2, accounting for unit prefixes */
    @Override
    public boolean isSubtype(AnnotationMirror a1, AnnotationMirror a2) {
        // If the prefix is Prefix.one, automatically strip it for LUB checking
        if (UnitsRelationsTools.getPrefixValue(a1) == Prefix.one) {
            a1 = UnitsRelationsTools.removePrefix(elements, a1);
        }
        if (UnitsRelationsTools.getPrefixValue(a2) == Prefix.one) {
            a2 = UnitsRelationsTools.removePrefix(elements, a2);
        }

        // See if the two units have the same base unit
        if (AnnotationUtils.areSameIgnoringValues(a1, a2)) {
            // If so, return whether they are the exact same prefixed unit
            return AnnotationUtils.areSame(a1, a2);
        } else {
            // If not, check using super
            a1 = UnitsRelationsTools.removePrefix(elements, a1);
            a2 = UnitsRelationsTools.removePrefix(elements, a2);

            // super call can only check using annotation mirrors in the supported type qualifier
            // hierarchy, which must be non-prefixed units
            return super.isSubtype(a1, a2);
        }
    }

    /**
     * Computes the LUB of two Units.
     *
     * <p>Alias annotations are not placed in the Supported Type Qualifiers set, instead, their base
     * units are in the set. Whenever an alias annotation or prefix-multiple of a base unit is used
     * we handle the LUB resolution here so we can correctly compute a LUB Unit.
     */
    @Override
    public AnnotationMirror leastUpperBound(AnnotationMirror a1, AnnotationMirror a2) {
        // If the prefix is Prefix.one, automatically strip it for LUB checking
        if (UnitsRelationsTools.getPrefixValue(a1) == Prefix.one) {
            a1 = UnitsRelationsTools.removePrefix(elements, a1);
        }
        if (UnitsRelationsTools.getPrefixValue(a2) == Prefix.one) {
            a2 = UnitsRelationsTools.removePrefix(elements, a2);
        }

        // Compute base units
        AnnotationMirror baseA1 = UnitsRelationsTools.removePrefix(elements, a1);
        AnnotationMirror baseA2 = UnitsRelationsTools.removePrefix(elements, a2);

        if (UnitsRelationsTools.isSameUnit(baseA1, baseA2)) {
            // If the two units have the same base unit
            if (UnitsRelationsTools.isSameUnit(a1, a2)) {
                // And if they have the same Prefix, it means it is the same unit, so return the
                // unit
                return a1;
            } else {
                // If they don't have the same Prefix, find the LUB:
                // Check if a1 is a prefixed multiple of a base unit
                boolean a1Prefixed = UnitsRelationsTools.hasPrefixValue(a1);
                // Check if a2 is a prefixed multiple of a base unit
                boolean a2Prefixed = UnitsRelationsTools.hasPrefixValue(a2);
                // Obtain a1 and a2's direct supertypes
                AnnotationMirror a1Super = getDirectSupertype(baseA1);
                AnnotationMirror a2Super = getDirectSupertype(baseA2);

                // findLub() only works with base units, so we use the direct supertype for any
                // prefixed unit
                if (a1Prefixed && a2Prefixed) {
                    // if both are prefixed, find the LUB of their direct supertypes
                    // eg LUB(@km, @km) == LUB(@Length, @Length) = @Length
                    return findLub(a1Super, a2Super);
                } else if (a1Prefixed && !a2Prefixed) {
                    // if only the left is prefixed, find LUB of (supertype of a1) and a2
                    // eg LUB(@km, @m) == LUB(@Length, @m) = @Length
                    return findLub(a1Super, a2);
                } else {
                    // else (only right is prefixed), find LUB of a1 and (supertype of a2)
                    // eg LUB(@m, @km) == LUB(@m, @Length) = @Length
                    return findLub(a1, a2Super);
                }
            }
        } else {
            // if they don't have the same base unit, user super
            return super.leastUpperBound(a1, a2);
        }
    }

    /**
     * Obtains the direct supertype of a unit.
     *
     * <p>Climbs the supertypes graph to get the direct supertype of unit.
     *
     * @param unit An annotation mirror.
     * @return the supertype of unit as an annotation mirror.
     */
    private AnnotationMirror getDirectSupertype(AnnotationMirror unit) {
        for (AnnotationMirror key : supertypesGraph.keySet()) {
            if (AnnotationUtils.areSame(key, unit)) {
                // each unit has exactly 1 super type
                return supertypesGraph.get(key).iterator().next();
            }
        }
        return null;
    }
}
