public class Comparison {

    public static void g(int y) {
        if (y == 0) {
            // :: warning: (divide.by.zero)
            int x = 1 / y;
        } else {
            // False positive
            // :: warning: (divide.by.zero)
            int x = 1 / y;
        }

        if (y != 0) {
            // False positive
            // :: warning: (divide.by.zero)
            int x = 1 / y;
        } else {
            // :: warning: (divide.by.zero)
            int x = 1 / y;
        }

        if (!(y == 0)) {
            // :: warning: (divide.by.zero)
            int x = 1 / y;
        } else {
            // False positive
            // :: warning: (divide.by.zero)
            int x = 1 / y;
        }

        if (!(y != 0)) {
            // False positive
            // :: warning: (divide.by.zero)
            int x = 1 / y;
        } else {
            // :: warning: (divide.by.zero)
            int x = 1 / y;
        }

        if (y < 0) {
            int x = 1 / y;
        } else {
            // :: warning: (divide.by.zero)
            int x = 1 / y;
        }

        if (y <= 0) {
            // :: warning: (divide.by.zero)
            int x = 1 / y;
        } else {
            int x = 1 / y;
        }

        if (y > 0) {
            int x = 1 / y;
        } else {
            // :: warning: (divide.by.zero)
            int x = 1 / y;
        }

        if (y >= 0) {
            // :: warning: (divide.by.zero)
            int x = 1 / y;
        } else {
            int x = 1 / y;
        }
    }
}
