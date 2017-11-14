package org.checkerframework.checker.units;

import com.sun.tools.javac.code.Type.MethodType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.UnitsMultiple;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

/**
 * Utility class providing numerous static methods which help process AnnotationMirrors and
 * AnnotatedTypeMirrors representing various units.
 */
public class UnitsRelationsTools {
    /**
     * Checks to see if the input annotation mirror is the {@link UnitsMultiple} meta annotation.
     *
     * @param metaAnno An annotation mirror.
     * @return True if the input annotation mirror is the {@link UnitsMultiple} meta annotation,
     *     false otherwise.
     */
    public static boolean isUnitsMultiple(AnnotationMirror metaAnno) {
        return AnnotationUtils.areSameByClass(metaAnno, UnitsMultiple.class);
    }

    /**
     * Checks to see if the input annotation mirror is a prefixed base unit.
     *
     * @param unitsAnnotation An annotation mirror representing a unit.
     * @return True if the input annotation mirror is a prefixed base unit, false otherwise.
     */
    public static boolean isPrefixedUnit(@Nullable final AnnotationMirror unitsAnnotation) {
        return hasPrefixValue(unitsAnnotation);
    }

    /**
     * Checks to see if the input annotation mirror is a non-prefixed base unit.
     *
     * @param unitsAnnotation An annotation mirror representing a unit.
     * @return True if the input annotation mirror is a non-prefixed base unit, false otherwise.
     */
    public static boolean isBaseUnit(@Nullable final AnnotationMirror unitsAnnotation) {
        return hasPrefixField(unitsAnnotation) && !isPrefixedUnit(unitsAnnotation);
    }

    /**
     * Creates an AnnotationMirror representing a unit defined by annoClass. If the annoClass allows
     * for a prefix it is built with the default Prefix of Prefix.one, otherwise it is built without
     * any prefixes.
     *
     * @param env The Checker Processing Environment, provided as a parameter in init() of a
     *     UnitsRelations implementation.
     * @param annoClass The Class of an Annotation representing a Unit (eg m.class for meters).
     * @return An AnnotationMirror of the Unit (with Prefix.one if allowed), or null if it cannot be
     *     constructed.
     */
    public static @Nullable AnnotationMirror buildAnnoMirrorWithDefaultPrefix(
            final ProcessingEnvironment env, final Class<? extends Annotation> annoClass) {
        if (env == null || annoClass == null) {
            return null;
        }

        boolean hasPrefix = false;
        for (Method m : annoClass.getMethods()) {
            if (m.getReturnType().equals(Prefix.class)) {
                hasPrefix = true;
                break;
            }
        }

        if (hasPrefix) {
            return buildAnnoMirrorWithSpecificPrefix(env, annoClass, Prefix.one);
        } else {
            return buildAnnoMirrorWithNoPrefix(env, annoClass);
        }
    }

    /**
     * Creates an AnnotationMirror representing a unit defined by annoClass, with the specific
     * Prefix p.
     *
     * @param env The Checker Processing Environment, provided as a parameter in init() of a
     *     UnitsRelations implementation.
     * @param annoClass The Class of an Annotation representing a Unit (eg m.class for meters).
     * @param p A Prefix value.
     * @return An AnnotationMirror of the Unit with the Prefix p, or null if it cannot be
     *     constructed.
     */
    public static @Nullable AnnotationMirror buildAnnoMirrorWithSpecificPrefix(
            final ProcessingEnvironment env,
            final Class<? extends Annotation> annoClass,
            final Prefix p) {
        return buildAnnoMirrorWithSpecificPrefix(env, annoClass.getCanonicalName(), p);
    }

