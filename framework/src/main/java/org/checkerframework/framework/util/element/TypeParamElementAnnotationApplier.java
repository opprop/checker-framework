package org.checkerframework.framework.util.element;

import com.sun.tools.javac.code.Attribute.TypeCompound;
import com.sun.tools.javac.code.TargetType;

import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedIntersectionType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedTypeVariable;
import org.checkerframework.framework.util.element.ElementAnnotationUtil.UnexpectedAnnotationLocationException;
import org.checkerframework.javacutil.BugInCF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;

/**
 * Applies Element annotations to a single AnnotatedTypeVariable representing a type parameter.
 * Note, the index of IndexedElementAnnotationApplier refers to the type parameter's index in the
 * list that encloses it.
 */
abstract class TypeParamElementAnnotationApplier extends IndexedElementAnnotationApplier {

    /**
     * Returns true if element is a TYPE_PARAMETER.
     *
     * @param typeMirror ignored
     * @param element the element that might be a TYPE_PARAMETER
     * @return true if element is a TYPE_PARAMETER
     */
    public static boolean accepts(AnnotatedTypeMirror typeMirror, Element element) {
        return element.getKind() == ElementKind.TYPE_PARAMETER;
    }

    protected final AnnotatedTypeVariable typeParam;
    protected final AnnotatedTypeFactory atypeFactory;

    /**
     * Returns target type that represents the location of the lower bound of element.
     *
     * @return target type that represents the location of the lower bound of element
     */
    protected abstract TargetType lowerBoundTarget();

    /**
     * Returns target type that represents the location of the upper bound of element.
     *
     * @return target type that represents the location of the upper bound of element
     */
    protected abstract TargetType upperBoundTarget();

    /**
     * Constructor.
     *
     * @param type the type to annotate
     * @param element the corresponding element
     * @param atypeFactory the type factory
     */
    /*package-private*/ TypeParamElementAnnotationApplier(
            AnnotatedTypeVariable type, Element element, AnnotatedTypeFactory atypeFactory) {
        super(type, element);
        this.typeParam = type;
        this.atypeFactory = atypeFactory;
    }

    /**
     * Returns the lower bound and upper bound targets.
     *
     * @return the lower bound and upper bound targets
     */
    @Override
    protected TargetType[] annotatedTargets() {
        return new TargetType[] {lowerBoundTarget(), upperBoundTarget()};
    }

    /**
     * Returns the parameter_index of anno's TypeAnnotationPosition which will actually point to the
     * type parameter's index in its enclosing type parameter list.
     *
     * @return the parameter_index of anno's TypeAnnotationPosition which will actually point to the
     *     type parameter's index in its enclosing type parameter list
     */
    @Override
    public int getTypeCompoundIndex(TypeCompound anno) {
        return anno.getPosition().parameter_index;
    }

    /**
     * @param targeted the list of annotations that were on the lower/upper bounds of the type
     *     parameter
     *     <p>Note: When handling type parameters we NEVER add primary annotations to the type
     *     parameter. Primary annotations are reserved for the use of a type parameter
     *     (e.g. @Nullable T t; )
     *     <p>If an annotation is present on the type parameter itself, it represents the
     *     lower-bound annotation of that type parameter. Any annotation on the extends bound of a
     *     type parameter is placed on that bound.
     */
    @Override
    protected void handleTargeted(List<TypeCompound> targeted)
            throws UnexpectedAnnotationLocationException {
        int paramIndex = getElementIndex();
        List<TypeCompound> upperBoundAnnos = new ArrayList<>();
        List<TypeCompound> lowerBoundAnnos = new ArrayList<>();

        for (TypeCompound anno : targeted) {
            AnnotationMirror aliasedAnno = atypeFactory.canonicalAnnotation(anno);
            AnnotationMirror canonicalAnno = (aliasedAnno != null) ? aliasedAnno : anno;

            if (anno.position.parameter_index != paramIndex
                    || !atypeFactory.isSupportedQualifier(canonicalAnno)) {
                continue;
            }

            if (ElementAnnotationUtil.isOnComponentType(anno)) {
                applyComponentAnnotation(anno);
            } else if (anno.position.type == upperBoundTarget()) {
                upperBoundAnnos.add(anno);
            } else {
                lowerBoundAnnos.add(anno);
            }
        }

        applyLowerBounds(lowerBoundAnnos);
        applyUpperBounds(upperBoundAnnos);
    }

