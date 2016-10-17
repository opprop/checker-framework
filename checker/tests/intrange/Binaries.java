import org.checkerframework.checker.intrange.qual.*;

public class Binaries {

    public void misc(int a, double b) {
        int c = a * 3;
        double d = b + 2;
    }

    public void add(
            @IntRange(from = -5, to = 5) int a,
            @IntRange(from = 10, to = 20) int b,
            @IntRange(from = 0, to = 5) short s) {
        @IntRange(from = 6, to = 25)
        //:: error: (assignment.type.incompatible)
        int plus1 = a + b; // error
        @IntRange(from = 0, to = 25)
        int plus2 = a + b; //OK

        @IntRange(from = -5, to = 10)
        int plus3 = a + s; //OK
    }

    public void subtract(@IntRange(from = -5, to = 5) int a, @IntRange(from = 10, to = 20) int b) {
        @IntRange(from = -25, to = -6)
        //:: error: (assignment.type.incompatible)
        int minus1 = a - b; //error
        @IntRange(from = -25, to = -5)
        int minus2 = a - b; //OK
    }

    public void multiply(
            @IntRange(from = -5, to = 5) int a,
            @IntRange(from = 10, to = 20) int b,
            @IntRange(from = -20, to = -10) int c) {
        @IntRange(from = -100, to = 99)
        //:: error: (assignment.type.incompatible)
        int mult1 = a * b; //error
        @IntRange(from = -100, to = 100)
        int mult2 = a * b; //OK

        @IntRange(from = -100, to = 99)
        //:: error: (assignment.type.incompatible)
        int mult3 = a * c; //error
        @IntRange(from = -100, to = 100)
        int mult4 = a * c; //OK

        @IntRange(from = -400, to = -101)
        //:: error: (assignment.type.incompatible)
        int mult5 = b * c; //error
        @IntRange(from = -400, to = -100)
        int mult6 = b * c; //OK
    }

    @SuppressWarnings("intrange:possible.division.by.zero")
    public void divide(
            @IntRange(from = 5, to = 10) int gtz,
            @IntRange(from = 0, to = 5) int gez,
            @IntRange(from = -10, to = -5) int ltz,
            @IntRange(from = -5, to = 0) int lez,
            @IntRange(from = -5, to = 5) int ze) {

        @IntRange(from = 1, to = 10)
        int s11 = gtz / gez;
        @IntRange(from = 1, to = 9)
        //:: error: (assignment.type.incompatible)
        int s12 = gtz / gez;
        @IntRange(from = 2, to = 10)
        //:: error: (assignment.type.incompatible)
        int s13 = gtz / gez;

        @IntRange(from = -10, to = -1)
        int s21 = gtz / lez;
        @IntRange(from = -9, to = -1)
        //:: error: (assignment.type.incompatible)
        int s22 = gtz / lez;
        @IntRange(from = -10, to = -2)
        //:: error: (assignment.type.incompatible)
        int s23 = gtz / lez;

        @IntRange(from = -10, to = 10)
        int s31 = gtz / ze;
        @IntRange(from = -9, to = 10)
        //:: error: (assignment.type.incompatible)
        int s32 = gtz / ze;
        @IntRange(from = -10, to = 9)
        //:: error: (assignment.type.incompatible)
        int s33 = gtz / ze;

        @IntRange(from = -10, to = -1)
        int s41 = ltz / gez;
        @IntRange(from = -9, to = -1)
        //:: error: (assignment.type.incompatible)
        int s42 = ltz / gez;
        @IntRange(from = -10, to = -2)
        //:: error: (assignment.type.incompatible)
        int s43 = ltz / gez;

        @IntRange(from = 1, to = 10)
        int s51 = ltz / lez;
        @IntRange(from = 1, to = 9)
        //:: error: (assignment.type.incompatible)
        int s52 = ltz / lez;
        @IntRange(from = 2, to = 10)
        //:: error: (assignment.type.incompatible)
        int s53 = ltz / lez;

        @IntRange(from = -10, to = 10)
        int s61 = ltz / ze;
        @IntRange(from = -9, to = 10)
        //:: error: (assignment.type.incompatible)
        int s62 = ltz / ze;
        @IntRange(from = -10, to = 9)
        //:: error: (assignment.type.incompatible)
        int s63 = ltz / ze;

        @IntRange(from = -1, to = 1)
        int s71 = ze / gtz;
        @IntRange(from = 0, to = 1)
        //:: error: (assignment.type.incompatible)
        int s72 = ze / gtz;
        @IntRange(from = -1, to = 0)
        //:: error: (assignment.type.incompatible)
        int s73 = ze / gtz;

        @IntRange(from = -1, to = 1)
        int s81 = ze / ltz;
        @IntRange(from = 0, to = 1)
        //:: error: (assignment.type.incompatible)
        int s82 = ze / ltz;
        @IntRange(from = -1, to = 0)
        //:: error: (assignment.type.incompatible)
        int s83 = ze / ltz;

        @IntRange(from = -5, to = 5)
        int s91 = ze / ze;
        @IntRange(from = -4, to = 5)
        //:: error: (assignment.type.incompatible)
        int s92 = ze / ze;
        @IntRange(from = -5, to = 4)
        //:: error: (assignment.type.incompatible)
        int s93 = ze / ze;
    }

    @SuppressWarnings("intrange:possible.division.by.zero")
    public void remainder(@IntRange(from = 5, to = 10) int a, @IntRange(from = 1, to = 7) int b) {

        @IntRange(from = -1, to = 2)
        //:: error: (assignment.type.incompatible)
        int c = a % b;

        @IntRange(from = 0, to = 10)
        int d = a % b;
    }

    @SuppressWarnings("intrange:shift.out.of.range")
    public void shiftLeft(@IntRange(from = 2, to = 10) int l, @IntRange(from = 1, to = 2) int r) {

        @IntRange(from = 8, to = 40)
        int b = l << 2;
        @IntRange(from = 9, to = 40)
        //:: error: (assignment.type.incompatible)
        int c = l << 2;
        @IntRange(from = 8, to = 39)
        //:: error: (assignment.type.incompatible)
        int d = l << 2;

        @IntRange(from = 4, to = 40)
        int e = l << r;
        @IntRange(from = 5, to = 40)
        //:: error: (assignment.type.incompatible)
        int f = l << r;
        @IntRange(from = 4, to = 39)
        //:: error: (assignment.type.incompatible)
        int g = l << r;

        int h = l << -1;
        @IntRange(from = -2000000L, to = 2000000L)
        //:: error: (assignment.type.incompatible)
        int i = l << -1;
    }

    @SuppressWarnings("intrange:shift.out.of.range")
    public void signedShiftRight(
            @IntRange(from = -8, to = -3) int l, @IntRange(from = 1, to = 2) int r) {

        @IntRange(from = -4, to = -1)
        int a = l >> r;
        @IntRange(from = -3, to = -1)
        //:: error: (assignment.type.incompatible)
        int b = l >> r;
        @IntRange(from = -4, to = -2)
        //:: error: (assignment.type.incompatible)
        int c = l >> r;
    }
}
