import org.checkerframework.common.value.qual.IntVal;

public class DeadBranch {
    @IntVal(1) int dead_branch_equal() {
        int x = 0;
        int y = 0;
        if (x == 0) {
            y = 1;
        }
        return y;
    }

    @IntVal(0) int dead_branch_not_equal() {
        int x = 0;
        int y = 0;
        if (x != 0) {
            y = 1;
        }
        return y;
    }

    @IntVal(0) int dead_branch_greater() {
        int x = 0;
        int y = 0;
        if (x > 0) {
            y = 1;
        }
        return y;
    }

    @IntVal(1) int dead_branch_greater_equal() {
        int x = 0;
        int y = 0;
        if (x >= 0) {
            y = 1;
        }
        return y;
    }

    @IntVal(0) int dead_branch_less() {
        int x = 0;
        int y = 0;
        if (x < 0) {
            y = 1;
        }
        return y;
    }

    @IntVal(1) int dead_branch_less_equal() {
        int x = 0;
        int y = 0;
        if (x <= 0) {
            y = 1;
        }
        return y;
    }

    @IntVal(0) int dead_branch_conditional_not() {
        int x = 0;
        int y = 0;
        if (!(x == 0)) {
            y = 1;
        }
        return y;
    }

    @IntVal(0) int dead_branch_conditional_and() {
        int x = 0;
        int y = 0;
        if (x == 0 && x != 0) {
            y = 1;
        }
        return y;
    }

    @IntVal(1) int dead_branch_conditional_or() {
        int x = 0;
        int y = 0;
        if (x == 0 || x != 0) {
            y = 1;
        }
        return y;
    }
}
