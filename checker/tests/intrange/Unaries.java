import org.checkerframework.checker.intrange.qual.*;

class Unaries {

    public void complememnt(@IntRange(from = -2, to = 10) int a) {
        @IntRange(from = -11, to = 1)
        int b = ~a;
        @IntRange(from = -10, to = 1)
        //:: error: (assignment.type.incompatible)
        int c = ~a;
        @IntRange(from = -11, to = 0)
        //:: error: (assignment.type.incompatible)
        int d = ~a;
    }

    public void unaryPlusMinus(@IntRange(from = -2, to = 10) int a) {
        @IntRange(from = -2, to = 10)
        int b = +a;
        @IntRange(from = -10, to = 2)
        int c = -a;
    }
}