    /**
     * Creates an AnnotationMirror representing a unit defined by canonical class name
     * annoClassName, with the specific Prefix p.
     *
     * @param env The Checker Processing Environment, provided as a parameter in init() of a
     *     UnitsRelations implementation.
     * @param annoClassName The canonical class name of an Annotation representing a Unit (eg
     *     org.checkerframework.checker.units.qual.m for meters).
     * @param p A Prefix value.
     * @return An AnnotationMirror of the Unit with the Prefix p, or null if it cannot be
     *     constructed.
     */
    public static @Nullable AnnotationMirror buildAnnoMirrorWithSpecificPrefix(
            final ProcessingEnvironment env, final String annoClassName, final Prefix p) {
        if (env == null || annoClassName == null || p == null) {
            return null;
        }

        AnnotationBuilder builder = new AnnotationBuilder(env, annoClassName);
        builder.setValue("value", p);
        return builder.build();
    }

    /**
     * Creates an AnnotationMirror representing a unit defined by annoClass, with no prefix.
     *
     * @param env The checker Processing Environment, provided as a parameter in init() of a
     *     UnitsRelations implementation.
     * @param annoClass The Class of an Annotation representing a Unit (eg m.class for meters).
     * @return An AnnotationMirror of the Unit with no prefix, or null if it cannot be constructed.
     */
    public static @Nullable AnnotationMirror buildAnnoMirrorWithNoPrefix(
            final ProcessingEnvironment env, final Class<? extends Annotation> annoClass) {
        if (env == null || annoClass == null) {
            return null;
        }

        return AnnotationBuilder.fromClass(env.getElementUtils(), annoClass);
    }

    /**
     * Checks to see if a unit annotation has a properly defined Prefix field.
     *
     * @param unitsAnnotation An annotation mirror.
     * @return True if it has a properly defined Prefix field.
     */
    public static boolean hasPrefixField(@Nullable final AnnotationMirror unitsAnnotation) {
        if (unitsAnnotation != null) {
            // A unit has the prefix field if one of it's enclosing elements is a method with type
            // Prefix and method name value()
            for (Element ele :
                    unitsAnnotation.getAnnotationType().asElement().getEnclosedElements()) {
                if (ele.getKind() == ElementKind.METHOD) {
                    MethodType mt = (MethodType) ele.asType();

                    return (mt.getReturnType()
                                    .toString()
                                    .contentEquals(Prefix.class.getCanonicalName())
                            && ele.toString().contentEquals("value()"));
                }
            }
        }
        return false;
    }

    /**
     * Retrieves the Prefix of an Annotated Type.
     *
     * @param annoType An AnnotatedTypeMirror representing a Units Annotated Type.
     * @return A Prefix value (including Prefix.one), or null if it has none.
     */
    public static @Nullable Prefix getPrefixValue(final AnnotatedTypeMirror annoType) {
        if (annoType == null) {
            return null;
        }

        Prefix result = null;

        // Go through each Annotation of an Annotated Type, find the prefix and return it
        for (AnnotationMirror mirror : annoType.getAnnotations()) {
            // Try to get a prefix
            result = getPrefixValue(mirror);
            // If it is not null, then return the retrieved prefix immediately
            if (result != null) {
                return result;
            }
        }

        // If we can not find any prefix at all, then return null
        return result;
    }

    /**
     * Retrieves the Prefix of an AnnotationMirror.
     *
     * @param unitsAnnotation An AnnotationMirror representing a Unit.
     * @return A Prefix value (including Prefix.one), or null if it has none.
     */
    public static @Nullable Prefix getPrefixValue(
            @Nullable final AnnotationMirror unitsAnnotation) {
        AnnotationValue annotationValue = getAnnotationMirrorPrefix(unitsAnnotation);

        // If this Annotation has no prefix, return null
        if (annotationValue == null) {
            return null;
        }

        // if the Annotation has a value, then detect and match the string name of the prefix, and return the matching Prefix
        String prefixString = annotationValue.getValue().toString();
        for (Prefix prefix : Prefix.values()) {
            if (prefixString.equals(prefix.toString())) {
                return prefix;
            }
        }

        // if none of the strings match, then return null
        return null;
    }

