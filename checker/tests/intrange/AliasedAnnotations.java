import android.support.annotation.*;

class AliasedAnnotations {

    void useIntRangeAnnotation() {
        @android.support.annotation.IntRange(from = 0, to = 10)
        //:: error: (assignment.type.incompatible)
        int i = 12;

        @IntRange(from = 0, to = 10)
        //:: error: (assignment.type.incompatible)
        int j = 13;

        @IntRange(from = 0)
        int k = 10;
    }
}
