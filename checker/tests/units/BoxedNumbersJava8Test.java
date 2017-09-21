import org.checkerframework.checker.units.UnitsTools;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.mPERs;
import org.checkerframework.checker.units.qual.time.duration.s;

// @below-java8-jdk-skip-test
// The following tests only the new methods introduced in Java 8 to the boxed
// number classes

class BoxedNumbersJava8Test {

    void ByteTest() {
        @SuppressWarnings({"units"})
        @m byte meterByte = (byte) (100 * UnitsTools.m);

        @Dimensionless int hash = Byte.hashCode(meterByte);

        @m int meterInt = Byte.toUnsignedInt(meterByte);
        @m long meterLong = Byte.toUnsignedLong(meterByte);
    }

    void ShortTest() {
        @SuppressWarnings({"units"})
        @m short meterShort = (short) (100 * UnitsTools.m);

        @Dimensionless int hash = Short.hashCode(meterShort);

        @m int meterInt = Short.toUnsignedInt(meterShort);
        @m long meterLong = Short.toUnsignedLong(meterShort);
    }

    void IntegerTest() {
        @m int meterInteger = 100 * UnitsTools.m;
        @s int secondInteger = 100 * UnitsTools.s;

        String x = Integer.toUnsignedString(meterInteger);
        x = Integer.toUnsignedString(meterInteger, 20);

        @Dimensionless int hash = Integer.hashCode(meterInteger);

        @Dimensionless int r = Integer.compareUnsigned(meterInteger, meterInteger);
        //:: error: (comparison.unit.mismatch)
        r = Integer.compareUnsigned(meterInteger, secondInteger);

        @m long meterLong = Integer.toUnsignedLong(meterInteger);

        @mPERs int mps = Integer.divideUnsigned(meterInteger, secondInteger);
        meterInteger = Integer.remainderUnsigned(meterInteger, secondInteger);

        meterInteger = Integer.sum(meterInteger, meterInteger);
        meterInteger = Integer.max(meterInteger, meterInteger);
        meterInteger = Integer.min(meterInteger, meterInteger);
    }

    void LongTest() {
        @m long meterLong = 100l * UnitsTools.m;
        @s long secondLong = 100l * UnitsTools.s;

        String x = Long.toUnsignedString(meterLong);
        x = Long.toUnsignedString(meterLong, 20);

        @Dimensionless int hash = Long.hashCode(meterLong);

        @Dimensionless int r = Long.compareUnsigned(meterLong, meterLong);
        //:: error: (comparison.unit.mismatch)
        r = Long.compareUnsigned(meterLong, secondLong);

        @mPERs long mps = Long.divideUnsigned(meterLong, secondLong);
        meterLong = Long.remainderUnsigned(meterLong, secondLong);

        meterLong = Long.sum(meterLong, meterLong);
        meterLong = Long.max(meterLong, meterLong);
        meterLong = Long.min(meterLong, meterLong);
    }

    void FloatTest() {
        @m float meterFloat = 100f * UnitsTools.m;

        @Dimensionless int hash = Float.hashCode(meterFloat);
        boolean b = Float.isFinite(meterFloat);

        meterFloat = Float.sum(meterFloat, meterFloat);
        meterFloat = Float.max(meterFloat, meterFloat);
        meterFloat = Float.min(meterFloat, meterFloat);
    }

    void DoubleTest() {
        @m double meterDouble = 100d * UnitsTools.m;

        @Dimensionless int hash = Double.hashCode(meterDouble);
        boolean b = Double.isFinite(meterDouble);

        meterDouble = Double.sum(meterDouble, meterDouble);
        meterDouble = Double.max(meterDouble, meterDouble);
        meterDouble = Double.min(meterDouble, meterDouble);
    }
}
