package tests;

import java.io.File;
import java.util.List;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.framework.test.FrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

/** Tests the constant value propagation type system without overflow. */
public class ValueDivideByZeroTest extends FrameworkPerDirectoryTest {

    /** @param testFiles the files containing test code, which will be type-checked */
    public ValueDivideByZeroTest(List<File> testFiles) {
        super(
                testFiles,
                org.checkerframework.common.value.ValueChecker.class,
                "value",
                "-Anomsgtext",
                "-A" + ValueChecker.REPORT_EVAL_WARNS,
                "-A" + ValueChecker.DIVIDE_BY_ZERO_CHECK);
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[] {"all-systems", "value-divide-by-zero"};
    }
}
