package org.checkerframework.checker.units;

import java.lang.annotation.Annotation;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.Op;
import org.checkerframework.checker.units.qual.Relation;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.checker.units.qual.UnitsMultiple;
import org.checkerframework.checker.units.qual.UnitsRelations;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.checker.units.qual.time.TimeRelation;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.PolymorphicQualifier;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotationClassLoader;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

/**
 * Annotation Class Loader for the Units Checker. This class is responsible for filtering out
 * prefixed units for constructing the supported type qualifiers set, and adding default and
 * user-declared unit relationships.
 */
public final class UnitsAnnotationClassLoader extends AnnotationClassLoader {
    private final BaseTypeChecker checker;
    private final UnitsRelationsManager relations;

    private final AnnotationMirror UNKNOWN;
    private final AnnotationMirror DIMENSIONLESS;
    private final AnnotationMirror BOTTOM;

    public UnitsAnnotationClassLoader(BaseTypeChecker checker, AnnotatedTypeFactory atf) {
        super(checker);
        this.checker = checker;

        UnitsMirrors mirrors = UnitsMirrors.getInstance(checker);
        UNKNOWN = mirrors.UNKNOWN;
        DIMENSIONLESS = mirrors.DIMENSIONLESS;
        BOTTOM = mirrors.BOTTOM;

        relations = UnitsRelationsManager.getInstance(checker, atf);
    }

    /**
     * Adds unit relationships for a loaded annotation class.
     *
     * <p>Default relationships between the loaded class and {@link UnknownUnits}, {@link
     * Dimensionless}, and {@link UnitsBottom} are added first, then any explicitly declared
     * relationships given by {@link UnitsRelations} is added.
     *
     * <p>Relationships between time duration and their corresponding time instant units are also
     * added here.
     */
    @Override
    protected void postProcessLoadedClass(Class<? extends Annotation> annoClass) {
        AnnotationMirror annoMirror = relations.buildAnnoMirror(annoClass);

        // Add relations for non-polymorphic annotation classes
        if (annoClass.getAnnotation(PolymorphicQualifier.class) == null) {

            // If a TimeRelation meta annotation is present, add the specific
            // relationships between the duration and instant units
            TimeRelation tr = annoClass.getAnnotation(TimeRelation.class);
            if (tr != null) {
                // Add time unit relations
                AnnotationMirror duration = relations.buildAnnoMirror(tr.duration());
                AnnotationMirror instant = relations.buildAnnoMirror(tr.instant());

                relations.addDirectRelation(Op.ADD, duration, duration, duration);
                relations.addDirectRelation(Op.SUB, duration, duration, duration);

                relations.addDirectRelation(Op.ADD, instant, instant, UNKNOWN);
                relations.addDirectRelation(Op.SUB, instant, instant, duration);

                relations.addDirectRelation(Op.ADD, duration, instant, instant);
                relations.addDirectRelation(Op.SUB, duration, instant, UNKNOWN);

                relations.addDirectRelation(Op.ADD, instant, duration, instant);
                relations.addDirectRelation(Op.SUB, instant, duration, instant);
            }

            // If the loaded class is not UnknownUnits, Bottom, or Dimensionless, then
            // add standard relationships for the unit
            if (!(UnitsRelationsTools.isSameUnit(annoMirror, UNKNOWN)
                    || UnitsRelationsTools.isSameUnit(annoMirror, BOTTOM)
                    || UnitsRelationsTools.isSameUnit(annoMirror, DIMENSIONLESS))) {
                relations.addStandardRelations(annoMirror);
            }

            // Add relations stated in the UnitsRelations meta annotation
            UnitsRelations relationsAnno = annoClass.getAnnotation(UnitsRelations.class);
            if (relationsAnno != null) {
                for (Relation rel : relationsAnno.value()) {
                    // add the relation
                    relations.addRelation(rel.op(), rel.lhs(), rel.rhs(), rel.res());
                }
            }
        }
    }

