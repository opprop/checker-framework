public class DivideByZeroTest {
    public static void f() {
        int one = 1;
        int zero = 0;
        // :: warning: (divide.by.zero)
        int x = one / zero;
        int y = zero / one;
        // :: warning: (divide.by.zero)
        int z = x / y;
        String s = "hello";
    }

    public static void h() {
        int zero_the_hard_way = 0 + 0 - 0 * 0;
        // :: warning: (divide.by.zero)
        int x = 1 / zero_the_hard_way;

        int one_the_hard_way = 0 * 1 + 1;
        int y = 1 / one_the_hard_way;
    }

    public static void l() {
        // :: warning: (divide.by.zero)
        int a = 1 / (1 - 1);
        int y = 1;
        // :: warning: (divide.by.zero)
        int x = 1 / (y - y);
        int z = y - y;
        // :: warning: (divide.by.zero)
        int k = 1 / z;
    }
}
