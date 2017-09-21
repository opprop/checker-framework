import org.checkerframework.checker.units.UnitsTools;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.time.duration.s;

class BoxedNumbersTest {
    void ByteTest() {
        @Dimensionless byte dimensionlessByte = (byte) 100;
        @SuppressWarnings({"units"})
        @m byte meterByte = (byte) (100 * UnitsTools.m);
        @SuppressWarnings({"units"})
        @s byte secondByte = (byte) (100 * UnitsTools.s);

        Byte unknownByteBox = new Byte((byte) 30);
        @Dimensionless Byte dimensionlessByteBox = new @Dimensionless Byte(dimensionlessByte);
        //:: error: (assignment.type.incompatible)
        @Dimensionless Byte dimensionlessByteBox2 = new @UnknownUnits Byte(dimensionlessByte);
        @s Byte secondByteBox = new @s Byte((byte) 30);
        //:: error: (assignment.type.incompatible) :: error: (constructor.invocation.invalid)
        @m Byte meterByteBox = new Byte(meterByte);
        @m Byte meterByteBox2 = new @m Byte((byte) 50);
        @m Byte meterByteBox3 = meterByte; // auto boxing
        // valueOf construction
        @m Byte meterByteBox4 = Byte.valueOf(meterByte);
        //:: error: (assignment.type.incompatible)
        @m Byte meterByteBox4Bad = Byte.valueOf(dimensionlessByte);
        // cannot force units conversion through constructor
        //:: error: (constructor.invocation.invalid)
        @m Byte meterByteBox5 = new @m Byte(secondByte);

        meterByte = meterByteBox; // auto unboxing
        //:: error: (assignment.type.incompatible)
        meterByte = dimensionlessByteBox;

        @m byte mByte = meterByteBox.byteValue();
        @m short mShort = meterByteBox.shortValue();
        @m int mInteger = meterByteBox.intValue();
        @m long mLong = meterByteBox.longValue();
        @m float mFloat = meterByteBox.floatValue();
        @m double mDouble = meterByteBox.doubleValue();

        String x = meterByteBox.toString();
        x = Byte.toString(meterByte);
        int hash = meterByteBox.hashCode();

        meterByteBox.equals(meterByteBox2);
        // comparison with dimensionless literals and objects, and null literals are
        // allowed
        meterByteBox.equals(5);
        meterByteBox.equals(dimensionlessByteBox);
        meterByteBox.equals(null);
        // otherwise comparison must be between the same unit
        //:: error: (comparison.unit.mismatch)
        meterByteBox.equals(secondByteBox);

        @Dimensionless int r = meterByteBox.compareTo(meterByteBox2);
        //:: error: (comparison.unit.mismatch)
        r = meterByteBox.compareTo(secondByteBox);

        r = Byte.compare(meterByte, meterByte);
        //:: error: (comparison.unit.mismatch)
        r = Byte.compare(meterByte, secondByte);
    }

    void ShortTest() {
        @Dimensionless short dimensionlessShort = 100;
        @SuppressWarnings({"units"})
        @m short meterShort = (short) (100 * UnitsTools.m);
        @SuppressWarnings({"units"})
        @s short secondShort = (short) (100 * UnitsTools.s);

        @Dimensionless Short dimensionlessShortBox = new @Dimensionless Short(dimensionlessShort);
        @s Short secondShortBox = new @s Short((short) 30);
        //:: error: (assignment.type.incompatible) :: error: (constructor.invocation.invalid)
        @m Short meterShortBox = new Short(meterShort);
        @m Short meterShortBox2 = new @m Short((short) 50);
        @m Short meterShortBox3 = meterShort; // auto boxing
        // valueOf construction
        @m Short meterShortBox4 = Short.valueOf(meterShort);
        //:: error: (assignment.type.incompatible)
        @m Short meterShortBox4Bad = Short.valueOf(dimensionlessShort);
        // cannot force units conversion through constructor
        //:: error: (constructor.invocation.invalid)
        @m Short meterShortBox5 = new @m Short(secondShort);

        meterShort = meterShortBox; // auto unboxing
        //:: error: (assignment.type.incompatible)
        meterShort = dimensionlessShortBox;

        @m byte mByte = meterShortBox.byteValue();
        @m short mShort = meterShortBox.shortValue();
        @m int mInteger = meterShortBox.intValue();
        @m long mLong = meterShortBox.longValue();
        @m float mFloat = meterShortBox.floatValue();
        @m double mDouble = meterShortBox.doubleValue();

        String x = meterShortBox.toString();
        x = Short.toString(meterShort);
        int hash = meterShortBox.hashCode();

        meterShortBox.equals(meterShortBox2);
        // comparison with dimensionless literals and objects, and null literals are
        // allowed
        meterShortBox.equals(5);
        meterShortBox.equals(dimensionlessShortBox);
        meterShortBox.equals(null);
        // otherwise comparison must be between the same unit
        //:: error: (comparison.unit.mismatch)
        meterShortBox.equals(secondShortBox);

        @Dimensionless int r = meterShortBox.compareTo(meterShortBox2);
        //:: error: (comparison.unit.mismatch)
        r = meterShortBox.compareTo(secondShortBox);

        r = Short.compare(meterShort, meterShort);
        //:: error: (comparison.unit.mismatch)
        r = Short.compare(meterShort, secondShort);

        meterShort = Short.reverseBytes(meterShort);
    }

