package org.checkerframework.checker.units;

import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.TypesUtils;

/** Units visitor. */
public class UnitsVisitor extends BaseTypeVisitor<UnitsAnnotatedTypeFactory> {
    public UnitsVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    /**
     * Override to allow references to be declared using any units annotation except {@link
     * UnitsBottom}. Classes are by default {@link Dimensionless}, but these reference declarations
     * will use some unit that isn't a subtype of {@link Dimensionless}. If this override isn't in
     * place, then a lot of Type.Invalid errors show.
     */
    @Override
    public boolean isValidUse(
            AnnotatedDeclaredType declarationType, AnnotatedDeclaredType useType, Tree tree) {
        // eg for the statement "@m Double x;" the declarationType is @Dimensionless
        // Double, and the useType is @m Double
        if (declarationType.getEffectiveAnnotation(Dimensionless.class) != null
                && useType.getEffectiveAnnotation(UnitsBottom.class) == null) {
            // if declared type of a class is Dimensionless, and the use of that class
            // is any of the Units annotations other than UnitsBottom, return
            // true
            return true;
        } else {
            // otherwise check the usage using super
            return super.isValidUse(declarationType, useType, tree);
        }
    }

    /**
     * Override to allow the creation of boxed primitive number objects using any units annotation
     * except {@link UnitsBottom}. Classes are by default {@link Dimensionless}, but these objects
     * may use some unit that isn't a subtype of {@link Dimensionless}.
     */
    @Override
    protected boolean checkConstructorInvocation(
            AnnotatedDeclaredType invocation,
            AnnotatedExecutableType constructor,
            NewClassTree newClassTree) {
        // The declared constructor return type is the same as the declared type
        // of the class that is being constructed, by default this will be
        // Dimensionless.
        // For Boxed Number types, we have @PolyUnit for the constructor return
        // type which will match the unit of the single number parameter of the
        // constructor.
        // eg for the statement "new @m Double(30.0);" the constructor return
        // type is @Dimensionless Double while the use type is @m Double.
        AnnotatedTypeMirror declaredConstructorReturnType = constructor.getReturnType();

        // If it is a boxed primitive class, and either the constructor return
        // type or the use type is dimensionless, and the other unit is not
        // UnitsBottom, pass.
        if (isBoxedPrimitiveNumber(declaredConstructorReturnType.getUnderlyingType())
                && declaredConstructorReturnType.getEffectiveAnnotation(Dimensionless.class) != null
                && invocation.getEffectiveAnnotation(UnitsBottom.class) == null) {
            return true;
        }
        // otherwise check the constructor invocation using super
        return super.checkConstructorInvocation(invocation, constructor, newClassTree);
    }

    /**
     * Checks to see if the type is a boxed primitive number type (Byte, Short, Integer, Long,
     * Float, Double).
     *
     * @param type a TypeMirror for some code element.
     * @return true if the type is a boxed primitive number type, false otherwise.
     */
    private static boolean isBoxedPrimitiveNumber(TypeMirror type) {
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }

        String qualifiedName = TypesUtils.getQualifiedName((DeclaredType) type).toString();

        return (qualifiedName.contentEquals("java.lang.Byte")
                || qualifiedName.contentEquals("java.lang.Short")
                || qualifiedName.contentEquals("java.lang.Integer")
                || qualifiedName.contentEquals("java.lang.Long")
                || qualifiedName.contentEquals("java.lang.Float")
                || qualifiedName.contentEquals("java.lang.Double"));
    }

    /**
     * Type checks compound assignment operations. The result of the arithmetic operation must be
     * the same unit as the variable.
     */
    // This is called if a compound assignment is the only expression in a statement, eg x += y;
    @SuppressWarnings("fallthrough") // fallthrough is intended
    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
        switch (node.getKind()) {
            case PLUS_ASSIGNMENT:
            case MINUS_ASSIGNMENT:
            case MULTIPLY_ASSIGNMENT:
            case DIVIDE_ASSIGNMENT:
                getTypeFactory().relationsEnforcer.getCompoundAssignmentUnit(node);
                return null;
            default:
                return super.visitCompoundAssignment(node, p);
        }
    }
}
