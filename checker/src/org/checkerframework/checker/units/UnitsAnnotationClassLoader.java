package org.checkerframework.checker.units;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.checker.units.qual.UnitsMultiple;
import org.checkerframework.checker.units.qual.UnitsRelation;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.PolymorphicQualifier;
import org.checkerframework.framework.type.AnnotationClassLoader;

/**
 * Annotation Class Loader for the Units Checker. This class is responsible for filtering out
 * aliased units for constructing the supported type qualifiers set, and adding default and
 * user-declared unit relationships.
 */
public final class UnitsAnnotationClassLoader extends AnnotationClassLoader {
    private final BaseTypeChecker checker;
    private final UnitsAnnotatedTypeFactory atf;
    private final UnitsRelationsManager relationsManager;

    private final AnnotationMirror UNKNOWN;
    private final AnnotationMirror DIMENSIONLESS;
    private final AnnotationMirror BOTTOM;

    // Stores a map of the canonical name of a loaded annotation class and its corresponding
    // (non-alias) annotation mirror, this map includes alias and non-alias annotations, unlike
    // ATF's aliasMap.
    private final Map<String, AnnotationMirror> loadedAnnotations = new HashMap<>();

    // Stores the list of explicitly declared unit relationships
    private final List<UnitsRelation> relationshipsList = new ArrayList<>();

    public UnitsAnnotationClassLoader(BaseTypeChecker checker, UnitsAnnotatedTypeFactory atf) {
        super(checker);
        this.checker = checker;
        this.atf = atf;
        relationsManager = atf.relationsManager;

        UNKNOWN = atf.UNKNOWN;
        DIMENSIONLESS = atf.DIMENSIONLESS;
        BOTTOM = atf.BOTTOM;
    }