    /**
     * Checks to see if an Annotated Type has a prefix.
     *
     * @param annoType An AnnotatedTypeMirror representing a Units Annotated Type.
     * @return True if it has a prefix, false otherwise.
     */
    public static boolean hasPrefixValue(@Nullable final AnnotatedTypeMirror annoType) {
        if (annoType == null) {
            return false;
        }

        for (AnnotationMirror mirror : annoType.getAnnotations()) {
            // if any Annotation has a prefix, return true
            if (hasPrefixValue(mirror)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks to see if an Annotation has a prefix.
     *
     * @param unitsAnnotation An AnnotationMirror representing a Units Annotation.
     * @return True if it has a prefix, false otherwise.
     */
    public static boolean hasPrefixValue(@Nullable final AnnotationMirror unitsAnnotation) {
        AnnotationValue annotationValue = getAnnotationMirrorPrefix(unitsAnnotation);
        return annotationValue != null;
    }

    /**
     * Given an Annotation, returns the prefix (eg kilo) as an AnnotationValue if there is any,
     * otherwise returns null.
     */
    private static @Nullable AnnotationValue getAnnotationMirrorPrefix(
            @Nullable final AnnotationMirror unitsAnnotation) {
        if (unitsAnnotation == null) {
            return null;
        }

        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
                unitsAnnotation.getElementValues();

        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                elementValues.entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals("value")) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Removes the Prefix value from an Annotation, by constructing and returning a copy of its base
     * unit's Annotation.
     *
     * @param elements The Element Utilities from a checker's processing environment, typically
     *     obtained by calling env.getElementUtils() in init() of a Units Relations implementation.
     * @param unitsAnnotation An AnnotationMirror representing a Units Annotation.
     * @return The base Unit's AnnotationMirror, or null if the base Unit cannot be constructed.
     */
    public static @Nullable AnnotationMirror removePrefix(
            @Nullable final Elements elements, @Nullable final AnnotationMirror unitsAnnotation) {
        if (elements == null) {
            return null;
        }

        if (!hasPrefixValue(unitsAnnotation)) {
            return unitsAnnotation;
        } else {
            // The only value is the prefix value in Units Checker
            //
            // TODO: refine sensitivity of removal for extension units, in case extension
            // Annotations have more than just Prefix in its values. Do this by building a fresh
            // annotation and copy only non Prefix element values.
            return AnnotationBuilder.fromName(
                    elements, unitsAnnotation.getAnnotationType().toString());
        }
    }

    /**
     * Removes the Prefix value from an Annotated Type, by constructing and returning a copy of the
     * Annotated Type without the prefix.
     *
     * @param elements The Element Utilities from a checker's processing environment, typically
     *     obtained by calling env.getElementUtils() in init() of a Units Relations implementation.
     * @param annoType An AnnotatedTypeMirror representing a Units Annotated Type.
     * @return A copy of the Annotated Type without the prefix.
     */
    public static AnnotatedTypeMirror removePrefix(
            @Nullable final Elements elements, @Nullable final AnnotatedTypeMirror annoType) {
        // Deep copy the Annotated Type Mirror without any of the Annotations
        AnnotatedTypeMirror result = annoType.deepCopy(false);

        // Get all of the original Annotations in the Annotated Type
        Set<AnnotationMirror> annos = annoType.getAnnotations();

        // Loop through all the Annotations to see if they use Prefix.one, remove Prefix.one if it
        // does
        for (AnnotationMirror anno : annos) {
            // Try to clean the Annotation Mirror of the Prefix
            AnnotationMirror cleanedMirror = removePrefix(elements, anno);
            if (cleanedMirror != null) {
                // If successful, add the cleaned annotation to the deep copy
                result.addAnnotation(cleanedMirror);
            } else {
                // If unsuccessful, add the original annotation
                result.addAnnotation(anno);
            }
        }

        return result;
    }

    /**
     * Checks to see if a particular Annotated Type is {@link Dimensionless}.
     *
     * @param annoType An AnnotatedTypeMirror representing a Units Annotated Type.
     * @return True if the Type has no units, false otherwise.
     */
    public static boolean isDimensionless(@Nullable final AnnotatedTypeMirror annoType) {
        if (annoType == null) {
            return false;
        }

        return (annoType.getAnnotation(Dimensionless.class) != null);
    }

    /**
     * Checks to see if a particular Annotated Type has a specific unit (represented by its
     * Annotation).
     *
     * @param annoType An AnnotatedTypeMirror representing a Units Annotated Type.
     * @param unitsAnnotation An AnnotationMirror representing a Units Annotation of a specific
     *     unit.
     * @return True if the Type has the specific unit, false otherwise.
     */
    public static boolean hasSpecificUnit(
            @Nullable final AnnotatedTypeMirror annoType,
            @Nullable final AnnotationMirror unitsAnnotation) {
        if (annoType == null || unitsAnnotation == null) {
            return false;
        }

        return AnnotationUtils.containsSame(annoType.getAnnotations(), unitsAnnotation);
    }

    /**
     * Checks to see if a particular Annotated Type has a particular base unit (represented by its
     * Annotation).
     *
     * @param annoType An AnnotatedTypeMirror representing a Units Annotated Type.
     * @param unitsAnnotation An AnnotationMirror representing a Units Annotation of the base unit.
     * @return True if the Type has the specific unit, false otherwise.
     */
    public static boolean hasSpecificUnitIgnoringPrefix(
            @Nullable final AnnotatedTypeMirror annoType,
            @Nullable final AnnotationMirror unitsAnnotation) {
        if (annoType == null || unitsAnnotation == null) {
            return false;
        }

        return AnnotationUtils.containsSameIgnoringValues(
                annoType.getAnnotations(), unitsAnnotation);
    }

    /**
     * Checks to see if two units are the same.
     *
     * @param unit1 An AnnotationMirror representing one unit.
     * @param unit2 An AnnotationMirror representing another unit.
     * @return True if the units are the same unit.
     */
    public static boolean isSameUnit(final AnnotationMirror unit1, final AnnotationMirror unit2) {
        return AnnotationUtils.areSame(unit1, unit2);
    }

    private static final Map<String, AnnotationMirror> directSuperTypeMap =
            new HashMap<String, AnnotationMirror>();

    /**
     * Obtains the direct supertype of the input unit if it exists, by constructing it from the
     * class specified in the {@link SubtypeOf} meta annotation.
     *
     * @param env The Checker Processing Environment, provided as a parameter in init() of a
     *     UnitsRelations implementation.
     * @param unitsAnnotation An AnnotationMirror representing a unit.
     * @return A freshly constructed AnnotationMirror representing the supertype of the given unit,
     *     or null if no such unit exists.
     */
    // This is used in UnitsRelationsManager during createSupportedTypeQualifiers, where the qual
    // hierarchy isn't completely defined. If we discard all category units, then we can remove this
    // method and simply use the ATF qual hierarchy. See
    // UnitsRelationsManager.addStandardRelations().
    public static AnnotationMirror getDirectSupertype(
            final ProcessingEnvironment env, @Nullable final AnnotationMirror unitsAnnotation) {
        if (unitsAnnotation != null) {
            String unitName = unitsAnnotation.toString();

            if (directSuperTypeMap.containsKey(unitName)) {
                return directSuperTypeMap.get(unitName);
            }

            // loop through the meta-annotations of the annotation, look for
            // @SubtypeOf()
            for (AnnotationMirror metaAnno :
                    unitsAnnotation.getAnnotationType().asElement().getAnnotationMirrors()) {
                if (AnnotationUtils.areSameByClass(metaAnno, SubtypeOf.class)) {
                    // retrieve the Class name of the supertype unit annotation
                    List<com.sun.tools.javac.code.Type> superUnitClasses =
                            AnnotationUtils.getElementValueArray(
                                    metaAnno, "value", com.sun.tools.javac.code.Type.class, true);
                    if (superUnitClasses != null && !superUnitClasses.isEmpty()) {
                        // build and return an annotation using the class name
                        // In units checker, each unit can have at most 1 superunit
                        AnnotationMirror superUnit =
                                new AnnotationBuilder(
                                                env, superUnitClasses.iterator().next().toString())
                                        .build();
                        directSuperTypeMap.put(unitName, superUnit);
                        return directSuperTypeMap.get(unitName);
                    }
                }
            }
        }

        return null;
    }
}
