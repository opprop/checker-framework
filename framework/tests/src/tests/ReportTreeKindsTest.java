package tests;

import java.io.File;
import java.util.List;
import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

public class ReportTreeKindsTest extends CheckerFrameworkPerDirectoryTest {

    public ReportTreeKindsTest(List<File> testFiles) {
        super(
                testFiles,
                org.checkerframework.common.util.report.ReportChecker.class,
                "report",
                "-Anomsgtext",
                "-AreportTreeKinds=WHILE_LOOP,CONDITIONAL_AND");
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[] {"reporttreekinds"};
    }
}
