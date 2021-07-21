package tests;

import org.checkerframework.framework.test.FrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.List;

/** Test suite for the Subtyping Checker, using a simple {@link Encrypted} annotation. */
public class SubtypingEncryptedTest extends FrameworkPerDirectoryTest {

    /** @param testFiles the files containing test code, which will be type-checked */
    public SubtypingEncryptedTest(List<File> testFiles) {
        super(
                testFiles,
                org.checkerframework.common.subtyping.SubtypingChecker.class,
                "subtyping",
                "-Anomsgtext",
                "-Aquals=testlib.util.Encrypted,testlib.util.PolyEncrypted,org.checkerframework.common.subtyping.qual.Unqualified");
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[] {"subtyping", "all-systems"};
    }
}
