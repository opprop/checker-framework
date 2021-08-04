package tests;

import java.io.File;
import java.util.List;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.framework.test.FrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

/** Tests the constant value propagation type system with property file handler. */
public class ValueHandlePropertyFileTest extends FrameworkPerDirectoryTest {

    /** @param testFiles the files containing test code, which will be type-checked */
    public ValueHandlePropertyFileTest(List<File> testFiles) {
        super(
                testFiles,
                org.checkerframework.common.value.ValueChecker.class,
                "value",
                "-Anomsgtext",
                "-A" + ValueChecker.REPORT_EVAL_WARNS,
                "-A" + ValueChecker.HANDLE_PROPERTY_FILES);
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[] {"value", "all-systems", "value-handle-property-file"};
    }
}
