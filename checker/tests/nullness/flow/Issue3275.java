// Test case for Issue 3275:
// https://github.com/typetools/checker-framework/issues/3275

import org.checkerframework.checker.nullness.qual.Nullable;

class Bug {
    void foo(@Nullable Object obj) {
        if ((obj != null) == false) {
            // :: (dereference.of.nullable)
            obj.toString();
        }
    }

    void bar(@Nullable Object obj) {
        if (!(obj == null) == false) {
            // :: (dereference.of.nullable)
            obj.toString();
        }
    }

    void baz(@Nullable Object obj) {
        if ((obj == null) == true) {
            // :: (dereference.of.nullable)
            obj.toString();
        }
    }
}
