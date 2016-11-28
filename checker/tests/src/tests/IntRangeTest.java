package tests;

import java.io.File;
import java.util.List;
import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

public class IntRangeTest extends CheckerFrameworkPerDirectoryTest {

    public IntRangeTest(List<File> testFiles) {
        super(
                testFiles,
                org.checkerframework.checker.intrange.IntRangeChecker.class,
                "intrange",
                "-Anomsgtext");
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[] {"intrange"};
    }
}