    void IntegerTest() {
        @Dimensionless int dimensionlessInteger = 100;
        @m int meterInteger = 100 * UnitsTools.m;
        @s int secondInteger = 100 * UnitsTools.s;

        @Dimensionless
        Integer dimensionlessIntegerBox = new @Dimensionless Integer(dimensionlessInteger);
        @s Integer secondIntegerBox = new @s Integer(30);
        //:: error: (assignment.type.incompatible) :: error: (constructor.invocation.invalid)
        @m Integer meterIntegerBox = new Integer(meterInteger);
        @m Integer meterIntegerBox2 = new @m Integer(50);
        @m Integer meterIntegerBox3 = meterInteger; // auto boxing
        // valueOf construction
        @m Integer meterIntegerBox4 = Integer.valueOf(meterInteger);
        //:: error: (assignment.type.incompatible)
        @m Integer meterIntegerBox4Bad = Integer.valueOf(dimensionlessInteger);
        // cannot force units conversion through constructor
        //:: error: (constructor.invocation.invalid)
        @m Integer meterIntegerBox5 = new @m Integer(secondInteger);

        meterInteger = meterIntegerBox; // auto unboxing
        //:: error: (assignment.type.incompatible)
        meterInteger = dimensionlessIntegerBox;

        @m byte mByte = meterIntegerBox.byteValue();
        @m short mShort = meterIntegerBox.shortValue();
        @m int mInteger = meterIntegerBox.intValue();
        @m long mLong = meterIntegerBox.longValue();
        @m float mFloat = meterIntegerBox.floatValue();
        @m double mDouble = meterIntegerBox.doubleValue();

        String x = meterIntegerBox.toString();
        x = Integer.toString(meterInteger);
        x = Integer.toString(meterInteger, 16);
        x = Integer.toHexString(meterInteger);
        x = Integer.toOctalString(meterInteger);
        x = Integer.toBinaryString(meterInteger);

        int hash = meterIntegerBox.hashCode();

        meterIntegerBox = Integer.getInteger("test", meterInteger);
        meterIntegerBox = Integer.getInteger("test", meterIntegerBox);

        meterIntegerBox.equals(meterIntegerBox2);
        // comparison with dimensionless literals and objects, and null literals are
        // allowed
        meterIntegerBox.equals(5);
        meterIntegerBox.equals(dimensionlessIntegerBox);
        meterIntegerBox.equals(null);
        // otherwise comparison must be between the same unit
        //:: error: (comparison.unit.mismatch)
        meterIntegerBox.equals(secondIntegerBox);

        @Dimensionless int r = meterIntegerBox.compareTo(meterIntegerBox2);
        //:: error: (comparison.unit.mismatch)
        r = meterIntegerBox.compareTo(secondIntegerBox);

        r = Integer.compare(meterInteger, meterInteger);
        //:: error: (comparison.unit.mismatch)
        r = Integer.compare(meterInteger, secondInteger);

        meterInteger = Integer.highestOneBit(meterInteger);
        meterInteger = Integer.lowestOneBit(meterInteger);

        int y = Integer.numberOfLeadingZeros(meterInteger);
        y = Integer.numberOfTrailingZeros(meterInteger);
        y = Integer.bitCount(meterInteger);

        meterInteger = Integer.rotateLeft(meterInteger, 5);
        meterInteger = Integer.rotateRight(meterInteger, 5);
        meterInteger = Integer.reverse(meterInteger);
        y = Integer.signum(meterInteger);
        meterInteger = Integer.reverseBytes(meterInteger);
    }

