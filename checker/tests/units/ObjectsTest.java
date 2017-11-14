import org.checkerframework.checker.units.qual.*;
import org.checkerframework.framework.qual.DefaultQualifier;

class ObjectsTest {
    // null literal is always UnitsBottom

    Object field_default = null; // field default is Dimensionless

    void m() {
        Object local_var_default = null; // local var default is UnknownUnits

        @m Object meter = new @m Integer(5);

        // :: error: (assignment.type.incompatible)
        field_default = meter;
    }

    // sets default receiver, and default everything to UnknownUnits within this class
    @DefaultQualifier(UnknownUnits.class)
    class Inner {
        void m() {}
    }

    class Test {
        @UnknownUnits Inner x;

        void m() {
            // this call would give a method.invocation.invalid error if the receiver was left as Dimensionless
            x.m();
        }
    }
}