    /**
     * Applies a list of annotations to the upperBound of the type parameter. If the type of the
     * upper bound is an intersection we must first find the correct location for each annotation.
     */
    private void applyUpperBounds(List<TypeCompound> upperBounds) {
        if (!upperBounds.isEmpty()) {
            AnnotatedTypeMirror upperBoundType = typeParam.getUpperBound();

            if (upperBoundType.getKind() == TypeKind.INTERSECTION) {
                List<AnnotatedTypeMirror> bounds =
                        ((AnnotatedIntersectionType) upperBoundType).getBounds();
                int boundIndexOffset = ElementAnnotationUtil.getBoundIndexOffset(bounds);

                for (TypeCompound anno : upperBounds) {
                    int boundIndex = anno.position.bound_index + boundIndexOffset;

                    if (boundIndex < 0 || boundIndex > bounds.size()) {
                        throw new BugInCF(
                                "Invalid bound index on element annotation ( "
                                        + anno
                                        + " ) "
                                        + "for type ( "
                                        + typeParam
                                        + " ) with "
                                        + "upper bound ( "
                                        + typeParam.getUpperBound()
                                        + " ) "
                                        + "and boundIndex( "
                                        + boundIndex
                                        + " ) ");
                    }

                    bounds.get(boundIndex).replaceAnnotation(anno); // TODO: WHY NOT ADD?
                }
                ((AnnotatedIntersectionType) upperBoundType).copyIntersectionBoundAnnotations();

            } else {
                upperBoundType.addAnnotations(upperBounds);
            }
        }
    }

    /**
     * In the event of multiple annotations on an AnnotatedNullType lower bound we want to preserve
     * the multiple annotations so that a type.invalid error is issued later.
     *
     * @param annos the annotations to add to the lower bound
     */
    private void applyLowerBounds(List<? extends AnnotationMirror> annos) {
        if (!annos.isEmpty()) {
            AnnotatedTypeMirror lowerBound = typeParam.getLowerBound();

            for (AnnotationMirror anno : annos) {
                lowerBound.addAnnotation(anno);
            }
        }
    }

    private void addAnnotationToMap(
            AnnotatedTypeMirror type,
            TypeCompound anno,
            Map<AnnotatedTypeMirror, List<TypeCompound>> typeToAnnos) {
        List<TypeCompound> annoList = typeToAnnos.computeIfAbsent(type, __ -> new ArrayList<>());
        annoList.add(anno);
    }

    private void applyComponentAnnotation(TypeCompound anno)
            throws UnexpectedAnnotationLocationException {
        AnnotatedTypeMirror upperBoundType = typeParam.getUpperBound();

        Map<AnnotatedTypeMirror, List<TypeCompound>> typeToAnnotations = new HashMap<>();

        if (anno.position.type == upperBoundTarget()) {
            if (upperBoundType.getKind() == TypeKind.INTERSECTION) {
                List<AnnotatedTypeMirror> bounds =
                        ((AnnotatedIntersectionType) upperBoundType).getBounds();
                int boundIndex =
                        anno.position.bound_index
                                + ElementAnnotationUtil.getBoundIndexOffset(bounds);

                if (boundIndex < 0 || boundIndex > bounds.size()) {
                    throw new BugInCF(
                            "Invalid bound index on element annotation ( "
                                    + anno
                                    + " ) "
                                    + "for type ( "
                                    + typeParam
                                    + " ) with upper bound ( "
                                    + typeParam.getUpperBound()
                                    + " )");
                }
                addAnnotationToMap(bounds.get(boundIndex), anno, typeToAnnotations);
            } else {
                addAnnotationToMap(upperBoundType, anno, typeToAnnotations);
            }
        } else {
            addAnnotationToMap(typeParam.getLowerBound(), anno, typeToAnnotations);
        }

        for (Map.Entry<AnnotatedTypeMirror, List<TypeCompound>> typeToAnno :
                typeToAnnotations.entrySet()) {
            ElementAnnotationUtil.annotateViaTypeAnnoPosition(
                    typeToAnno.getKey(), typeToAnno.getValue());
        }
    }
}
