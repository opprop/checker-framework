import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TestTernary {

    public class ImmutableIntList {

        @Override
        public boolean equals(@Nullable Object obj) {
            return obj instanceof ImmutableIntList ? obj instanceof List : obj instanceof List;
        }
    }
}
