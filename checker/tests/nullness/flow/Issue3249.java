public class Issue3249 {

    private final double field;

    Issue3249() {
        double local;
        while (true) {
            local = 1;
            break;
        }
        field = local;
    }

    Issue3249(int x) {
        double local;
        while (!false) {
            local = 1;
            break;
        }
        field = local;
    }

    Issue3249(float x) {
        double local;
        while (true || x > 0) {
            local = 1;
            break;
        }
        // :: error: (assignment.type.incompatible)
        field = local;
    }

    Issue3249(double x) {
        double local;
        while (!false && true && !false) {
            local = 1;
            break;
        }
        field = local;
    }
}
