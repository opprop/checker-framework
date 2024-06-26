import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;
import org.checkerframework.checker.initialization.qual.UnderInitialization;

public class NotOnlyInitializedTest {

    @NotOnlyInitialized NotOnlyInitializedTest f;
    NotOnlyInitializedTest g;

    public NotOnlyInitializedTest() {
        f = new NotOnlyInitializedTest();
        g = new NotOnlyInitializedTest();
    }

    public NotOnlyInitializedTest(char i) {
        // we can store something that is under initialization (like this) in f, but not in g
        f = this;
        // :: error: (assignment.type.incompatible)
        g = this;
    }

    static void testDeref(NotOnlyInitializedTest o) {
        // o is fully iniatlized, so we can dereference its fields
        o.f.toString();
        o.g.toString();
    }

    static void testDeref2(@UnderInitialization NotOnlyInitializedTest o) {
        // o is not fully iniatlized, so we cannot dereference its fields.
        // We thus get a dereference.of.nullable error by the Nullness Checker for both o.f and o.g.
        // For o.f, we also get a method.invocation.invalid error by the Initialization Checker
        // because o.f is declared as @NotOnlyInitialized and thus may not be @Initialized,
        // but toLowerCase()'s receiver type is, by default, @Initialized.

        // :: error: (dereference.of.nullable) :: error: (method.invocation.invalid)
        o.f.toString();
        // :: error: (dereference.of.nullable)
        o.g.toString();
    }
}
