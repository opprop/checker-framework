public class FalsePositive {
    int false_positive(int y) {
        int x = 0;
        int z = 0;
        if (y == 1) {
            x = 1;
        } else {
            z = 1;
        }
        // :: warning: (divide.by.zero)
        return 10 / (x - z);
    }
}
