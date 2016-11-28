import org.checkerframework.checker.intrange.qual.*;

class Methods {

    public void simple() {
        @IntRange(from = 0, to = 15)
        //:: error: (assignment.type.incompatible)
        int a = getNumber();

        char b = getChar();
    }

    @IntRange(from = 0, to = 20)
    public int getNumber() {
        //:: error: (return.type.incompatible)
        return 100;
    }

    @IntRange(from = 'a', to = 'z')
    public char getChar() {
        return 'c';
    }
}
