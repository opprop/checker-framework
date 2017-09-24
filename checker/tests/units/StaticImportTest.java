import static org.checkerframework.checker.units.UnitsTools.s;

import org.checkerframework.checker.units.UnitsTools;
import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.time.duration.s;

class Units {
    // The advantage of using the multiplication with a unit is that
    // also double, float, etc. are easily handled and we don't need
    // to end a huge number of methods to UnitsTools.
    @m double dm = 9.34d * UnitsTools.m;

    // With a static import:
    @s float time = 5.32f * s;
}
