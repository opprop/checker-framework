package org.checkerframework.checker.units;

import java.lang.annotation.Annotation;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.PolyUnit;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.checker.units.qual.time.duration.TimeDuration;
import org.checkerframework.checker.units.qual.time.instant.TimeInstant;
import org.checkerframework.common.basetype.BaseTypeChecker;

/**
 * Utility class for Units Checker to create and hold annotation mirrors used in Units Checker
 * logic.
 *
 * <p>This class is a singleton class. Instantiate a copy of the class via {@link
 * #getInstance(BaseTypeChecker)} then directly reference the fields of the class.
 */
public class UnitsMirrors {
    private static UnitsMirrors instance = null;

    private final ProcessingEnvironment processingEnv;

    protected final AnnotationMirror UNKNOWN;
    protected final AnnotationMirror DIMENSIONLESS;
    protected final AnnotationMirror BOTTOM;
    protected final AnnotationMirror POLYUNIT;
    protected final AnnotationMirror INSTANT;
    protected final AnnotationMirror DURATION;

    public static UnitsMirrors getInstance(BaseTypeChecker checker) {
        if (instance == null) {
            instance = new UnitsMirrors(checker);
        }
        return instance;
    }

    private UnitsMirrors(BaseTypeChecker checker) {
        processingEnv = checker.getProcessingEnvironment();

        UNKNOWN = buildUnitsAnno(UnknownUnits.class);
        DIMENSIONLESS = buildUnitsAnno(Dimensionless.class);
        BOTTOM = buildUnitsAnno(UnitsBottom.class);
        POLYUNIT = buildUnitsAnno(PolyUnit.class);
        INSTANT = buildUnitsAnno(TimeInstant.class);
        DURATION = buildUnitsAnno(TimeDuration.class);
    }

    private AnnotationMirror buildUnitsAnno(Class<? extends Annotation> unit) {
        return UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, unit);
    }
}
