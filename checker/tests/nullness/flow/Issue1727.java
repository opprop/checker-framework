class B {}

public class Issue1727 {

    private B foo() {
        // Default type for local variable b is @UnknownInitialization @Nullable
        B b;

        while (true) {
            B op = getB();
            // :: warning: (known.nonnull)
            if (op == null) {
                b = new B();
                break;
            } else {
                b = op;
                break;
            }
        }

        return b;
    }

    private B getB() {
        return new B();
    }
}
