import org.checkerframework.checker.nullness.qual.Nullable;

public class IfTrue {

    Object foo(@Nullable Object myObj, boolean x) {
        if (x) {
            myObj = new Object();
        }
        return myObj;
    }
}
