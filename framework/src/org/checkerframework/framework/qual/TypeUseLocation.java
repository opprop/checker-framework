package org.checkerframework.framework.qual;

/**
 * Specifies the locations to which a {@link DefaultQualifier} annotation applies.
 *
 * The order of enums is important. Defaults are applied in this order.
 * In particular, this means that OTHERWISE and ALL should be last.
 *
 * @see DefaultQualifier
 * @see javax.lang.model.element.ElementKind
 */
public enum TypeUseLocation {

    /**
     * Apply default annotations to all unannotated raw types
     * of fields.
     */
    FIELD,

    /**
     * Apply default annotations to all unannotated raw types
     * of local variables, casts, and instanceof.
     * <p>
     * TODO: should cast/instanceof be separated?
     */
    LOCAL_VARIABLE,

    /**
     * Apply default annotations to all unannotated raw types
     * of resource variables.
     */
    RESOURCE_VARIABLE,

    /**
     * Apply default annotations to all unannotated raw types
     * of exception parameters.
     */
    EXCEPTION_PARAMETER,

    /**
     * Apply default annotations to all unannotated raw types
     * of receiver types.
     */
    RECEIVER,

    /**
     * Apply default annotations to all unannotated raw types
     * of formal parameter types.
     */
    PARAMETER,

    /**
     * Apply default annotations to all unannotated raw types
     * of return types.
     */
    RETURN,

    /**
     * Apply default annotations to unannotated lower bounds
     * for type variables and wildcards both explicit ones in
     * {@code extends} clauses, and implicit upper bounds
     * when no explicit {@code extends} or {@code super}
     * clause is present
     */
    LOWER_BOUND,

    /**
     * Apply default annotations to unannotated, but explicit lower bounds:
     * {@code <? super Object>}
     *
     */
    EXPLICIT_LOWER_BOUND,

    /**
     * Apply default annotations to unannotated, but implicit lower bounds:
     * {@code <T>}
     * {@code <?>}
     */
    IMPLICIT_LOWER_BOUND,

    /**
     * Apply default annotations to unannotated upper bounds:  both
     * explicit ones in {@code extends} clauses, and implicit upper bounds
     * when no explicit {@code extends} or {@code super} clause is
     * present.
     *
     * Especially useful for parametrized classes that provide a lot of
     * static methods with the same generic parameters as the class.
     *
     * TODO: more doc, relation to other UPPER_BOUND
     */
    UPPER_BOUND,

    /**
     * Apply default annotations to unannotated, but explicit upper bounds:
     * {@code <T extends Object>}
     *
     * TODO: more doc, relation to other UPPER_BOUND
     */
    EXPLICIT_UPPER_BOUND,

    /**
     * Apply default annotations to unannotated type variables:
     * {@code <T>}
     *
     * TODO: more doc, relation to other UPPER_BOUND
     */
    IMPLICIT_UPPER_BOUND,

    /**
     * Apply default annotations to unannotated type declarations:
     * {@code @HERE class Demo{}}
     */
    TYPE_DECLARATION,

    /**
     * Represents type argument location in parameterized type
     * {@code List<@TA1 ArrayList<@TA2 String>>}
     */
    TYPE_ARGUMENT,

    /**
     * Represents array component location
     * {@code @AC2 String [] @AC1 []}
     */
    ARRAY_COMPONENT,

    /**
     * Represents extends location of a class/interface/enum/annotation type
     * {@code class C extends @E java.lang.Object}}
     */
    EXTENDS,

    /**
     * Represents implement location of a class/enum
     * {@code class C implements @I java.lang.Cloneable}}
     */
    IMPLEMENTS,

    /**
     * Represents method throw clause
     * {@code void foo() throws @T NullPointerException}
     */
    THROWS,

    /**
     * Represents instanceof location
     * {@code Number instanceof @IOF Object}
     */
    INSTANCEOF,

    /**
     * Represents new expression location
     * {@code new @N Object();}
     */
    NEW,

    /**
     * Represents cast location
     * {@code (@C Object) e}
     */
    CAST,

    /**
     * Apply if nothing more concrete is provided.
     */
    OTHERWISE,

    /**
     * Apply default annotations to all type uses other than uses of type parameters.
     * Does not allow any of the other constants. Usually you want OTHERWISE.
     */
    ALL;
}
