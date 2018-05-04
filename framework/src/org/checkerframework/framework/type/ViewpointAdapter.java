package org.checkerframework.framework.type;

import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeVariable;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;

public interface ViewpointAdapter {

    void viewpointAdaptMember(AnnotatedTypeMirror type, AnnotatedTypeMirror owner, Element element);

    AnnotatedExecutableType viewpointAdaptConstructor(
            AnnotatedTypeMirror type, AnnotatedExecutableType con);

    void viewpointAdaptMethod(
            ExecutableElement methodElt,
            AnnotatedTypeMirror receiverType,
            AnnotatedExecutableType methodType);

    void viewpointAdaptTypeVariableBounds(
            AnnotatedTypeMirror.AnnotatedDeclaredType type,
            List<AnnotatedTypeMirror> tvars,
            Map<TypeVariable, AnnotatedTypeMirror> mapping,
            List<AnnotatedTypeParameterBounds> res,
            TypeVariableSubstitutor typeVarSubstitutor);
}
