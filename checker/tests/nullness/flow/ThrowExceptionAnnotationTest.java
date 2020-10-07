import java.io.IOException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.ThrowsException;

public class ThrowExceptionAnnotationTest {
    private @Nullable Object mObj = null;

    @ThrowsException(IOException.class)
    public void foo() throws IOException {}

    @ThrowsException(NullPointerException.class)
    public void bar() {}

    public void m1(@Nullable Object obj) {
        int n;
        try {
            foo();

            // :: error: (dereference.of.nullable)
            int x = obj.hashCode();

        } catch (IOException e) {
            // :: error: (dereference.of.nullable)
            int x = obj.hashCode();
        } finally {
        }
    }

    public void m2(@Nullable Object obj) {
        int n;
        try {
            foo();

        } catch (IOException e) {
        } finally {
            // :: error: (dereference.of.nullable)
            int x = obj.hashCode();
        }
    }

    public void m3(@Nullable Object obj) {
        int n;
        try {
            foo();

        } catch (IOException e) {
        } finally {
        }

        // :: error: (dereference.of.nullable)
        int x = obj.hashCode();
    }

    public void m4(@Nullable Object obj) throws IOException {
        int n;
        if (obj == null) {
            foo();
        }
        n = obj.hashCode();
    }

    public void m5() {
        if (mObj == null) {
            bar();
        }
        int x = mObj.hashCode();
    }
}