    void LongTest() {
        @Dimensionless long dimensionlessLong = 100l;
        @m long meterLong = 100l * UnitsTools.m;
        @s long secondLong = 100l * UnitsTools.s;

        @Dimensionless Long dimensionlessLongBox = new @Dimensionless Long(dimensionlessLong);
        @s Long secondLongBox = new @s Long(30l);
        //:: error: (assignment.type.incompatible) :: error: (constructor.invocation.invalid)
        @m Long meterLongBox = new Long(meterLong);
        @m Long meterLongBox2 = new @m Long(50l);
        @m Long meterLongBox3 = meterLong; // auto boxing
        // valueOf construction
        @m Long meterLongBox4 = Long.valueOf(meterLong);
        //:: error: (assignment.type.incompatible)
        @m Long meterLongBox4Bad = Long.valueOf(dimensionlessLong);
        // cannot force units conversion through constructor
        //:: error: (constructor.invocation.invalid)
        @m Long meterLongBox5 = new @m Long(secondLong);

        meterLong = meterLongBox; // auto unboxing
        //:: error: (assignment.type.incompatible)
        meterLong = dimensionlessLongBox;

        @m byte mByte = meterLongBox.byteValue();
        @m short mShort = meterLongBox.shortValue();
        @m int mInteger = meterLongBox.intValue();
        @m long mLong = meterLongBox.longValue();
        @m float mFloat = meterLongBox.floatValue();
        @m double mDouble = meterLongBox.doubleValue();

        String x = meterLongBox.toString();
        x = Long.toString(meterLong);
        x = Long.toString(meterLong, 16);
        x = Long.toHexString(meterLong);
        x = Long.toOctalString(meterLong);
        x = Long.toBinaryString(meterLong);

        int hash = meterLongBox.hashCode();

        meterLongBox = Long.getLong("test", meterLong);
        meterLongBox = Long.getLong("test", meterLongBox);

        meterLongBox.equals(meterLongBox2);
        // comparison with dimensionless literals and objects, and null literals are
        // allowed
        meterLongBox.equals(5);
        meterLongBox.equals(dimensionlessLongBox);
        meterLongBox.equals(null);
        // otherwise comparison must be between the same unit
        //:: error: (comparison.unit.mismatch)
        meterLongBox.equals(secondLongBox);

        @Dimensionless int r = meterLongBox.compareTo(meterLongBox2);
        //:: error: (comparison.unit.mismatch)
        r = meterLongBox.compareTo(secondLongBox);

        r = Long.compare(meterLong, meterLong);
        //:: error: (comparison.unit.mismatch)
        r = Long.compare(meterLong, secondLong);

        meterLong = Long.highestOneBit(meterLong);
        meterLong = Long.lowestOneBit(meterLong);

        long y = Long.numberOfLeadingZeros(meterLong);
        y = Long.numberOfTrailingZeros(meterLong);
        y = Long.bitCount(meterLong);

        meterLong = Long.rotateLeft(meterLong, 5);
        meterLong = Long.rotateRight(meterLong, 5);
        meterLong = Long.reverse(meterLong);
        y = Long.signum(meterLong);
        meterLong = Long.reverseBytes(meterLong);
    }

    void FloatTest() {
        @Dimensionless float dimensionlessFloat = 100.0f;
        @m float meterFloat = 100f * UnitsTools.m;
        @s float secondFloat = 100f * UnitsTools.s;
        @m double meterDouble = 100d * UnitsTools.m;
        @s double secondDouble = 100d * UnitsTools.s;

        @Dimensionless Float dimensionlessFloatBox = new @Dimensionless Float(dimensionlessFloat);
        @s Float secondFloatBox = new @s Float(30.0f);
        //:: error: (assignment.type.incompatible) :: error: (constructor.invocation.invalid)
        @m Float meterFloatBox = new Float(meterFloat);
        @m Float meterFloatBox2 = new @m Float(50.0f);
        @m Float meterFloatBox3 = meterFloat; // auto boxing
        // valueOf construction
        @m Float meterFloatBox4 = Float.valueOf(meterFloat);
        //:: error: (assignment.type.incompatible)
        @m Float meterFloatBox4Bad = Float.valueOf(dimensionlessFloat);
        // cannot force units conversion through constructor
        //:: error: (constructor.invocation.invalid)
        @m Float meterFloatBox5 = new @m Float(secondFloat);
        @m Float meterFloatBoxFromDouble = new @m Float(meterDouble);
        //:: error: (constructor.invocation.invalid)
        @m Float meterFloatBoxFromDoubleBad = new @m Float(secondDouble);

        meterFloat = meterFloatBox; // auto unboxing
        //:: error: (assignment.type.incompatible)
        meterFloat = dimensionlessFloatBox;

        @m byte mByte = meterFloatBox.byteValue();
        @m short mShort = meterFloatBox.shortValue();
        @m int mInteger = meterFloatBox.intValue();
        @m long mLong = meterFloatBox.longValue();
        @m float mFloat = meterFloatBox.floatValue();
        @m double mDouble = meterFloatBox.doubleValue();

        String x = meterFloatBox.toString();
        x = Float.toString(meterFloat);
        x = Float.toHexString(meterFloat);

        int hash = meterFloatBox.hashCode();

        boolean b = meterFloatBox.isNaN();
        b = Float.isNaN(meterFloat);
        b = meterFloatBox.isInfinite();
        b = Float.isInfinite(meterFloat);

        meterFloatBox.equals(meterFloatBox2);
        // comparison with dimensionless literals and objects, and null literals are
        // allowed
        meterFloatBox.equals(5);
        meterFloatBox.equals(dimensionlessFloatBox);
        meterFloatBox.equals(null);
        // otherwise comparison must be between the same unit
        //:: error: (comparison.unit.mismatch)
        meterFloatBox.equals(secondFloatBox);

        @Dimensionless int r = meterFloatBox.compareTo(meterFloatBox2);
        //:: error: (comparison.unit.mismatch)
        r = meterFloatBox.compareTo(secondFloatBox);

        r = Float.compare(meterFloat, meterFloat);
        //:: error: (comparison.unit.mismatch)
        r = Float.compare(meterFloat, secondFloat);

        @m int meterInt = Float.floatToIntBits(meterFloat);
        meterInt = Float.floatToRawIntBits(meterFloat);
        meterFloat = Float.intBitsToFloat(meterInt);
    }

