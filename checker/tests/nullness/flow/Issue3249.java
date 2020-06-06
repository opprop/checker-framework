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
}
