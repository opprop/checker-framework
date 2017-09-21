package org.checkerframework.checker.units;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.DefaultAnnotatedTypeFormatter;
import org.checkerframework.framework.util.DefaultAnnotationFormatter;
import org.checkerframework.javacutil.AnnotationUtils;

/**
 * Annotated Type Formatter for the Units Checker.
 *
 * <p>We always format the print out of qualifiers by removing {@link Prefix#one}.
 */
public final class UnitsAnnotatedTypeFormatter extends DefaultAnnotatedTypeFormatter {

    public UnitsAnnotatedTypeFormatter(BaseTypeChecker checker) {
        super(
                new DefaultAnnotatedTypeFormatter.FormattingVisitor(
                        new UnitsAnnotationFormatter(checker),
                        checker.hasOption("printVerboseGenerics"),
                        checker.hasOption("printAllQualifiers")));
    }

    /**
     * Annotation Formatter for the Units Checker.
     *
     * <p>This class removes {@link Prefix#one} from any qualifier for error printing.
     */
    protected static class UnitsAnnotationFormatter extends DefaultAnnotationFormatter {
        protected final Elements elements;

        public UnitsAnnotationFormatter(BaseTypeChecker checker) {
            this.elements = checker.getElementUtils();
        }

        // Loops through a given a set of annotation mirrors and removes
        // Prefix.one if it is found. This method creates a new set with the
        // modified annotation mirrors in it, then passes to super for further
        // processing.
        @Override
        public String formatAnnotationString(
                Collection<? extends AnnotationMirror> annos, boolean printInvisible) {
            Set<AnnotationMirror> trimmedAnnoSet = AnnotationUtils.createAnnotationSet();

            for (AnnotationMirror anno : annos) {
                if (UnitsRelationsTools.getPrefixValue(anno) == Prefix.one) {
                    anno = UnitsRelationsTools.removePrefix(elements, anno);
                }
                trimmedAnnoSet.add(anno);
            }

            return super.formatAnnotationString(
                    Collections.unmodifiableSet(trimmedAnnoSet), printInvisible);
        }
    }
}
