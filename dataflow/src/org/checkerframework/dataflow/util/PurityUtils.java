package org.checkerframework.dataflow.util;

import com.sun.source.tree.MethodTree;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import org.checkerframework.dataflow.qual.MultipleRunDeterministic;
import org.checkerframework.dataflow.qual.NonDeterministic;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Pure.Kind;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.SingleRunDeterministic;
import org.checkerframework.javacutil.AnnotationProvider;
import org.checkerframework.javacutil.InternalUtils;

/**
 * An utility class for working with the {@link SideEffectFree}, {@link Deterministic}, and {@link
 * Pure} annotations.
 *
 * @see SideEffectFree
 * @see Deterministic
 * @see Pure
 * @author Stefan Heule
 */
public class PurityUtils {

    /** Does the method {@code tree} have any purity annotation? */
    public static boolean hasPurityAnnotation(AnnotationProvider provider, MethodTree tree) {
        return !getPurityKinds(provider, tree).isEmpty();
    }

    /** Does the method {@code methodElement} have any purity annotation? */
    public static boolean hasPurityAnnotation(AnnotationProvider provider, Element methodElement) {
        return !getPurityKinds(provider, methodElement).isEmpty();
    }

    /** Is the method {@code tree} deterministic? */
    public static boolean isSingleDeterministic(AnnotationProvider provider, MethodTree tree) {
        Element methodElement = InternalUtils.symbol(tree);
        return isSingleDeterministic(provider, methodElement);
    }

    /** Is the method {@code methodElement} deterministic? */
    public static boolean isSingleDeterministic(
            AnnotationProvider provider, Element methodElement) {
        List<Kind> kinds = getPurityKinds(provider, methodElement);
        return kinds.contains(Kind.SINGLE_RUN_DETERMINISTIC);
    }

    /** Is the method {@code tree} deterministic? */
    public static boolean isMultiDeterministic(AnnotationProvider provider, MethodTree tree) {
        Element methodElement = InternalUtils.symbol(tree);
        return isMultiDeterministic(provider, methodElement);
    }

    /** Is the method {@code methodElement} deterministic? */
    public static boolean isMultiDeterministic(AnnotationProvider provider, Element methodElement) {
        List<Kind> kinds = getPurityKinds(provider, methodElement);
        return kinds.contains(Kind.MULTIPLE_RUN_DETERMINISTIC);
    }

    /** Is the method {@code tree} deterministic? */
    public static boolean isNonDeterministic(AnnotationProvider provider, MethodTree tree) {
        Element methodElement = InternalUtils.symbol(tree);
        return isNonDeterministic(provider, methodElement);
    }

    /** Is the method {@code methodElement} deterministic? */
    public static boolean isNonDeterministic(AnnotationProvider provider, Element methodElement) {
        List<Kind> kinds = getPurityKinds(provider, methodElement);
        return kinds.contains(Kind.NON_DETERMINISTIC);
    }

    /** Is the method {@code tree} side-effect-free? */
    public static boolean isSideEffectFree(AnnotationProvider provider, MethodTree tree) {
        Element methodElement = InternalUtils.symbol(tree);
        return isSideEffectFree(provider, methodElement);
    }

    /** Is the method {@code methodElement} side-effect-free? */
    public static boolean isSideEffectFree(AnnotationProvider provider, Element methodElement) {
        List<Kind> kinds = getPurityKinds(provider, methodElement);
        return kinds.contains(Kind.SIDE_EFFECT_FREE);
    }

    /** @return the types of purity of the method {@code tree}. */
    public static List<Pure.Kind> getPurityKinds(AnnotationProvider provider, MethodTree tree) {
        Element methodElement = InternalUtils.symbol(tree);
        return getPurityKinds(provider, methodElement);
    }

    /**
     * @return the types of purity of the method {@code methodElement}. TODO: should the return type
     *     be an EnumSet?
     */
    public static List<Pure.Kind> getPurityKinds(
            AnnotationProvider provider, Element methodElement) {
        AnnotationMirror sefAnnotation =
                provider.getDeclAnnotation(methodElement, SideEffectFree.class);
        AnnotationMirror sdetAnnotation =
                provider.getDeclAnnotation(methodElement, SingleRunDeterministic.class);
        AnnotationMirror mdetAnnotation =
                provider.getDeclAnnotation(methodElement, MultipleRunDeterministic.class);
        AnnotationMirror ndetAnnotation =
                provider.getDeclAnnotation(methodElement, NonDeterministic.class);

        List<Pure.Kind> kinds = new ArrayList<>();
        if (sefAnnotation != null) {
            kinds.add(Kind.SIDE_EFFECT_FREE);
        }
        if (mdetAnnotation != null) {
            kinds.add(Kind.MULTIPLE_RUN_DETERMINISTIC);
        }
        if (sdetAnnotation != null) {
            kinds.add(Kind.SINGLE_RUN_DETERMINISTIC);
            kinds.add(Kind.MULTIPLE_RUN_DETERMINISTIC);
        }
        if (ndetAnnotation != null) {
            kinds.add(Kind.NON_DETERMINISTIC);
            kinds.add(Kind.SINGLE_RUN_DETERMINISTIC);
            kinds.add(Kind.MULTIPLE_RUN_DETERMINISTIC);
        }
        return kinds;
    }
}
