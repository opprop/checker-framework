package viewpointtest;

import org.checkerframework.framework.type.AbstractViewpointAdapter;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import viewpointtest.quals.ReceiverDependantQual;
import viewpointtest.quals.Top;

import javax.lang.model.element.AnnotationMirror;

public class ViewpointTestViewpointAdapter extends AbstractViewpointAdapter {

    private final AnnotationMirror TOP, RECEIVERDEPENDANTQUAL;

    /**
     * The class constructor.
     *
     * @param atypeFactory
     */
    public ViewpointTestViewpointAdapter(AnnotatedTypeFactory atypeFactory) {
        super(atypeFactory);
        TOP = AnnotationBuilder.fromClass(atypeFactory.getElementUtils(), Top.class);
        RECEIVERDEPENDANTQUAL =
                AnnotationBuilder.fromClass(
                        atypeFactory.getElementUtils(), ReceiverDependantQual.class);
    }

    @Override
    protected AnnotationMirror extractAnnotationMirror(AnnotatedTypeMirror atm) {
        return atm.getAnnotationInHierarchy(TOP);
    }

    @Override
    protected AnnotationMirror combineAnnotationWithAnnotation(
            AnnotationMirror receiverAnnotation, AnnotationMirror declaredAnnotation) {

        if (AnnotationUtils.areSame(declaredAnnotation, RECEIVERDEPENDANTQUAL)) {
            return receiverAnnotation;
        }
        return declaredAnnotation;
    }
}
