package org.checkerframework.checker.units;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.PolyUnit;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFormatter;
import org.checkerframework.framework.type.AnnotationClassLoader;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;

/**
 * Annotated type factory for the Units Checker.
 *
 * <p>Handles multiple names for the same unit, with different prefixes, e.g. @kg is the same
 * as @g(Prefix.kilo).
 *
 * <p>Supports relations between units, e.g. if "m" is a variable of type "@m" and "s" is a variable
 * of type "@s", the division "m/s" is automatically annotated as "mPERs", the correct unit for the
 * result.
 */
public class UnitsAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    // Helper constants
    protected final AnnotationMirror UNKNOWN =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, UnknownUnits.class);
    protected final AnnotationMirror DIMENSIONLESS =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, Dimensionless.class);
    protected final AnnotationMirror BOTTOM =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, UnitsBottom.class);
    protected final AnnotationMirror POLYUNIT =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, PolyUnit.class);

    // Helper classes to Units ATF
    protected final UnitsRelationsManager relationsManager =
            new UnitsRelationsManager(checker, this);
    protected final UnitsRelationsEnforcer relationsEnforcer =
            new UnitsRelationsEnforcer(checker, this);

    // Maps fully qualified class names of alias annotation mirrors to their normalized annotation
    // mirrors
    private static final Map<String, AnnotationMirror> aliasMap = new HashMap<>();

    // AnnotationClassLoader for the units checker
    private UnitsAnnotationClassLoader unitsLoader;

    public UnitsAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker, true);
        this.postInit();

        // Within createSupportedTypeQualifiers(), we load all units and instantiate the relations
        // map with some default relationships and any declared relationships present on the
        // annotations, here we fill in the rest the arithmetic relationships with defaults
        relationsManager.completeRelationMaps();

        // TODO (jeff): remove after inference has been implemented as this is debug use only
        // Print CSV file if requested in command line
        // eg javac -processor Units -AwriteCSV=$PWD Manual.java
        // outputs the files in the units folder
        if (checker.hasOption("writeCSV")) {
            String csvFilePath = checker.getOption("writeCSV");
            if (csvFilePath != null) {
                boolean printUU = checker.hasOption("printUU");
                relationsManager.writeCSV(csvFilePath, printUU);
            } else {
                checker.errorAbort(
                        "The writeCSV option must be used with a file path. Ex: -AwriteCSV=$PWD");
            }
        }
    }

    // Annotation print formatting =================================================================
    // In Units Checker, we always want to format the print out of qualifiers by removing Prefix.one
    @Override
    protected AnnotatedTypeFormatter createAnnotatedTypeFormatter() {
        return new UnitsAnnotatedTypeFormatter(checker);
    }

    // Annotation aliasing =========================================================================
    /**
     * Checks to see if the input annotation mirror is an alias unit by checking for a mapping in
     * aliasMap, and if so returns it's equivalent prefixed base unit. Non alias units are returned
     * without any modifications.
     *
     * @param anno A potentially aliased unit annotation mirror.
     * @return The prefixed base unit annotation mirror for alias units, or the input anno for
     *     non-alias units.
     */
    @Override
    public AnnotationMirror aliasedAnnotation(AnnotationMirror anno) {
        // Get the canonical class name of the annotation
        String aname = anno.getAnnotationType().toString();

        // See if we already have a map from this aliased annotation to its
        // corresponding base unit annotation
        if (aliasMap.containsKey(aname)) {
            // if so return it
            return aliasMap.get(aname);
        } else {
            // For any other annotations that are encountered which are not
            // supported qualifiers, map it for faster future retrieval
            aliasMap.put(aname, super.aliasedAnnotation(anno));

            return anno;
        }
    }

    /**
     * Adds a mapping from an alias unit's canonical class name to its equivalent prefixed base
     * unit's AnnotationMirror to the aliasMap.
     *
     * @param aliasAnnoCanonicalClassName the canonical class name of an alias unit
     * @param normalizedAnno AnnotationMirror of the equivalent prefixed base unit of the alias unit
     */
    protected void addToAliasMap(
            String aliasAnnoCanonicalClassName, AnnotationMirror normalizedAnno) {
        if (!aliasMap.containsKey(aliasAnnoCanonicalClassName)) {
            aliasMap.put(aliasAnnoCanonicalClassName, normalizedAnno);
        }
    }

    /**
     * Constructs an annotation mirror of the given base unit, with the given prefix.
     *
     * @param baseUnitAnnoClass Annotation class of the base unit.
     * @param prefix {@link Prefix} of the unit.
     * @return An annotation mirror built using the base unit and the prefix.
     */
    protected AnnotationMirror buildBaseUnitAnno(
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

    // Supported type qualifiers ===================================================================
    // In Units Checker, we support and load additional qualifiers defined by the user, and we
    // instantiate units relationship tables defined in the meta-annotations of the qualifiers.
    @Override
    protected AnnotationClassLoader createAnnotationClassLoader() {
        // Use the UnitsAnnotationClassLoader instead of the default one
        unitsLoader = new UnitsAnnotationClassLoader(checker, this);
        return unitsLoader;
    }

    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        // get all bundled annotations
        Set<Class<? extends Annotation>> qualSet = getBundledTypeQualifiersWithPolyAll();

        // load all the external units
        qualSet.addAll(unitsLoader.getAllExternalUnits());

        // add relations to all loaded units
        unitsLoader.addRelationshipsToLoadedUnits();

        return qualSet;
    }

    // Tree Annotators =============================================================================
    /**
     * Override to use {@link ImplicitsTreeAnnotator} for the null literal and void class, and
     * {@link UnitsPropagationTreeAnnotator} to type check and propagate units.
     */
    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                new ImplicitsTreeAnnotator(this), new UnitsPropagationTreeAnnotator(this));
    }

    // Qualifier Hierarchy =========================================================================
    /**
     * Use custom qualifier hierarchy to programmatically set bottom of hierarchy and define subtype
     * and LUB relations for units.
     */
    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory mgf) {
        return new UnitsQualifierHierarchy(mgf, this);
    }
}
