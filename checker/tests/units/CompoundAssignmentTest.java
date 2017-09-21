import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.time.duration.s;

class CompoundAssignmentTest {
    @m int m;
    @s int s;

    float f;
    double d;
    long l;
    int i;

    void visitorTest() {
        m += m;
        s -= s;

        //:: error: (compound.assignment.type.incompatible)
        m += s;
        //:: error: (compound.assignment.type.incompatible)
        s -= m;

        m *= 10;
        s /= 30;

        //:: error: (compound.assignment.type.incompatible)
        m *= m;
        //:: error: (compound.assignment.type.incompatible)
        s /= s;

        //:: error: (compound.assignment.type.incompatible)
        s *= m;
        //:: error: (compound.assignment.type.incompatible)
        m /= s;
    }

    void atfTest() {
        m = m += m;
        s = s -= s;

        //:: error: (compound.assignment.type.incompatible)
        m = m += s;
        //:: error: (compound.assignment.type.incompatible)
        s = s -= m;

        m = m *= 20;
        s = s /= 80;

        //:: error: (compound.assignment.type.incompatible)
        s = s *= m;
        //:: error: (compound.assignment.type.incompatible)
        m = m /= s;
    }

    void twoLevelTest() {
        s = s += s -= s;
        m = m -= m += m;

        //:: error: (compound.assignment.type.incompatible)
        s = s += m -= s;
        //:: error: (compound.assignment.type.incompatible)
        s = s += s -= m;

        //:: error: (compound.assignment.type.incompatible)
        m = m *= m /= 10;
        //:: error: (compound.assignment.type.incompatible)
        m = m *= m /= m;
    }

    void manyCallsTest() {
        // TODO: currently, compound assignment errors must be issued in both UnitsVisitor and UnitsATF
        // figure out why some of the examples below don't get a check via visitor.

        // this gets 1 call to UnitsVisitor.visitCompoundAssignment
        // m += s;

        // this gets 1 call to UnitsVisitor.visitCompoundAssignment and 2 calls to UnitsATF.visitCompoundAssignment
        // m = m += s;     // visitor and atf check

        // this gets 3 calls to UnitsATF.visitCompoundAssignment
        // m += m += s;

        // this gets 8 calls to UnitsATF.visitCompoundAssignment
        // m += m += m += s;
    }
}
