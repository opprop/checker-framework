package org.checkerframework.framework.type;

import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;

/**
 * Interface that contains viewpoint adaptation logic.
 *
 * <p>Standard viewpoint adaptation happens on members/fields accesses, constructor invocations,
 * method invocations and type parameter instantiations.
 */
public interface ViewpointAdapter {

    /**
     * Viewpoint adapts members/fields accesses
     *
     * @param receiverType receiver type through which members are accessed
     * @param memberElement element of the accessed member
     * @param memberType accessed type of the member. After the method returns, it will be mutated
     *     to the viewpoint adapted result.
     */
    void viewpointAdaptMember(
            AnnotatedTypeMirror receiverType,
            Element memberElement,
            AnnotatedTypeMirror memberType);

    /**
     * Viewpoint adapts constructor invocations.
     *
     * @param receiverType receiver type through which constructors are invoked
     * @param constructorElt element of the invoked constructor
     * @param constructorType invoked type of the constructor. After the method returns, it will be
     *     mutated to the viewpoint adapted constructor signature.
     */
    void viewpointAdaptConstructor(
            AnnotatedTypeMirror receiverType,
            ExecutableElement constructorElt,
            AnnotatedExecutableType constructorType);

    /**
     * Viewpoint adapts method invocations.
     *
     * @param receiverType receiver type through which methods are invoked
     * @param methodElt element of the invoked method
     * @param methodType invoked type of the method. After the method returns, it will be mutated to
     *     the viewpoint adapted method signature.
     */
    void viewpointAdaptMethod(
            AnnotatedTypeMirror receiverType,
            ExecutableElement methodElt,
            AnnotatedExecutableType methodType);

    /**
     * Viewpoint adapts type parameter bounds when being instantiated.
     *
     * @param receiverType receiver type through which type parameter is instantiated
     * @param typeParameterBounds a list of type parameter bounds. After the method returns, it will
     *     be mutated to the viewpoint adapted type parameter bounds.
     */
    void viewpointAdaptTypeParameterBounds(
            AnnotatedTypeMirror receiverType,
            List<AnnotatedTypeParameterBounds> typeParameterBounds);
}
