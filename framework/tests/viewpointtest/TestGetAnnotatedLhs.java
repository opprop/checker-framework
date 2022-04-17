import viewpointtest.quals.A;
import viewpointtest.quals.B;
import viewpointtest.quals.ReceiverDependentQual;
import viewpointtest.quals.Top;

@ReceiverDependentQual
class TestGetAnnotatedLhs {
    @ReceiverDependentQual Object f;

    @SuppressWarnings({
        "inconsistent.constructor.type",
        "super.invocation.invalid",
        "cast.unsafe.constructor.invocation"
    })
    @ReceiverDependentQual
    TestGetAnnotatedLhs() {
        this.f = new @ReceiverDependentQual Object();
    }

    @SuppressWarnings({"cast.unsafe.constructor.invocation"})
    void test1() {
        TestGetAnnotatedLhs a = new @A TestGetAnnotatedLhs();
        TestGetAnnotatedLhs top = new @Top TestGetAnnotatedLhs();
        top = a;
        // :: error: (assignment.type.incompatible)
        top.f = new @B Object();
    }
}
