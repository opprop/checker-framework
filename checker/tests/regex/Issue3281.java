// Test case for Issue 3281:
// https://github.com/typetools/checker-framework/issues/3281

import java.util.regex.Pattern;
import org.checkerframework.checker.regex.RegexUtil;

public class Issue3281 {

    void bar(String s) {
        RegexUtil.isRegex(s);
        if (true) {
            // :: error: (argument.type.incompatible)
            Pattern.compile(s);
        }
    }
}
