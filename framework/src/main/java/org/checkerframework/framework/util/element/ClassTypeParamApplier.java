package org.checkerframework.framework.util.element;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TargetType;

import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedTypeVariable;
import org.checkerframework.framework.util.element.ElementAnnotationUtil.UnexpectedAnnotationLocationException;
import org.checkerframework.javacutil.BugInCF;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

/**
 * Applies the annotations present for a class type parameter onto an AnnotatedTypeVariable. See
 * {@link TypeParamElementAnnotationApplier} for details.
 */
public class ClassTypeParamApplier extends TypeParamElementAnnotationApplier {

    /**
     * Apply annotations from {@code element} to {@code type}.
     *
     * @param type the type to annotate
     * @param element the corresponding element
     * @param atypeFactory the type factory
     * @throws UnexpectedAnnotationLocationException if there is trouble
     */
    public static void apply(
            AnnotatedTypeVariable type, Element element, AnnotatedTypeFactory atypeFactory)
            throws UnexpectedAnnotationLocationException {
        new ClassTypeParamApplier(type, element, atypeFactory).extractAndApply();
    }

    /**
     * Returns true if element represents a type parameter for a class.
     *
     * @param type ignored
     * @param element the element that might be a type parameter
     * @return true if element represents a type parameter for a class
     */
    public static boolean accepts(AnnotatedTypeMirror type, Element element) {
        return element.getKind() == ElementKind.TYPE_PARAMETER
                && element.getEnclosingElement() instanceof Symbol.ClassSymbol;
    }

    /** The class that holds the type parameter element. */
    private final Symbol.ClassSymbol enclosingClass;

    /**
     * Constructor.
     *
     * @param type the type to annotate
     * @param element the corresponding element
     * @param atypeFactory the type factory
     */
    /*package-private*/ ClassTypeParamApplier(
            AnnotatedTypeVariable type, Element element, AnnotatedTypeFactory atypeFactory) {
        super(type, element, atypeFactory);

        if (!(element.getEnclosingElement() instanceof Symbol.ClassSymbol)) {
            throw new BugInCF(
                    "TypeParameter not enclosed by class?  Type( "
                            + type
                            + " ) "
                            + "Element ( "
                            + element
                            + " ) ");
        }

        enclosingClass = (Symbol.ClassSymbol) element.getEnclosingElement();
    }

    /**
     * Returns TargetType.CLASS_TYPE_PARAMETER.
     *
     * @return TargetType.CLASS_TYPE_PARAMETER
     */
    @Override
    protected TargetType lowerBoundTarget() {
        return TargetType.CLASS_TYPE_PARAMETER;
    }

    /**
     * Returns TargetType.CLASS_TYPE_PARAMETER_BOUND.
     *
     * @return TargetType.CLASS_TYPE_PARAMETER_BOUND
     */
    @Override
    protected TargetType upperBoundTarget() {
        return TargetType.CLASS_TYPE_PARAMETER_BOUND;
    }

    /**
     * Returns the index of element in the type parameter list of its enclosing class.
     *
     * @return the index of element in the type parameter list of its enclosing class
     */
    @Override
    public int getElementIndex() {
        return enclosingClass.getTypeParameters().indexOf(element);
    }

    /** The valid targets. */
    private static final TargetType[] validTargets = new TargetType[] {TargetType.CLASS_EXTENDS};

    @Override
    protected TargetType[] validTargets() {
        return validTargets;
    }

    /**
     * Returns the raw type attributes of the enclosing class.
     *
     * @return the raw type attributes of the enclosing class
     */
    @Override
    protected Iterable<Attribute.TypeCompound> getRawTypeAttributes() {
        return enclosingClass.getRawTypeAttributes();
    }

    @Override
    protected boolean isAccepted() {
        return accepts(type, element);
    }
}
