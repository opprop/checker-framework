package org.checkerframework.framework.type;

import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;

public interface ViewpointAdapter {

    void viewpointAdaptMember(
            AnnotatedTypeMirror memberType,
            AnnotatedTypeMirror receiverType,
            Element memberElement);

    void viewpointAdaptConstructor(
            ExecutableElement constructorElt,
            AnnotatedTypeMirror receiverType,
            AnnotatedExecutableType constructorType);

    void viewpointAdaptMethod(
            ExecutableElement methodElt,
            AnnotatedTypeMirror receiverType,
            AnnotatedExecutableType methodType);

    void viewpointAdaptTypeParameterBounds(
            AnnotatedDeclaredType receiverType,
            List<AnnotatedTypeParameterBounds> typeParameterBounds);
}
