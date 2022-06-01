import org.checkerframework.checker.units.qual.kg;

public class UnqualTest {
    // :: error: (assignment.type.incompatible)
    @kg int kg = 5;
    int nonkg = kg;
    @kg int alsokg = nonkg; // in the initializer, we can take the previous refinemnt.

    void test() {
        // :: error: (assignment.type.incompatible)
        @kg int x = this.nonkg;
    }
}