    /**
     * Custom filter for units annotations:
     *
     * <p>This filter will ignore (by returning false) any units annotation which is an alias of
     * another base unit annotation (identified via {@link UnitsMultiple} meta-annotation). Alias
     * annotations can still be used in source code; they are converted into a base annotation by
     * {@link UnitsAnnotatedTypeFactory#aliasedAnnotation(AnnotationMirror)}. This filter simply
     * makes sure that the alias annotations themselves don't become part of the type hierarchy.
     * Further, it will ensure that their base annotations are in the hierarchy.
     */
    @Override
    protected boolean isSupportedAnnotationClass(Class<? extends Annotation> annoClass) {
        // build the initial annotation mirror (missing prefix)
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, annoClass);
        AnnotationMirror initialResult = builder.build();

        // further refine to see if the annotation is an alias of some other Unit annotation
        for (AnnotationMirror metaAnno :
                initialResult.getAnnotationType().asElement().getAnnotationMirrors()) {
            // if the annotation is a prefix multiple of some base unit, then return false
            // classic Units checker does not need to load the annotations of prefix multiples of
            // base units
            if (AnnotationUtils.areSameByClass(metaAnno, UnitsMultiple.class)) {

                // ensure it has a base unit
                Class<? extends Annotation> baseUnitClass = getBaseUnitAnnoClass(initialResult);
                // if the base unit isn't already loaded, then load the base unit
                String baseUnitClassName = baseUnitClass.getCanonicalName();

                if (!getLoadedAnnotationClasses().contains(baseUnitClass)) {
                    loadAnnotationClass(baseUnitClassName);
                }

                return false;
            }
        }

        // Not an alias unit
        return true;
    }

    /**
     * Obtains the base unit annotation class of an alias annotation by inspecting its {@link
     * UnitsMultiple} meta annotation.
     */
    private @Nullable Class<? extends Annotation> getBaseUnitAnnoClass(AnnotationMirror anno) {
        // loop through the meta annotations of the annotation, look for UnitsMultiple
        for (AnnotationMirror metaAnno :
                anno.getAnnotationType().asElement().getAnnotationMirrors()) {
            // see if the meta annotation is UnitsMultiple
            if (UnitsRelationsTools.isUnitsMultiple(metaAnno)) {
                // retrieve the Class of the base unit annotation
                Class<? extends Annotation> baseUnitAnnoClass =
                        AnnotationUtils.getElementValueClass(metaAnno, "quantity", true)
                                .asSubclass(Annotation.class);
                return baseUnitAnnoClass;
            }
        }
        // error: somehow the aliased annotation has @UnitsMultiple meta annotation, but no
        // base class defined in that meta annotation
        checker.userErrorAbort(
                checker.getClass().getSimpleName()
                        + ": Invalid @UnitsMultiple meta-annotation found in "
                        + anno.toString()
                        + ". @UnitsMultiple does not have a base unit class as its value.");
        return null;
    }

    /** Loads and processes all external units qualifiers */
    protected void loadAllExternalUnits() {
        // load external individually named units
        String qualNames = checker.getOption("units");
        if (qualNames != null) {
            for (String qualName : qualNames.split(",")) {
                loadExternalUnit(qualName);
            }
        }

        // load external directories of units
        String qualDirectories = checker.getOption("unitsDirs");
        if (qualDirectories != null) {
            for (String directoryName : qualDirectories.split(":")) {
                loadExternalDirectory(directoryName);
            }
        }
    }

    /** Loads and processes a single external units qualifier */
    private void loadExternalUnit(String annoName) {
        // only non-alias qualifiers are loaded in loadExternalAnnotationClass
        loadExternalAnnotationClass(annoName);
    }

    /**
     * Loads and processes the units qualifiers from a single external directory, only non-alias
     * qualifiers are added to external qual map.
     */
    private void loadExternalDirectory(String directoryName) {
        // only non-alias qualifiers are loaded in loadExternalAnnotationClassesFromDirectory
        loadExternalAnnotationClassesFromDirectory(directoryName);
    }
}
