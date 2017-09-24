import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.time.duration.s;

class ComparisonTest {
    @m int m;
    @s int s;

    void special() {
        if (m == 5) ;
        if (5 == s) ;
        @m Integer x = null;
        if (x != null) ;
        if (null != x) ;
    }

    void eq() {
        if (m == m) ;
        if (s == s) ;
        //:: error: (comparison.unit.mismatch)
        if (m == s) ;

        @UnknownUnits int k = m == m ? m : s;
        //:: error: (comparison.unit.mismatch)
        k = m == s ? m : s;
    }

    void neq() {
        if (m != m) ;
        if (s != s) ;
        //:: error: (comparison.unit.mismatch)
        if (m != s) ;

        @UnknownUnits int k = m != m ? m : s;
        //:: error: (comparison.unit.mismatch)
        k = m != s ? m : s;
    }

    void lt() {
        if (m < m) ;
        if (s < s) ;
        //:: error: (comparison.unit.mismatch)
        if (m < s) ;

        @UnknownUnits int k = m < m ? m : s;
        //:: error: (comparison.unit.mismatch)
        k = m < s ? m : s;
    }

    void gt() {
        if (m > m) ;
        if (s > s) ;
        //:: error: (comparison.unit.mismatch)
        if (m > s) ;

        @UnknownUnits int k = m > m ? m : s;
        //:: error: (comparison.unit.mismatch)
        k = m > s ? m : s;
    }

    void le() {
        if (m <= m) ;
        if (s <= s) ;
        //:: error: (comparison.unit.mismatch)
        if (m <= s) ;

        @UnknownUnits int k = m <= m ? m : s;
        //:: error: (comparison.unit.mismatch)
        k = m <= s ? m : s;
    }

    void ge() {
        if (m >= m) ;
        if (s >= s) ;
        //:: error: (comparison.unit.mismatch)
        if (m >= s) ;

        @UnknownUnits int k = m >= m ? m : s;
        //:: error: (comparison.unit.mismatch)
        k = m >= s ? m : s;
    }
}