    /**
     * Custom filter for units annotations:
     *
     * <p>This filter will ignore (by returning false) any units annotation which is an alias of
     * another base unit annotation (identified via {@link UnitsMultiple} meta-annotation). Alias
     * annotations can still be used in source code; they are converted into a base annotation by
     * {@link UnitsAnnotatedTypeFactory#aliasedAnnotation(AnnotationMirror)}. This filter simply
     * makes sure that the alias annotations themselves don't become part of the type hierarchy.
     *
     * <p>The base unit annotation for an alias is also built here and passed to the UnitsATF to be
     * used in {@link UnitsAnnotatedTypeFactory#aliasedAnnotation(AnnotationMirror)}.
     *
     * <p>Any unit relationships declared in meta-annotations of an annotation is extracted here as
     * well and queued for insertion in {@link #addRelationshipsToLoadedUnits()}.
     */
    @Override
    protected boolean isSupportedAnnotationClass(Class<? extends Annotation> annoClass) {
        // See if the annotation is an alias of some other Unit annotation
        UnitsMultiple metaAnno = annoClass.getAnnotation(UnitsMultiple.class);

        if (metaAnno != null) {
            // retrieve the class of the base unit annotation
            Class<? extends Annotation> baseUnitClass = metaAnno.quantity();
            Prefix prefix = metaAnno.prefix();

            // convert the aliased anno into its corresponding prefixed base unit anno
            AnnotationMirror baseUnitAnno = atf.buildBaseUnitAnno(baseUnitClass, prefix);

            // cache prefixed base unit in alias map
            atf.addToAliasMap(annoClass.getCanonicalName(), baseUnitAnno);
            loadedAnnotations.put(annoClass.getCanonicalName(), baseUnitAnno);

            // Add any units relationships declared in the meta-annotations of this unit
            queueStatedUnitRelationships(annoClass);

            // if the annotation is a prefix multiple of some base unit, then return false
            // classic Units checker does not need to load the annotations of prefix multiples of
            // base units
            return false;
        } else {
            // We build without prefixes since prefix.one is removed in type checking
            AnnotationMirror annoMirror =
                    UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, annoClass);

            loadedAnnotations.put(annoClass.getCanonicalName(), annoMirror);

            // Add any units relationships declared in the meta-annotations of this unit
            queueStatedUnitRelationships(annoClass);

            return true;
        }
    }

    /**
     * Queues up relationships declared in meta-annotations for non-polymorphic annotation classes.
     * The declared relationships are added to the relationship maps in {@link
     * #addRelationshipsToLoadedUnits()}.
     *
     * <p>We queue the relationships for two reasons:
     *
     * <p>1) Standard relationships between a unit and {@link UnknownUnits}, {@link Dimensionless},
     * and {@link UnitsBottom} should always be added prior to any stated relationship, in case the
     * user decides to declare inconsistent relations.,
     *
     * <p>2) The units declared in a relationship should also be loaded by the loader. If a unit was
     * stated in a relationship but not loaded, then we will have an inconsistent relations map.
     *
     * @param annoClass
     */
    private void queueStatedUnitRelationships(Class<? extends Annotation> annoClass) {
        // queue up declared relations for non-polymorphic annotation classes
        if (annoClass.getAnnotation(PolymorphicQualifier.class) == null) {
            // queue up relations stated in the UnitsRelations meta annotation
            UnitsRelation[] relationships = annoClass.getAnnotationsByType(UnitsRelation.class);
            relationshipsList.addAll(Arrays.asList(relationships));
        }
    }

    /**
     * Adds default relationships to all loaded units, and all relationships declared in
     * meta-annotations to the relationship maps.
     *
     * @see #addUnitRelationshipsForLoadedClass
     */
    protected void addRelationshipsToLoadedUnits() {
        // Add standard relationships for each loaded qualifier
        for (String loadedAnnotation : loadedAnnotations.keySet()) {
            addUnitRelationshipsForLoadedClass(loadedAnnotations.get(loadedAnnotation));
        }

        // Also add stated relationships in the meta-annotations
        for (UnitsRelation rel : relationshipsList) {
            AnnotationMirror lhs = loadedAnnotations.get(rel.lhs().getCanonicalName());
            AnnotationMirror rhs = loadedAnnotations.get(rel.rhs().getCanonicalName());
            AnnotationMirror res = loadedAnnotations.get(rel.res().getCanonicalName());
            relationsManager.addRelation(rel.op(), lhs, rhs, res);
        }
    }

    /**
     * Adds unit relationships for a loaded annotation class.
     *
     * <p>Default relationships between the loaded class and {@link UnknownUnits}, {@link
     * Dimensionless}, and {@link UnitsBottom} are added first, then explicitly declared
     * relationships given by {@link UnitsRelation} are added.
     *
     * <p>Relationships between time duration and their corresponding time instant units are also
     * added here.
     */
    protected void addUnitRelationshipsForLoadedClass(AnnotationMirror annoMirror) {
        // If the loaded class is not UnknownUnits, Bottom, or Dimensionless, then
        // add standard relationships for the unit
        if (!(UnitsRelationsTools.isSameUnit(annoMirror, UNKNOWN)
                || UnitsRelationsTools.isSameUnit(annoMirror, BOTTOM)
                || UnitsRelationsTools.isSameUnit(annoMirror, DIMENSIONLESS))) {
            relationsManager.addStandardRelations(annoMirror);
        }
    }

    /**
     * Loads, processes and returns all external units qualifiers.
     *
     * @return The externally defined units qualifiers as a mutable set.
     */
    protected Set<Class<? extends Annotation>> getAllExternalUnits() {
        Set<Class<? extends Annotation>> externalUnits = new HashSet<>();
        // load external individually named units
        String qualNames = checker.getOption("units");
        if (qualNames != null) {
            for (String qualName : qualNames.split(",")) {
                Class<? extends Annotation> externalUnit = loadExternalAnnotationClass(qualName);
                if (externalUnit != null) {
                    externalUnits.add(externalUnit);
                }
            }
        }

        // load external directories of units
        String qualDirectories = checker.getOption("unitsDirs");
        if (qualDirectories != null) {
            for (String directoryName : qualDirectories.split(":")) {
                externalUnits.addAll(loadExternalAnnotationClassesFromDirectory(directoryName));
            }
        }

        return externalUnits;
    }
}
