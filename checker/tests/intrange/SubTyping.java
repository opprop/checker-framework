import org.checkerframework.checker.intrange.qual.*;

class SubTyping {

    public void subTypingTest(
            @FullIntRange int fr,
            @IntRange(from = 0, to = 255) int ir,
            @IntRange int mir,
            int dir) {

        // Assign to top always good
        @FullIntRange int a = fr;
        @FullIntRange int b = ir;
        @FullIntRange int c = mir;

        // test 3 non-overlapping scenarios
        @IntRange(from = 0, to = 65535)
        //:: error: (assignment.type.incompatible)
        int d = fr; //error
        @IntRange(from = 0, to = 65535)
        int e = ir;
        @IntRange(from = 0, to = 65535)
        //:: error: (assignment.type.incompatible)
        int f = mir; //error

        // test IntRange qualifier default parameters
        //:: error: (assignment.type.incompatible)
        @IntRange int g = fr; //error
        @IntRange int h = ir;
        @IntRange int i = mir;

        // test overlapping scenarios
        @IntRange(from = -255, to = 128)
        //:: error: (assignment.type.incompatible)
        int k = ir; //error
        @IntRange(from = 128, to = 1000)
        //:: error: (assignment.type.incompatible)
        int l = ir; //error
        @IntRange(from = 64, to = 128)
        //:: error: (assignment.type.incompatible)
        int m = ir; //error

        // test edge cases
        @IntRange(from = 0, to = 255)
        int n = ir;
        @IntRange(from = 0, to = 300)
        int o = ir;
        @IntRange(from = -1, to = 255)
        int p = ir;
        @IntRange(from = 0, to = 200)
        //:: error: (assignment.type.incompatible)
        int q = ir; //error
        @IntRange(from = 1, to = 255)
        //:: error: (assignment.type.incompatible)
        int r = ir; //error

        // test default qualifier
        int s = fr;
        int t = ir;
        int u = mir;
        @IntRange(from = 0, to = 255)
        //:: error: (assignment.type.incompatible)
        int v = dir; //error
    }
}
