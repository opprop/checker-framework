package org.checkerframework.checker.units;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.UnitsMultiple;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.javacutil.AnnotationUtils;

/**
 * Alias Manager for the Units Checker.
 *
 * <p>This class builds and retains a mapping between alias annotations and their normalized
 * annotations. Alias units are mapped to their prefixed base units.
 *
 * <p>This class is a singleton class.
 */
public class UnitsAliasManager {
    private static UnitsAliasManager instance = null;

    private static final Map<String, AnnotationMirror> aliasMap = new HashMap<>();

    private final ProcessingEnvironment processingEnv;
    private final Elements elements;

    protected static UnitsAliasManager getInstance(BaseTypeChecker checker) {
        if (instance == null) {
            instance = new UnitsAliasManager(checker);
        }
        return instance;
    }

    private UnitsAliasManager(BaseTypeChecker checker) {
        this.processingEnv = checker.getProcessingEnvironment();
        this.elements = checker.getElementUtils();
    }

    /**
     * Checks to see if the input annotation class is an alias unit, and if so computes and returns
     * it's equivalent prefixed base unit as an annotation mirror.
     *
     * @param annoClass A potentially aliased unit annotation class.
     * @return The prefixed base unit annotation mirror for alias units, or null if it is not an
     *     alias unit.
     */
    protected AnnotationMirror getAliasAnnotation(Class<? extends Annotation> annoClass) {
        // Get the canonical class name of the annotation
        String aname = annoClass.getCanonicalName();

        // See if we already have a map from this aliased annotation to its
        // corresponding base unit annotation
        if (aliasMap.containsKey(aname)) {
            // if so return it
            return aliasMap.get(aname);
        }

        // If not, look for the UnitsMultiple meta annotations of this aliased
        // annotation
        UnitsMultiple metaAnno = annoClass.getAnnotation(UnitsMultiple.class);

        // If it exists, build the base unit annotation mirror with a prefix
        if (metaAnno != null) {
            Class<? extends Annotation> baseUnitAnnoClass = metaAnno.quantity();
            Prefix prefix = metaAnno.prefix();

            // Build the annotation mirror
            AnnotationMirror result = buildBaseUnitAnno(baseUnitAnnoClass, prefix);

            // Add this to the alias map, then return the annotation mirror
            aliasMap.put(aname, result);
            return result;
        }

        return null;
    }

    /**
     * Checks to see if the input annotation mirror is an alias unit, and if so computes and returns
     * it's equivalent prefixed base unit. Non alias units are returned without any modifications.
     *
     * @param anno A potentially aliased unit annotation mirror.
     * @return The prefixed base unit annotation mirror for alias units, or the input anno for
     *     non-alias units.
     */
    protected AnnotationMirror getAliasAnnotation(AnnotationMirror anno) {
        // Get the canonical class name of the annotation
        String aname = anno.getAnnotationType().toString();

        // See if we already have a map from this aliased annotation to its
        // corresponding base unit annotation
        if (aliasMap.containsKey(aname)) {
            // if so return it
            return aliasMap.get(aname);
        }

        // If not, look for the UnitsMultiple meta annotations of this aliased
        // annotation
        for (AnnotationMirror metaAnno :
                anno.getAnnotationType().asElement().getAnnotationMirrors()) {

            // If it exists, build the base unit annotation mirror with a prefix
            if (UnitsRelationsTools.isUnitsMultiple(metaAnno)) {
                // Retrieve the Class of the base unit annotation
                Class<? extends Annotation> baseUnitAnnoClass =
                        AnnotationUtils.getElementValueClass(metaAnno, "quantity", true)
                                .asSubclass(Annotation.class);

                // Retrieve the prefix of the aliased annotation
                Prefix prefix =
                        AnnotationUtils.getElementValueEnum(metaAnno, "prefix", Prefix.class, true);

                // Build the annotation mirror
                AnnotationMirror result = buildBaseUnitAnno(baseUnitAnnoClass, prefix);

                // Add this to the alias map, then return the annotation mirror
                aliasMap.put(aname, result);

                return result;
            }
        }

        // For any other annotations that are encountered which are not
        // supported qualifiers, map it to null for faster future retrieval
        aliasMap.put(aname, anno);
        return anno;
    }

    /**
     * Constructs an annotation mirror of the given base unit, with the given prefix.
     *
     * @param baseUnitAnnoClass Annotation class of the base unit.
     * @param prefix {@link Prefix} of the unit.
     * @return An annotation mirror built using the base unit and the prefix.
     */
    private AnnotationMirror buildBaseUnitAnno(
            Class<? extends Annotation> baseUnitAnnoClass, Prefix prefix) {
        // Build a base unit annotation with the prefix applied
        AnnotationMirror result =
                UnitsRelationsTools.buildAnnoMirrorWithSpecificPrefix(
                        processingEnv, baseUnitAnnoClass, prefix);

        // Aliases shouldn't have Prefix.one, but if it does then clean it
        // up here
        if (UnitsRelationsTools.getPrefixValue(result) == Prefix.one) {
            result = UnitsRelationsTools.removePrefix(elements, result);
        }

        return result;
    }
}
