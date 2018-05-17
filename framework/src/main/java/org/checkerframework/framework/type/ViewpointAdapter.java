package org.checkerframework.framework.type;

import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeVariable;
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

    void viewpointAdaptTypeVariableBounds(
            AnnotatedDeclaredType receiverType,
            List<AnnotatedTypeMirror> declaredTypeVariables,
            Map<TypeVariable, AnnotatedTypeMirror> typeVarToTypeArgMapping,
            List<AnnotatedTypeParameterBounds> adaptedTypeVariableBounds,
            TypeVariableSubstitutor typeVarSubstitutor);
}
