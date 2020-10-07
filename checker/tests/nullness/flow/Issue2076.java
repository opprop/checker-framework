// Test case for issue #2076:
// https://github.com/typetools/checker-framework/issues/2076

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.ThrowsException;

public class Issue2076 {
    private @Nullable Object mObj = null;

    @ThrowsException(NullPointerException.class)
    public void buildAndThrowNPE() {}

    public @NonNull Object m1() {
        if (mObj == null) {
            buildAndThrowNPE();
        }
        return mObj;
    }

    public void m2(@Nullable Object obj) {
        int n;
        if (obj == null) {
            buildAndThrowNPE();
        }
        n = obj.hashCode();
    }
}
