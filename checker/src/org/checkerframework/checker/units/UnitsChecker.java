package org.checkerframework.checker.units;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.SupportedOptions;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.qual.StubFiles;

/**
 * Units Checker Main Class.
 *
 * <p>Supports "units" option to add support for additional individually named and externally
 * defined units, and "unitsDirs" option to add support for directories of externally defined units.
 * Directories must be well-formed paths from file system root, separated by colon (:) between each
 * directory.
 *
 * <p>Ex: {@code -Aunits=myPackage.qual.MyUnit,myPackage.qual.MyOtherUnit}
 *
 * <p>Ex: {@code -AunitsDirs=/full/path/to/myProject/bin:/full/path/to/myLibrary/bin}
 *
 * <p>Supports the debug "writeCSV" option which writes a set of CSV files to the given directory,
 * each showing the units relationship for an arithmetic operation. This option can be used with the
 * "printUU" option to show UnknownUnits in the CSV files.
 *
 * <p>Ex: {@code -AwriteCSV=$PWD}
 *
 * <p>Ex: {@code -AwriteCSV=$PWD -AprintUU}
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@SupportedOptions({"units", "unitsDirs", "writeCSV", "printUU"})
@StubFiles({"JavaBoxedPrimitives.astub", "JavaPrintStream.astub"})
public class UnitsChecker extends BaseTypeChecker {
    @Override
    public Collection<String> getSuppressWarningsKeys() {
        Set<String> swKeys = new HashSet<String>(super.getSuppressWarningsKeys());
        Set<Class<? extends Annotation>> annos =
                ((BaseTypeVisitor<?>) visitor).getTypeFactory().getSupportedTypeQualifiers();

        for (Class<? extends Annotation> anno : annos) {
            swKeys.add(anno.getSimpleName().toLowerCase());
        }

        return swKeys;
    }
}
