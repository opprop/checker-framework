import org.checkerframework.checker.intrange.qual.*;

class Basics {

    public void IntegerTest() {
        Integer d = new Integer(0);

        @IntRange(from = 0, to = 2)
        Integer b = d;

        @IntRange(from = 1, to = 1)
        //:: error: (assignment.type.incompatible)
        Integer c = d;
    }
}
