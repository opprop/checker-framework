import org.checkerframework.checker.intrange.qual.*;

class Implicit {

    public void longTest() {
        @IntRange(from = 0, to = 255)
        //:: error: (assignment.type.incompatible)
        long l0 = -1L;
        @IntRange(from = 0, to = 255)
        long l1 = 100L;
        @IntRange(from = 0, to = 255)
        //:: error: (assignment.type.incompatible)
        long l2 = 1000L;
    }

    public void intTest() {
        @IntRange(from = 0, to = 255)
        //:: error: (assignment.type.incompatible)
        int i0 = -1;
        @IntRange(from = 0, to = 255)
        int i1 = 100;
        @IntRange(from = 0, to = 255)
        //:: error: (assignment.type.incompatible)
        int i2 = 1000;
    }

    void charTest() {
        @IntRange(from = 'Z', to = 'b')
        //:: error: (assignment.type.incompatible)
        int c0 = 'A'; // 'A' = 65;
        @IntRange(from = 'Z', to = 'b')
        int c1 = 'a'; // 'a' = 97;
        @IntRange(from = 'Z', to = 'b')
        //:: error: (assignment.type.incompatible)
        int c2 = 'z'; // 'z' = 122;
    }
}
