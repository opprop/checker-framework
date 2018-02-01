import org.checkerframework.checker.units.qual.*;
import org.checkerframework.framework.qual.DefaultQualifier;

class ObjectsTest {
    // null literal is always UnitsBottom

    Object field_default = null; // field default is Dimensionless

    // ensure fields can be declared with units
    @m Integer boxedMeter;

    void m() {
        Object local_var_default = null; // local var default is UnknownUnits

        // ensure local references can be declared with units
        @m Object meter;

        // ensure that objects can be created with units
        meter = new @m Integer(5);
        boxedMeter = new @m Integer(5);

        @s Integer boxedLocalSecond = new @s Integer(10);

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
            // this call would give a method.invocation.invalid error if the receiver was not set to
            // UnknownUnits above
            x.m();
        }
    }
}