    void DoubleTest() {
        @Dimensionless double dimensionlessDouble = 100.0d;
        @m double meterDouble = 100d * UnitsTools.m;
        @s double secondDouble = 100d * UnitsTools.s;

        @Dimensionless
        Double dimensionlessDoubleBox = new @Dimensionless Double(dimensionlessDouble);
        @s Double secondDoubleBox = new @s Double(30.0d);
        //:: error: (assignment.type.incompatible) :: error: (constructor.invocation.invalid)
        @m Double meterDoubleBox = new Double(meterDouble);
        @m Double meterDoubleBox2 = new @m Double(50.0d);
        @m Double meterDoubleBox3 = meterDouble; // auto boxing
        // valueOf construction
        @m Double meterDoubleBox4 = Double.valueOf(meterDouble);
        //:: error: (assignment.type.incompatible)
        @m Double meterDoubleBox4Bad = Double.valueOf(dimensionlessDouble);
        // cannot force units conversion through constructor
        //:: error: (constructor.invocation.invalid)
        @m Double meterDoubleBox5 = new @m Double(secondDouble);

        meterDouble = meterDoubleBox; // auto unboxing
        //:: error: (assignment.type.incompatible)
        meterDouble = dimensionlessDoubleBox;

        @m byte mByte = meterDoubleBox.byteValue();
        @m short mShort = meterDoubleBox.shortValue();
        @m int mInteger = meterDoubleBox.intValue();
        @m long mLong = meterDoubleBox.longValue();
        @m float mFloat = meterDoubleBox.floatValue();
        @m double mDouble = meterDoubleBox.doubleValue();

        String x = meterDoubleBox.toString();
        x = Double.toString(meterDouble);
        x = Double.toHexString(meterDouble);

        int hash = meterDoubleBox.hashCode();

        boolean b = meterDoubleBox.isNaN();
        b = Double.isNaN(meterDouble);
        b = meterDoubleBox.isInfinite();
        b = Double.isInfinite(meterDouble);

        meterDoubleBox.equals(meterDoubleBox2);
        // comparison with dimensionless literals and objects, and null literals are
        // allowed
        meterDoubleBox.equals(5);
        meterDoubleBox.equals(dimensionlessDoubleBox);
        meterDoubleBox.equals(null);
        // otherwise comparison must be between the same unit
        //:: error: (comparison.unit.mismatch)
        meterDoubleBox.equals(secondDoubleBox);

        @Dimensionless int r = meterDoubleBox.compareTo(meterDoubleBox2);
        //:: error: (comparison.unit.mismatch)
        r = meterDoubleBox.compareTo(secondDoubleBox);

        r = Double.compare(meterDouble, meterDouble);
        //:: error: (comparison.unit.mismatch)
        r = Double.compare(meterDouble, secondDouble);

        @m long meterLong = Double.doubleToLongBits(meterDouble);
        meterLong = Double.doubleToRawLongBits(meterDouble);
        meterDouble = Double.longBitsToDouble(meterLong);
    }
}
