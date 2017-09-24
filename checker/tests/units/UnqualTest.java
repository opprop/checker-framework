import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.checker.units.qual.kg;

// Tests for unqualified assignments
class UnqualTest {
    // Fields are by default @Dimensionless, inference should transfer units for
    // UnknownUnits, but not dimensionless
    int dimensionless = 5;

    //:: error: (assignment.type.incompatible)
    @kg int kg = dimensionless;

    // inferred to be kg
    @UnknownUnits int inferredKg = kg;
    // accepted due to inference of kg unit
    @kg int alsoKg = inferredKg;

    int alsoDimensionless = dimensionless;

    //:: error: (assignment.type.incompatible)
    @kg int kg2 = dimensionless;
    //:: error: (assignment.type.incompatible)
    @Dimensionless int notKg = kg2;
    //:: error: (assignment.type.incompatible)
    @kg int alsokg2 = notKg;

    void m() {
        // Local Variables are by default @UnknownUnits, inference should
        // transfer units for UnknownUnits

        // primitive number literals are by default @Dimensionless
        // this is inferred to be dimensionless
        int inferredDimensionless = 5;

        //:: error: (assignment.type.incompatible)
        @kg int kgLV = inferredDimensionless;

        // inferred to be kg
        int inferredKgLV = kgLV;
        // accepted due to inference of kg unit
        @kg int alsokgLV = inferredKgLV;

        @Dimensionless int dimensionless = inferredDimensionless;

        //:: error: (assignment.type.incompatible)
        @kg int kg2 = inferredDimensionless;
        //:: error: (assignment.type.incompatible)
        @Dimensionless int notKg = kg2;
        //:: error: (assignment.type.incompatible)
        @kg int alsokg2 = notKg;
    }
}
