import org.checkerframework.checker.units.qual.*;

public class UnqualTest {
    // :: error: (assignment.type.incompatible)
    @kg int kg = 5;
    // :: error: (assignment.type.incompatible)
    int nonkg = kg;
    // :: error: (assignment.type.incompatible)
    @kg int alsokg = nonkg;

    void m() {
        // :: error: (assignment.type.incompatible)
        @kg int local_kg = 5;
        int unknown_refined_to_kg = local_kg;
        @kg int alsokg = unknown_refined_to_kg;
    }
}
